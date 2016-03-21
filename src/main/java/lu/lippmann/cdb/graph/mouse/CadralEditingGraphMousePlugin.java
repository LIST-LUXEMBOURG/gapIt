/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.mouse;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;

/**
 * 
 * @author
 *
 */
public class CadralEditingGraphMousePlugin extends EditingGraphMousePlugin<CNode, CEdge> {

	private final CommandDispatcher commandDispatcher;

	private CNode dragVertex;
	private CNode lastDragVertex;

	private Map<CNode,Color> pointedVertices;

	/**
	 * 
	 * @param vertexFactory
	 * @param edgeFactory
	 */
	public CadralEditingGraphMousePlugin(Factory<CNode> vertexFactory, Factory<CEdge> edgeFactory,final CommandDispatcher commandDispatcher) 
	{
		super(vertexFactory, edgeFactory);
		this.commandDispatcher=commandDispatcher;
		this.pointedVertices = new HashMap<CNode,Color>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton()!=3){ //not with left click
			@SuppressWarnings("unchecked")
			final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
			final Point2D p = e.getPoint();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();
			if (pickSupport != null) {
				final CNode vertex = pickSupport.getVertex(layout, p.getX(),p.getY());
				if (vertex == null) {
					return;
				}else{
					//set fields for mouseReleased
					startVertex = vertex;
					down = e.getPoint();
					vv.addPostRenderPaintable(edgePaintable);
					vv.addPostRenderPaintable(arrowPaintable);
					edgeIsDirected = EdgeType.DIRECTED;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//Don't create node or edge with right click !
		if(!e.isPopupTrigger()){
			@SuppressWarnings("unchecked")
			final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();

			final Point2D p = e.getPoint();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphWithOperations graph = (GraphWithOperations)layout.getGraph();

			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();

			if (pickSupport != null) {
				CNode vertex = pickSupport.getVertex(layout, p.getX(),p.getY());
				// if no vertex create one on the fly
				if (vertex == null) {
					final CNode newNode = vertexFactory.create();
					final Point2D newPos = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
					graph.addVertex(newNode,new CPoint(newPos.getX(),newPos.getY()));
					layout.lock(newNode, false);
					vertex = newNode;
				}else{
					//reset to initial color if needed
					if(startVertex != null && !startVertex.equals(vertex)){
						if(pointedVertices.containsKey(vertex)){
							vertex.setColor(pointedVertices.get(vertex));
						}
					}
					layout.lock(vertex, false);
				}
				// if the source & destination vertex are not the same : create edge
				if(vertex != null && startVertex != null){
					if(!startVertex.equals(vertex)){
						transformEdgeShape(down, down);
						transformArrowShape(down, e.getPoint());
						graph.addEdge(edgeFactory.create(), startVertex, vertex, edgeIsDirected);
					}
				}
			}
			//Reset fields
			startVertex = null;
			down = null;
			edgeIsDirected = EdgeType.UNDIRECTED;
			vv.removePostRenderPaintable(edgePaintable);
			vv.removePostRenderPaintable(arrowPaintable);

			//clear color mapping map
			pointedVertices.clear(); 
			dragVertex = null;
			lastDragVertex = null;

			vv.repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void mouseDragged(MouseEvent e) {
		if(checkModifiers(e)) {
			@SuppressWarnings("unchecked")
			final VisualizationViewer<CNode,CEdge> vv =(VisualizationViewer<CNode,CEdge>)e.getSource();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();
			if(startVertex != null) {
				transformEdgeShape(down, e.getPoint());
				if(edgeIsDirected == EdgeType.DIRECTED) {
					transformArrowShape(down, e.getPoint());
					final CNode pointedVertex = pickSupport.getVertex(layout,e.getX(),e.getY());
					if(pointedVertex!=null && pointedVertex != startVertex){
						if(!pointedVertices.containsKey(pointedVertex)){
							pointedVertices.put(pointedVertex,pointedVertex.getColor()); //save original color
						}
						Color prevColor = pointedVertex.getColor();
						if(pointedVertex.getColor().equals(pointedVertices.get(pointedVertex))){

							if(pointedVertex != this.lastDragVertex && this.lastDragVertex != null){
								lastDragVertex.setColor(pointedVertices.get(lastDragVertex));
							}
							//Don't change color if there is an existing edge
							if(layout.getGraph().findEdge(startVertex,pointedVertex)==null){
								if(GraphUtil.isDarkNode(pointedVertex)){
									if(prevColor.darker().equals(prevColor)){
										pointedVertex.setColor(Color.GRAY);
									}else{
										if(prevColor.brighter().equals(prevColor)){
											pointedVertex.setColor(Color.GRAY);
										}else{
											pointedVertex.setColor(pointedVertices.get(pointedVertex).brighter());
										}
									}
								}else{
									pointedVertex.setColor(pointedVertices.get(pointedVertex).darker());
								}
							}
						}
						this.lastDragVertex = this.dragVertex;
						this.dragVertex = pointedVertex;
					}else if(dragVertex!=null){
						dragVertex.setColor(pointedVertices.get(dragVertex));
					}
				}
			}
			vv.repaint();
		}
	}
	/**
	 * When we are editing the graph : publish an event to select the node if 
	 * we right click on an existing node (to edit its properties)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==3){ //right click
			final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
			final Point2D p = e.getPoint();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();
			if (pickSupport != null) {
				final CNode vertex = pickSupport.getVertex(layout, p.getX(),p.getY());
				if (vertex != null) {
					commandDispatcher.dispatch(new SelectNodeCommand(vertex,2));
				}else{
					final CEdge edge  = pickSupport.getEdge(layout,p.getX(),p.getY());
					if(edge != null){
						commandDispatcher.dispatch(new SelectEdgeCommand(edge,2));
					}
				}
			}
		}
	}


	/**
	 * Function to create the edge shape while drawing
	 * @param down
	 * @param out
	 */
	private void transformEdgeShape(Point2D down, Point2D out)
	{
		float x1 = (float)down.getX();
		float y1 = (float)down.getY();
		float x2 = (float)out.getX();
		float y2 = (float)out.getY();
		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
		float dx = x2 - x1;
		float dy = y2 - y1;
		float thetaRadians = (float)Math.atan2(dy, dx);
		xform.rotate(thetaRadians);
		float dist = (float)Math.sqrt(dx * dx + dy * dy);
		xform.scale((double)dist / rawEdge.getBounds().getWidth(), 1.0d);
		edgeShape = xform.createTransformedShape(rawEdge);
	}

	/**
	 * Function to draw the arrow while drawing
	 * @param down
	 * @param out
	 */
	private void transformArrowShape(Point2D down, Point2D out)
	{
		float x1 = (float)down.getX();
		float y1 = (float)down.getY();
		float x2 = (float)out.getX();
		float y2 = (float)out.getY();
		AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);
		float dx = x2 - x1;
		float dy = y2 - y1;
		float thetaRadians = (float)Math.atan2(dy, dx);
		xform.rotate(thetaRadians);
		arrowShape = xform.createTransformedShape(rawArrowShape);
	}

}
