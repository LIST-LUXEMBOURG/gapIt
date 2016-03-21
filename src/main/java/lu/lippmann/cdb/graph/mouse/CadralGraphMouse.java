/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.mouse;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JComboBox;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;


/**
 * 
 * @author
 *
 */
public class CadralGraphMouse extends DefaultModalGraphMouse<CNode, CEdge> implements ModalGraphMouse, ItemSelectable {

	private EditingGraphMousePlugin<CNode, CEdge> editingGraphMousePlugin;
	private CadralTaggingGraphMousePlugin taggingGraphMousePlugin;

	/** coordinates before moving nodes **/
	private final Map<Long,Point2D> before = new LinkedHashMap<Long, Point2D>();

	private CShape shape;
	private Color  color;

	private final CommandDispatcher commandDispatcher;

	private GraphWithOperations graph;

	/**
	 * Constructor.
	 */
	public CadralGraphMouse(final CShape shape,final Color color,final CommandDispatcher commandDispatcher) {
		super(1 / 1.1f,  1.1f);
		this.shape = shape;
		this.color = color;
		this.commandDispatcher=commandDispatcher;
		initPlugins();		
		setMode(Mode.TRANSFORMING);
	}

	/**
	 * {@inheritDoc}
	 * Mode.PICKING      : Move node & edge
	 * Mode.EDITING      : Add  node & edge
	 * Mode.TRANSFORMING : Move graph layout
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		before.clear();
		//System.out.println("Before map cleared");
		if(!mode.equals(Mode.EDITING)){
			@SuppressWarnings("unchecked")
			final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
			final Point2D p = e.getPoint();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();
			if (pickSupport != null) {
				final CNode vertex = pickSupport.getVertex(layout, p.getX(),p.getY());
				final CEdge edge = pickSupport.getEdge(layout, p.getX(), p.getY());

				if(mode.equals(Mode.TRANSFORMING)){
					if (vertex != null || edge!=null) {
						setMode(Mode.PICKING);
						vv.setCursor(Cursor.getPredefinedCursor(12));
					}
					if((vertex == null && edge==null) && (e.isShiftDown())) {
						setMode(Mode.PICKING);
						vv.setCursor(Cursor.getPredefinedCursor(12));
					}
				}else if(mode.equals(Mode.PICKING) && !e.isShiftDown()){
					if (vertex == null && edge==null) {
						setMode(Mode.TRANSFORMING);
					}
				}
				//Final choosen mode is transforming ! -> Deselect all nodes
				if(mode.equals(Mode.TRANSFORMING)){
					commandDispatcher.dispatch(new DeselectAllCommand());
				}

				//Save initial positions
				if(mode.equals(Mode.PICKING)){
					if(vertex!=null){
						saveInitialPositionOfPickedNodes(vv);
					}
				}
			}
		}

		//will call either :
		// - CadralEditingGraphMousePlugin.mousePressed(e)
		// - CadralPickingGraphMousePlugin.mousePressed(e)
		super.mousePressed(e);


		//System.out.println("Coords clicked : " + e.getPoint());
	}

	/**
	 * Save the layout transformation map before doing anything 
	 * TODO : we should take picked vertex instead of all vertices, but i don't know how to do it properly in mouseEnter ... 
	 * @param vv
	 * @param layout
	 * @param vertex
	 */
	private void saveInitialPositionOfPickedNodes(final VisualizationViewer<CNode, CEdge> vv) {
		final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
		for(CNode nodes : layout.getGraph().getVertices()){
			before.put(nodes.getId(),layout.transform(nodes));	
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if(mode.equals(Mode.PICKING)){
			@SuppressWarnings("unchecked")
			final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
			final Point2D p = e.getPoint();
			final Layout<CNode, CEdge> layout = vv.getModel().getGraphLayout();
			final GraphElementAccessor<CNode, CEdge> pickSupport = vv.getPickSupport();
			if (pickSupport != null) {
				final CNode vertex = pickSupport.getVertex(layout, p.getX(),p.getY());
				if(vertex!=null){
					//Check that we moved the selected vertex
					if(before != null && !layout.transform(vertex).equals(before.get(vertex.getId()))){
						boolean needsGroup = (vv.getPickedVertexState().getPicked().size() > 1);
						if(needsGroup){
							graph.startGroupOperation();
						}
						for(CNode picked : vv.getPickedVertexState().getPicked()){
							if(before.containsKey(picked.getId())){
								graph.moveNodeTo(picked,before.get(picked.getId()),layout.transform(picked));
							}
						}
						if(needsGroup){
							graph.stopGroupOperation();
						}
					}
				}
			}
		}
		//will call either :
		// - CadralEditingGraphMousePlugin.mouseReleased(e)
		// - CadralPickingGraphMousePlugin.mouseReleased(e)
		//System.out.println("Position : " + e.getPoint());
		super.mouseReleased(e);
	}

	/**
	 * 
	 */
	private void initPlugins() {
		final Factory<CNode> vertexFactory = new Factory<CNode>() { // My vertex factory
			@Override
			public CNode create() {
				final CNode res =  new CNode(graph.nextUntitledVertexLabel());
				res.setShape(shape);
				res.setColor(color);
				return res;
			}
		};

		final Factory<CEdge> edgeFactory = new Factory<CEdge>() { // My edge factory
			@Override
			public CEdge create() {
				return new CEdge(graph.nextUntitledEdgeLabel(),"true");
			}
		};

		this.editingGraphMousePlugin = new CadralEditingGraphMousePlugin(vertexFactory, edgeFactory,commandDispatcher);
		this.taggingGraphMousePlugin=new CadralTaggingGraphMousePlugin(commandDispatcher);
		this.pickingPlugin = new CadralPickingGraphMousePlugin(commandDispatcher);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMode(final Mode m) {
		super.setMode(m);

		if (m == Mode.EDITING) 
		{
			remove(taggingGraphMousePlugin);
			remove(translatingPlugin);
			remove(rotatingPlugin);
			remove(shearingPlugin);
			remove(pickingPlugin);
			add(animatedPickingPlugin);
			add(editingGraphMousePlugin);
		} 
		else if (m==Mode.ANNOTATING) 
		{	
			remove(translatingPlugin);
			remove(rotatingPlugin);
			remove(shearingPlugin);
			remove(pickingPlugin);
			remove(editingGraphMousePlugin);
			add(taggingGraphMousePlugin);
		}		
		else 
		{
			remove(taggingGraphMousePlugin);
			remove(editingGraphMousePlugin);
			remove(rotatingPlugin);
			remove(shearingPlugin);
		}
	}

	/**
	 * 
	 * @param shape
	 */
	public void setShape(CShape shape){
		this.shape = shape;
	}

	/**
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * 
	 * @param clickedGraph
	 */
	public void setClickedGraph(GraphWithOperations clickedGraph) {
		this.graph = clickedGraph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JComboBox getModeComboBox() {
		if(modeBox == null) {
			modeBox = new JComboBox(new Mode[]{Mode.TRANSFORMING, Mode.PICKING,Mode.EDITING,Mode.ANNOTATING});
			modeBox.addItemListener(getModeListener());
		}
		modeBox.setSelectedItem(mode);
		return modeBox;
	}

}
