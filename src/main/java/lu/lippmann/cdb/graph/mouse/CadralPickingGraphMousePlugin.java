/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.mouse;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.FileUtil;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;


/**
 * 
 * @author
 *
 */
public class CadralPickingGraphMousePlugin extends PickingGraphMousePlugin<CNode, CEdge> 
{
	//
	//
	//

	private CNode selectedVertex;
	private CEdge selectedEdge;

	/** */
	private final CommandDispatcher commandDispatcher;
	private boolean selectedVertexIsPreviouslyPicked;
	private boolean selectedEdgeIsPreviouslyPicked;


	//
	//
	//

	/**
	 * Constructor.	 
	 */
	public CadralPickingGraphMousePlugin(final CommandDispatcher commandDispatcher)
	{
		this.commandDispatcher=commandDispatcher;
	}


	//
	//
	//


	@SuppressWarnings({"unchecked" })
	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);

		final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
		PickedState<CNode> pickedVertexState = vv.getPickedVertexState();
		PickedState<CEdge> pickedEdgeState   = vv.getPickedEdgeState();
		//Saved selected vertex & edge
		if (this.vertex != null) {
			selectedVertex = vertex;
			selectedVertexIsPreviouslyPicked = !pickedVertexState.isPicked(selectedVertex);
		} else {
			selectedVertex = null;
			selectedVertexIsPreviouslyPicked = false;
		}
		if (this.edge != null) {
			selectedEdge = edge;
			selectedEdgeIsPreviouslyPicked   = !pickedEdgeState.isPicked(selectedEdge);
		} else {
			selectedEdge = null;
			selectedEdgeIsPreviouslyPicked = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void mouseReleased(MouseEvent e) {

		final VisualizationViewer<CNode, CEdge> vv = (VisualizationViewer<CNode, CEdge>) e.getSource();
		PickedState<CEdge> pickedEdgeState   = vv.getPickedEdgeState();


		//System.out.println("Mouse realeased");
		
		final Point2D savedDown = down;

		super.mouseReleased(e);

		//clearPickedState(e);
		
		if(savedDown==null){
			return;
		}

		//Pick edges using vertex picked by super.mouseReleased !
		final Rectangle2D pickRectangle = new Rectangle2D.Double();
		pickRectangle.setFrameFromDiagonal(savedDown, e.getPoint());
		final Graph<CNode,CEdge> graph = vv.getGraphLayout().getGraph();
		final Set<CNode> nodes = vv.getPickedVertexState().getPicked();
		for(final CNode node : nodes){
			final Collection<CEdge> edges = graph.getIncidentEdges(node);
			if(edges != null){
				for(final CEdge edge : edges){
					final CNode src = graph.getSource(edge);
					final CNode dst = graph.getDest(edge);
					if(nodes.contains(src) && nodes.contains(dst)){
						pickedEdgeState.pick(edge,true);
					}
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		/**
		 * Avoid selected a node & edge without using addToSelectionModifier
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final VisualizationViewer<CNode,CEdge> vv = (VisualizationViewer)e.getSource();
		final GraphElementAccessor<CNode,CEdge> pickSupport = vv.getPickSupport();
		final Point2D ip = e.getPoint();
		final Layout<CNode,CEdge> layout = vv.getGraphLayout();
		
		if(e.getButton()==MouseEvent.BUTTON3){
			final CNode rightClickVertex = pickSupport.getVertex(layout,ip.getX(),ip.getY());
			if(rightClickVertex!=null){
				final JPopupMenu popup = new JPopupMenu("Options");
				popup.setLightWeightPopupEnabled(false); 
				final JMenuItem getSubGraphItem = new JMenuItem("Select connected graph");
				getSubGraphItem.setIcon(FileUtil.getImageFromFile("move.png"));
				getSubGraphItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						commandDispatcher.dispatch(new SelectSubGraphCommand(rightClickVertex,e.getModifiers()==addToSelectionModifiers));
					}
				});
				popup.add(getSubGraphItem);
				
				final JMenuItem clusterSubGraphItem = new JMenuItem("Reorganize connected graph");
				clusterSubGraphItem.setIcon(FileUtil.getImageFromFile("tree.gif"));
				clusterSubGraphItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						commandDispatcher.dispatch(new SelectSubGraphCommand(rightClickVertex,e.getModifiers()==addToSelectionModifiers));
						commandDispatcher.dispatch(new ClusterGraphCommand());
					}
				});
				popup.add(clusterSubGraphItem);

				
				final Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, popup.getForeground());
				final TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, popup.getLabel(),
						TitledBorder.LEFT, TitledBorder.ABOVE_TOP, popup.getFont(), popup.getForeground());
				popup.setBorder(BorderFactory.createCompoundBorder(popup.getBorder(),labelBorder));
				final JComponent comp = new JPanel();
				comp.setComponentPopupMenu(popup);
				popup.show(e.getComponent(), e.getX(),e.getY());
			}
			super.mouseClicked(e);
			return;
		}

		//Clear picked state
		clearPickedState(e);


		//Dispatch command to select node & edge -> open modification panel
		final PickedState<CNode> pickedVertexState = vv.getPickedVertexState();
		final PickedState<CEdge> pickedEdgeState   = vv.getPickedEdgeState();
		final int count = e.getClickCount();
		if(selectedVertex!=null){
			if(e.getModifiers()!=addToSelectionModifiers){
				pickedVertexState.pick(selectedVertex,true);
			}else{
				pickedVertexState.pick(selectedVertex,!selectedVertexIsPreviouslyPicked);
			}
			commandDispatcher.dispatch(new SelectNodeCommand(selectedVertex,count));
		}
		if(selectedEdge!=null){
			if(e.getModifiers()!=addToSelectionModifiers){
				pickedEdgeState.pick(selectedEdge,true);
			}else{
				pickedEdgeState.pick(selectedEdge,!selectedEdgeIsPreviouslyPicked);
			}
			commandDispatcher.dispatch(new SelectEdgeCommand(selectedEdge,count));
		}
		super.mouseClicked(e);
	}
	
	
	/**
	 * 
	 * @param e
	 */
	private void clearPickedState(MouseEvent e){
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final VisualizationViewer<CNode,CEdge> vv = (VisualizationViewer)e.getSource();
		final GraphElementAccessor<CNode,CEdge> pickSupport = vv.getPickSupport();
		final Layout<CNode,CEdge> layout = vv.getGraphLayout();
		final PickedState<CNode> pickedVertexState = vv.getPickedVertexState();
		final PickedState<CEdge> pickedEdgeState   = vv.getPickedEdgeState();
		final Point2D ip = e.getPoint();
		
		if(pickSupport != null && pickedVertexState != null) {
			if(e.getModifiers() != addToSelectionModifiers){
				if(pickSupport.getEdge(layout, ip.getX(), ip.getY())!=null
						||pickSupport.getVertex(layout, ip.getX(), ip.getY())!=null){
					pickedVertexState.clear();
					pickedEdgeState.clear();
				}
			}
		}
	}
	
}
