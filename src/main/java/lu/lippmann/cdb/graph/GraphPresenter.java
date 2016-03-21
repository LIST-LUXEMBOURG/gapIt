/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.Cursor;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;

import lu.lippmann.cdb.App;
import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.mvp.Presenter;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.graph.mouse.CadralGraphMouse;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.*;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.bushe.swing.event.annotation.EventSubscriber;

import weka.core.Instances;

import com.google.inject.Inject;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.picking.PickedState;


/**
 * GraphPresenter.
 * 
 * @author Acora Team
 */
public class GraphPresenter implements Presenter<GraphView> 
{
	//
	// Instance fields
	//

	private final GraphView view;

	@Inject 
	private ApplicationContext applicationContext;

	private final EventPublisher eventPublisher;
	private final CommandDispatcher commandDispatcher;

	/** buffer of copy & paste */
	private GraphWithOperations gwoCopy;
	private int pasteCounter = 1;


	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	@Inject
	public GraphPresenter(final GraphView view,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher) 
	{
		view.init();
		this.view = view;

		this.gwoCopy = new GraphWithOperations();

		this.eventPublisher=eventPublisher;
		this.commandDispatcher=commandDispatcher;

		eventPublisher.markAsEventListener(this);
		commandDispatcher.markAsCommandHandler(this);
	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() 
	{
		
		//When the size of the graph will be set -> auto-fit
		if(view instanceof GraphViewImpl){
			((GraphViewImpl)view).addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					view.autoFit();
				}
			});
		}
		
		if (App.BOOT_WITH_NEW_GRAPH)
		{
			final CGraph cgraph=new CGraph();
			final GraphWithOperations gwo=new GraphWithOperations();
			gwo.setWorkingUser(applicationContext.getUser());
			final Layout<CNode,CEdge> layout=view.getVisualisationViewer().getGraphLayout();//
			//new StaticLayout<CNode,CEdge>(gwo);
			cgraph.setInternalLayout(layout);
			applicationContext.setCadralGraph(cgraph);
			reInitView(cgraph,true);
		}
		else
		{
			reInitView(null,true);
		}
	}

	/**
	 * Reinit view.
	 */
	private void reInitView(final CGraph cgraph,boolean reInitVars)
	{	
		
		if(cgraph!=null && cgraph.getInternalGraph() instanceof GraphWithOperations){
			final GraphWithOperations graph = (GraphWithOperations)cgraph.getInternalGraph();
			((CadralGraphMouse)view.getVisualisationViewer().getGraphMouse()).setClickedGraph(graph);
		}

		view.resetLayout();

		//Clear pick state
		view.getVisualisationViewer().getPickedVertexState().clear();
		view.getVisualisationViewer().getPickedEdgeState().clear();

		// Reinit graph view
		view.setCGraph(cgraph);		

		// Clear copy/paste buffer
		gwoCopy = new GraphWithOperations();
		pasteCounter = 1;

		//Clear variable to default state
		if(reInitVars){
			eventPublisher.publish(new VariableDefinitionHasChangedEvent());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphView getDisplay() {
		return view;
	}

	@EventSubscriber(eventClass = GraphReloadedEvent.class)
	public void onAskReload(GraphReloadedEvent evt) 
	{
		reInitView(applicationContext.getCadralGraph(),evt.isReloadVariables());
	}

	@EventSubscriber(eventClass = GraphRepaintedEvent.class)
	public void onAskRepaint(GraphRepaintedEvent evt) 
	{
		view.repaint();
	}

	@EventSubscriber(eventClass = SelectAllCommand.class)
	public void handle(SelectAllCommand cmd) {
		view.selectAll(true);
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = DeselectAllCommand.class)
	public void handle(DeselectAllCommand cmd) {
		view.selectAll(false);
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = ClusterGraphCommand.class)
	public void handle(ClusterGraphCommand cmd) {
		view.reorganize();
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = DeleteCommand.class)
	public void handle(DeleteCommand cmd) {
		view.deleteSelected();
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = HighlightCommand.class)
	public void handle(final HighlightCommand cmd) {
		view.highlightPath(cmd.getPath());
	}

	@EventSubscriber(eventClass = UnHighlightCommand.class)
	public void handle(final UnHighlightCommand cmd) {
		view.resetVertexAndEdgeShape();
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = UndoCommand.class)
	public void handle(UndoCommand cmd){
		final GraphWithOperations gwo = ((GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph());
		gwo.previous(applicationContext.getUser());
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = RedoCommand.class)
	public void handle(RedoCommand cmd){
		final GraphWithOperations gwo = ((GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph());
		gwo.next(applicationContext.getUser());
		eventPublisher.publish(new GraphRepaintedEvent());
	}

	@EventSubscriber(eventClass = CopySelectionCommand.class)
	public void handle(CopySelectionCommand cmd){
		final Set<CNode> nodes = new HashSet<CNode>(view.getVisualisationViewer().getPickedVertexState().getPicked());
		final Set<CEdge> edges = new HashSet<CEdge>(view.getVisualisationViewer().getPickedEdgeState().getPicked());

		if(view.getGraph() instanceof GraphWithOperations){
			this.gwoCopy = new GraphWithOperations();
			for(CNode node: nodes){
				gwoCopy.addVertex(node);
			}
			for(CEdge edge : edges){
				gwoCopy.addEdge(edge, view.getGraph().getSource(edge),view.getGraph().getDest(edge),EdgeType.DIRECTED);
			}
		}else{
			throw new IllegalStateException("Invalid graph type : " + view.getGraph().getClass().getName());
		}

		this.pasteCounter = 1;
	}

	@EventSubscriber(eventClass = PasteSelectionCommand.class)
	public void handle(PasteSelectionCommand cmd){

		final boolean bufferIsEmpty 		= gwoCopy.getVertices().isEmpty();

		//Ignore empty buffer
		if(bufferIsEmpty) return;

		final Layout<CNode,CEdge> layout	= view.getVisualisationViewer().getGraphLayout();
		final GraphWithOperations gwo 		= ((GraphWithOperations)layout.getGraph());
		final PickedState<CNode> pickNode   = view.getVisualisationViewer().getPickedVertexState();
		final PickedState<CEdge> pickEdge 	= view.getVisualisationViewer().getPickedEdgeState();
		final Map<Long,Long> oldNewId = new HashMap<Long, Long>();

		gwo.setChangeEnabled(false);

		pickNode.clear();
		pickEdge.clear();


		/** needs to copy because it seems pickNode change nodeCopy someway **/ 
		for(final CNode node : gwoCopy.getVertices()){
			final CNode newNode = new CNode(node.getName()+"(new)",node.getColor(),node.getShape());
			oldNewId.put(node.getId(),newNode.getId());
			// position to change
			final Point2D iPos   = layout.transform(node);
			final CPoint point  = new CPoint(iPos.getX()+pasteCounter*50.0 ,iPos.getY()+pasteCounter*50.0);

			gwo.addVertex(newNode,point);

			// change picked state
			pickNode.pick(newNode,true);
		}

		pasteCounter++;

		/** needs to copy because it seems pickEdge change edgeCopy someway **/
		for(final CEdge edge : gwoCopy.getEdges()){
			final CNode sNode = gwoCopy.getSource(edge);
			final CNode dNode = gwoCopy.getDest(edge);
			final CNode nsNode = new CNode(oldNewId.get(sNode.getId()),sNode.getName());
			final CNode ndNode = new CNode(oldNewId.get(dNode.getId()),dNode.getName());
			final CEdge newEdge = new CEdge(edge.getName(),edge.getExpression());
			gwo.addEdge(newEdge,nsNode,ndNode);
			// change picked state
			pickEdge.pick(newEdge,true);
		}


		//Switch to pick mode to be able to move pasted graph & change cursor right away
		((CadralGraphMouse)view.getVisualisationViewer().getGraphMouse()).setMode(Mode.PICKING);
		view.getVisualisationViewer().setCursor(Cursor.getPredefinedCursor(12)); //FIXME : dirty

		//Refresh structure & re-enable change listener
		gwo.setChangeEnabled(true);
		eventPublisher.publish(new GraphStructureChangedEvent());

		//Let's select edit button because we will be able to move pasted elements
		commandDispatcher.dispatch(new ClickViewModeCommand(ViewMode.Edit));
	}

	
	@EventSubscriber(eventClass=SelectSubGraphCommand.class)
	public void pickSubGraphOf(SelectSubGraphCommand evt){
		final VisualizationViewer<CNode,CEdge> vv = view.getVisualisationViewer();
		if(!evt.isAddToSelection()){
			vv.getPickedVertexState().clear();
			vv.getPickedEdgeState().clear();
		}
		final Graph<CNode,CEdge> subGraph = GraphUtil.getSubGraph(evt.getNode(),vv.getGraphLayout().getGraph());
		for(CNode node : subGraph.getVertices()){
			vv.getPickedVertexState().pick(node,true);
			for(CEdge edge : subGraph.getOutEdges(node)){
				vv.getPickedEdgeState().pick(edge,true);
			}
		}
	}

	

	@EventSubscriber(eventClass=SetViewModeCommand.class)
	public void handleViewModeCommand(final SetViewModeCommand command)
	{
		view.setViewMode(command.getViewMode());
	}	

	@EventSubscriber(eventClass=SetShapeCommand.class)
	public void handleSetShapeCommand(final SetShapeCommand command)
	{
		view.setShape(command.getShape());
	}	

	@EventSubscriber(eventClass=SetColorCommand.class)
	public void handleSetColorCommand(final SetColorCommand command)
	{
		view.setColor(command.getColor());
	}	
	
	@EventSubscriber(eventClass=AutoFitCommand.class)
	public void handleAutoFitCommand(final AutoFitCommand command)
	{
		view.autoFit();
	}
	
	@EventSubscriber(eventClass = GraphStructureChangedEvent.class)
	public void onVariableChanged(final GraphStructureChangedEvent evt) 
	{
		final Layout<CNode,CEdge> layout	= view.getVisualisationViewer().getGraphLayout();
		final GraphWithOperations gwo 		= ((GraphWithOperations)layout.getGraph());
		if(evt.isGlobal()||evt.isHistoricalChange()){
			//GraphUtil.updateUsedVariablesInGraph(gwo);
			eventPublisher.publish(new VariableDefinitionHasChangedEvent());
		}else if(evt.getOperation().equals(Operation.EDGE_DATA_UPDATED) || evt.getOperation().equals(Operation.EDGE_REMOVED)){
			boolean hasChanged = true;//GraphUtil.updateUsedVariablesInGraph(gwo);
			if(hasChanged){
				eventPublisher.publish(new VariableDefinitionHasChangedEvent());
			}
			
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventSubscriber(eventClass = GraphLinkedWithDataSetEvent.class)
	public void onGraphLinkedWithDataSet(final GraphLinkedWithDataSetEvent evt) throws IllegalStateException{
		final Instances instances     = evt.getDataSet();
		final GraphWithOperations gwo = evt.getGraph();
		final Object[] mapRep = WekaDataStatsUtil.buildNodeAndEdgeRepartitionMap(gwo,instances);
		view.updateVertexShapeTransformer((Map<CNode,Map<Object,Integer>>)mapRep[0]);
		view.updateEdgeShapeRenderer((Map<CEdge,Float>)mapRep[1]);
	}
	
	@EventSubscriber(eventClass = GraphUnlinkedWithDataSetEvent.class)
	public void onGraphUnlinkedWithDataSet(final GraphUnlinkedWithDataSetEvent evt) throws IllegalStateException{
		view.resetVertexAndEdgeShape();
	}

}
