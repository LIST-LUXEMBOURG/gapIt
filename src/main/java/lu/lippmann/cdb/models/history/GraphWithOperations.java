/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models.history;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import javax.swing.JOptionPane;

import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;


/**
 * 
 * 
 * @author Yoann DIDRY, Olivier PARISOT
 */
public class GraphWithOperations extends DirectedSparseGraph<CNode,CEdge> implements CGraphElement{

	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID = 8981348482740531L;
	/** Random generator. */
	private static final Random RANDOM = new Random();
	/** Logger */
	protected static final Logger GRAPH_LOGGER =  Logger.getLogger(GraphWithOperations.class.getName());

	/** Hookup to close log file handler **/
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run() 
			{	
				for(Handler h : GRAPH_LOGGER.getHandlers())
				{
					if(h != null){
						h.close();
					}
					GRAPH_LOGGER.removeHandler(h);
				}


			}			
		});
	}


	//
	// Instance fields
	//

	protected CGraphElement cei;

	protected final List<GraphOperation> operations = new ArrayList<GraphOperation>();
	protected int operationIndex = 0;
	protected boolean lastOperationStatus;

	protected Listener<Operation> structureChange;

	protected Listener<CNodePosition> nodeMoved;
	protected Listener<CLayoutTransition> layoutChanged;

	/** used to force to disable change event in case it's needed */
	protected boolean disableChange = false;

	protected boolean isGroupOperation		  = false;
	protected boolean isHistoryRevertOperation = false;
	protected long groupCount = 0;
	protected long inverseGroupCount = -1l;

	/** tweaking **/
	protected final Map<Long,CNode> mapNode = new HashMap<Long, CNode>();
	protected final Map<Long,CEdge> mapEdge = new HashMap<Long, CEdge>();
	protected final Map<CNode,GraphOperation> nodeAddedMap = new HashMap<CNode, GraphOperation>();

	private CUser workingUser;

	/** is the graph cyclic ? **/
	protected boolean isGraphAlreadyCyclic;

	/** new untitled label count **/
	protected int untitledVertexCount = 0;
	protected int untitledEdgeCount   = 0;

	/** Has the graph been modified**/
	protected boolean dirty;

	/** List of variables used in the graph **/
	protected Set<CVariable> variables;

	//
	// Constructors
	//

	/**
	 * 
	 * @param graph
	 */
	public GraphWithOperations()
	{
		super();
		this.cei=new CGraphElementImpl(RANDOM.nextLong(),"hmmhmm");
		this.dirty = false;
		this.variables = new HashSet<CVariable>();
		initLogger();
	}


	/**
	 * Initialize the logger.
	 */
	private void initLogger() {
		if(GRAPH_LOGGER.getHandlers().length==0){
			FileHandler fh;
			try {

				//create log dir on the fly
				final File logdir = new File("log");
				if (!logdir.exists()) {
					if (!logdir.mkdir()) throw new RuntimeException("log directory not created!");
				}

				GRAPH_LOGGER.setUseParentHandlers(false);

				fh = new FileHandler("log/graph.log",false);
				fh.setFormatter(new Formatter() {
					private final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
					@Override
					public String format(LogRecord record) {
						final String logString = dateFormat.format(new Date(record.getMillis()));
						return "["+logString+"] "+record.getMessage()+"\n";
					}
				});

				GRAPH_LOGGER.addHandler(fh);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	//
	// Instance methods
	//

	/**
	 * Handle implicit transformations such as linked nodes & non existing nodes while adding edges
	 * @param o
	 * @param parameters
	 */
	protected boolean handleImplicitTransformations(final CUser user,final Operation o,final Object... parameters){
		if(user!=null){
			// use 'fine()' to avoid to build big log files			
			GRAPH_LOGGER.fine("Handle implicit transformation by "+user+":"+o+" "+Arrays.asList(parameters));
		}

		boolean res = false;
		//remove edges if node is linked !

		//FIXME : dirt patch != null
		if(o.equals(Operation.NODE_REMOVED) && this.getIncidentEdges((CNode)parameters[0])!=null && !this.getIncidentEdges((CNode)parameters[0]).isEmpty()){
			final List<CEdge> edges = Collections.unmodifiableList(new ArrayList<CEdge>(this.getIncidentEdges((CNode)parameters[0])));
			for(CEdge incidentEdge : edges){
				transform(true,false,user,Operation.EDGE_REMOVED, incidentEdge, this.getSource(incidentEdge),this.getDest(incidentEdge));
			}
			//call again with no more edges
			transform(true,false,user,Operation.NODE_REMOVED,parameters);
			res = true;
			//create source&dest node if not exists !
		}else if(o.equals(Operation.EDGE_ADDED) && 
				((!this.containsVertex((CNode)parameters[1]))||(!this.containsVertex((CNode)parameters[2]) ))
				){
			if(!this.containsVertex((CNode)parameters[1])){
				transform(true,false,user,Operation.NODE_ADDED,(CNode)parameters[1]);
			}
			if(!this.containsVertex((CNode)parameters[2])){
				transform(true,false,user,Operation.NODE_ADDED,(CNode)parameters[2]);
			}
			//call again with nodes created !
			transform(true,false,user,Operation.EDGE_ADDED,parameters);
			res = true;
		}
		return res;
	}


	/**
	 * Transform used by addVertex/addEdge/etc : removes history !
	 * @param o
	 * @param parameters
	 * @return
	 */
	protected void transform(Operation o,Object... parameters){
		transform(true,true,workingUser,o,parameters);
	}

	/**
	 * Do the transformation
	 * @param o
	 * @param parameters
	 */
	@SuppressWarnings("unchecked")
	protected void doTransform(Operation o, Object... parameters) {
		switch(o){
		case NODE_ADDED:
			final CNode tmpNode = (CNode)parameters[0];
			lastOperationStatus = super.addVertex((CNode)parameters[0]);
			if(parameters.length==3){
				if(nodeMoved!=null){
					nodeMoved.onAction(new CNodePosition((CNode)parameters[0],new Point2D.Double((Double)parameters[1],(Double)parameters[2])));
				}
			}
			mapNode.put(tmpNode.getId(), tmpNode);
			break;
		case EDGE_ADDED:
			final CEdge tmpEdge = (CEdge)parameters[0];
			this.lastOperationStatus = super.addEdge(tmpEdge,(CNode)parameters[1],(CNode)parameters[2],EdgeType.DIRECTED);
			mapEdge.put(tmpEdge.getId(), tmpEdge);
			break;
		case NODE_DATA_UPDATED:
			final CNode tmpNode3 = (CNode)parameters[0];
			final CNode node    = mapNode.get(tmpNode3.getId());
			node.setName((String)parameters[2]);
			node.setShape((CShape)parameters[4]);
			node.setColor((Color)parameters[6]);
			node.clearAndAddTags((List<CTag>)parameters[8]);
			lastOperationStatus = true;
			break;
		case EDGE_DATA_UPDATED:
			final CEdge tmpEdge1 = (CEdge)parameters[0];
			final CEdge  edge    = mapEdge.get(tmpEdge1.getId());
			edge.setName((String)parameters[2]);
			edge.setExpression((String)parameters[4]);
			edge.clearAndAddTags((List<CTag>)parameters[6]);
			this.lastOperationStatus = true;
			break;
		case NODE_REMOVED:
			final CNode tmpNode4 = (CNode)parameters[0];
			this.lastOperationStatus = super.removeVertex(tmpNode4);
			break;
		case EDGE_REMOVED:
			final CEdge tmpEdge2 = (CEdge)parameters[0];
			this.lastOperationStatus = super.removeEdge(tmpEdge2);
			break;
		case NODE_MOVED:
			this.lastOperationStatus = true;
			if(nodeMoved!=null){
				nodeMoved.onAction(new CNodePosition((CNode)parameters[0],new Point2D.Double((Double)parameters[2],(Double)parameters[4])));
			}
			break;
		case LAYOUT_CHANGED:
			final Map<CNode,CPoint>  oldLayout = (Map<CNode,CPoint>)parameters[0];
			final Map<CNode,CPoint>  newLayout = (Map<CNode,CPoint>)parameters[1];
			this.lastOperationStatus = true;
			if(layoutChanged!=null){
				layoutChanged.onAction(new CLayoutTransition(oldLayout, newLayout));
			}/*else{
				System.out.println("Can't call layout change listener, none set !");
			}*/
			break;
		case VARIABLE_CHANGED:
			final CVariable oldVar = (CVariable)parameters[0];
			final CVariable newVar = (CVariable)parameters[1];
			//..
			//A -> null : variable removed
			//null -> A : variable added
			//A -> B (other key)
			//Update definition
			if(oldVar!=null && newVar!=null && newVar.getKey().equals(oldVar.getKey())){
				this.variables.remove(oldVar);
				this.variables.add(newVar);
			}
			this.lastOperationStatus = true;
			break;
		default:
			break;
		}
	}


	/**
	 * @return
	 */
	public void transform(final boolean saveOperation,final boolean dropHistory,final CUser user,final Operation o,final Object... parameters){
		
		// use 'fine()' to avoid to build big log files
		GRAPH_LOGGER.fine("Transforming by "+(user!=null?user:"anonymous")+": saveOperation="+saveOperation+",dropHistory="+dropHistory+","+o+" "+Arrays.asList(parameters));		

		//Clean history if needed
		final ArrayList<Object> params = new ArrayList<Object>(Arrays.asList(parameters));
		if(dropHistory){
			boolean saveDisableChange 	 = disableChange;
			disableChange    = true;
			if(operationIndex!=operations.size()){

				int clonedOperationIndex = operationIndex;
				List<GraphOperation> clonedOperations = new LinkedList<GraphOperation>(operations);

				if(!clonedOperations.isEmpty()){

					startHistoryRevertOperation();

					for(int i = clonedOperations.size() -1 ; i >= clonedOperationIndex ; i--) {
						final GraphOperation graphOp = clonedOperations.get(i);
						final GraphOperation graphOpInv = computeInverseOperation(user,graphOp);
						graphOpInv.setIdGroup(inverseGroupCount);
						this.operations.add(graphOpInv);
						transform(true,false,user,graphOpInv.getOperation(), graphOpInv.getParameters().toArray());
					}

					stopHistoryRevertOperation();
				}
				operationIndex = operations.size();
			}
			disableChange    = saveDisableChange;
		}

		//Handle implicit transformation if needed !
		if(!handleImplicitTransformations(user,o,parameters)){

			//do core transform
			doTransform(o, parameters);

			//Save the operation in the history
			if(saveOperation){
				if(operationIndex==operations.size()){
					final GraphOperation operation = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,o,params);
					if(isGroupOperation){
						operation.setIdGroup(groupCount);
					}else if(isHistoryRevertOperation){
						operation.setIdGroup(inverseGroupCount);
					}
					if(o.equals(Operation.NODE_ADDED)){
						nodeAddedMap.put((CNode)parameters[0],operation);
					}
					this.operations.add(operation);
					this.operationIndex++; 
				}
			}
		}
		/**
		 * Fire structure change listener
		 */
		if(!disableChange && structureChange!=null){
			structureChange.onAction(o);
			dirty = true;
		}
	}



	/**
	 * (o1,n1,o2,n2,o3,n3,...) -> (n1,o1,n2,o2,n3,o3,....)
	 * @param parameters
	 */
	private ArrayList<Object> switchParameterFrom(ArrayList<Object> params, int start){
		final ArrayList<Object> res = new ArrayList<Object>(params);
		for(int i = start ; i+1 < params.size() ; i+=2){
			final Object oldValue = params.get(i);
			final Object newValue = params.get(i+1);
			res.set(i,newValue);
			res.set(i+1,oldValue);
		}
		return res;
	}

	/**
	 * 
	 * @param operation
	 */
	protected GraphOperation computeInverseOperation(final CUser user,final GraphOperation operation){
		GraphOperation res = null;
		final ArrayList<Object> params = operation.getParameters();
		switch(operation.getOperation()){
		case NODE_ADDED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.NODE_REMOVED,params);
			break;
		case EDGE_ADDED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.EDGE_REMOVED,params);
			break;
		case NODE_DATA_UPDATED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.NODE_DATA_UPDATED,switchParameterFrom(params,1));
			break;
		case EDGE_DATA_UPDATED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.EDGE_DATA_UPDATED,switchParameterFrom(params,1));
			break;
		case NODE_REMOVED:
			final ArrayList<Object> parametersAdded = nodeAddedMap.get((CNode)params.get(0)).getParameters();
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.NODE_ADDED,parametersAdded);
			break;
		case EDGE_REMOVED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.EDGE_ADDED,params);
			break;
		case NODE_MOVED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.NODE_MOVED,switchParameterFrom(params,1));
			break;
		case LAYOUT_CHANGED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.LAYOUT_CHANGED,switchParameterFrom(params,0));
			break;
		case VARIABLE_CHANGED:
			res = new GraphOperation(untitledVertexCount,untitledEdgeCount,user,Operation.VARIABLE_CHANGED,switchParameterFrom(params,0));
			break;
		default:
			break;
		}
		return res;
	}


	/**
	 * 
	 * @return
	 */
	public void inverseTransform(final CUser user,final Operation o,final Object... parameters){
		if(user!=null){
			GRAPH_LOGGER.info("Inverse transforming by "+user+" "+o+" "+Arrays.asList(parameters));
		}
		final GraphOperation inverseOperation = computeInverseOperation(user,new GraphOperation(untitledVertexCount,untitledEdgeCount,user,o,new ArrayList<Object>(Arrays.asList(parameters))));
		doTransform(inverseOperation.getOperation(),inverseOperation.getParameters().toArray());
		if(!disableChange && structureChange!=null){
			structureChange.onAction(inverseOperation.getOperation());
		}
	}


	/**
	 * 
	 * @return
	 */
	public void previous(final CUser user){
		GRAPH_LOGGER.info("Previous by "+user+" currentIndex="+operationIndex);
		if(operationIndex - 1 >=0){
			final GraphOperation go = operations.get(operationIndex-1);
			operationIndex--;
			inverseTransform(user,go.getOperation(),go.getParameters().toArray());
		}
	}

	/**
	 * 
	 * @return
	 */
	public void next(final CUser user){
		GRAPH_LOGGER.info("Next by "+user+" currentIndex="+operationIndex);
		if(operationIndex < operations.size()){
			final GraphOperation go = operations.get(operationIndex);
			transform(false,false,user,go.getOperation(),go.getParameters().toArray());
			operationIndex++;
		}
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	protected void moveAt(final CUser user,final int index){
		GRAPH_LOGGER.info("MoveAt by "+user+" currentIndex="+operationIndex+", going to:"+index);
		disableChange = true;
		if(index>=0){
			if(operationIndex>index+1){
				while(operationIndex>index+1){
					previous(user);
				}
			}else{
				while(operationIndex<index+1){
					next(user);
				}
			}
		}
		disableChange = false;
	}

	/**
	 * 
	 * @param parameter
	 */
	public void moveAt(GraphOperation parameter) {
		final int idx = operations.indexOf(parameter);
		if(idx != -1){
			moveAt(parameter.getUser(),idx);
		}
	}

	/**
	 * @return the graph
	 */
	public Graph<CNode,CEdge> getGraph() {
		return this;
	}


	/**
	 * @return the lastOperationStatus
	 */
	public boolean lastOperationStatus() {
		return lastOperationStatus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addVertex(CNode vertex) {
		if(!containsVertex(vertex)){
			transform(Operation.NODE_ADDED, vertex);
			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @param vertex
	 * @param point
	 * @return
	 */
	public boolean addVertex(CNode vertex,CPoint point) {
		if(!containsVertex(vertex)){
			transform(Operation.NODE_ADDED, vertex, point.getX(),point.getY());
			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeVertex(CNode vertex) {
		if(containsVertex(vertex)){
			transform(Operation.NODE_REMOVED, vertex);
			return lastOperationStatus;	
		}else{
			return false;
		}
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addEdge(CEdge edge, CNode start, CNode end) {
		return addEdge(edge,start,end,EdgeType.DIRECTED);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addEdge(CEdge edge, CNode start, CNode end,EdgeType type) {
		if(findEdge(start,end)==null){
			final GraphWithOperations graphCopy = GraphUtil.copyGraph(this);
			graphCopy.superAddEdge(edge, start,end,type);
			if(!isGraphAlreadyCyclic && GraphUtil.isGraphCyclic(graphCopy)){
				int n = JOptionPane.showConfirmDialog(null,"This edge will make the graph cyclic ?\n"+
						"If you allow it, then no more warning will be displayed","Cycle detected!"
						,JOptionPane.YES_NO_OPTION);
				if(n==JOptionPane.YES_OPTION){
					isGraphAlreadyCyclic = true;
					transform(Operation.EDGE_ADDED, edge,start,end,type);
					return lastOperationStatus;
				}else{
					return false;
				}
			}else{
				transform(Operation.EDGE_ADDED, edge,start,end,type);
				return lastOperationStatus;
			}
		}else{
			return false;
		}
	}

	/**
	 * Super method for add edge
	 * @param edge
	 * @param start
	 * @param end
	 * @param type
	 * @return
	 */
	protected boolean superAddEdge(CEdge edge, CNode start, CNode end, EdgeType type){
		return super.addEdge(edge, start, end, type);
	}

	public boolean superAddEdge(CEdge edge, CNode start, CNode end){
		return superAddEdge(edge, start, end,EdgeType.DIRECTED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeEdge(CEdge edge) {
		if(containsEdge(edge)){
			transform(Operation.EDGE_REMOVED, edge, this.getSource(edge),this.getDest(edge));
			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public boolean updateNode(final CNode nodeBefore,final CNode nodeAfter){
		if(containsVertex(nodeBefore)){
			transform(Operation.NODE_DATA_UPDATED,nodeBefore,nodeBefore.getName(),nodeAfter.getName(),nodeBefore.getShape(),nodeAfter.getShape(),nodeBefore.getColor(),nodeAfter.getColor(),nodeBefore.getTags(),nodeAfter.getTags());
			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @param edgeBefore
	 * @param edgeAfter
	 * @return
	 */
	public boolean updateEdge(final CEdge edgeBefore,final CEdge edgeAfter){
		if(containsEdge(edgeBefore)){

			transform(Operation.EDGE_DATA_UPDATED,edgeBefore,
					edgeBefore.getName(),edgeAfter.getName(),
					edgeBefore.getExpression(),edgeAfter.getExpression()
					,edgeBefore.getTags(),edgeAfter.getTags());

			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * We know both position (old,new)
	 * @param n1
	 * @param oldPos
	 * @param newPos
	 * @return
	 */
	public boolean moveNodeTo(CNode n1, Point2D oldPos,Point2D newPos){
		if(containsVertex(n1)){
			transform(Operation.NODE_MOVED,n1,oldPos.getX(),newPos.getX(),oldPos.getY(),newPos.getY());
			return lastOperationStatus;
		}else{
			return false;
		}
	}

	/**
	 * 
	 * @param enabled
	 */
	public void setChangeEnabled(boolean enabled){
		this.disableChange = !enabled;
	}

	/**
	 * 
	 */
	public void startGroupOperation(){
		this.isGroupOperation = true;
	}

	/**
	 * 
	 */
	public void startHistoryRevertOperation(){
		this.isHistoryRevertOperation = true;
	}

	/**
	 * 
	 */
	public void stopGroupOperation(){
		groupCount++;
		this.isGroupOperation = false;
	}

	/**
	 * 
	 */
	public void stopHistoryRevertOperation(){
		inverseGroupCount--;
		this.isHistoryRevertOperation = false;
	}


	/**
	 * @return the operations
	 */
	public List<GraphOperation> getOperations() {
		return operations;
	}

	/**
	 * @return the operationIndex
	 */
	public int getOperationIndex() {
		return operationIndex;
	}


	/**
	 * @param operationIndex the operationIndex to set
	 */
	public void setOperationIndex(int operationIndex) {
		this.operationIndex = operationIndex;
	}


	/**
	 * @param layout the layout to set
	 */
	public void changeLayout(final Map<CNode,CPoint> oldLayout, final Map<CNode,CPoint>  newLayout) {
		transform(Operation.LAYOUT_CHANGED,oldLayout,newLayout);
	}

	/**
	 * 
	 * @param change
	 */
	public void addStructureChangeListener(Listener<Operation> change) {
		this.structureChange = change;
	}

	/**
	 * 
	 * @param change
	 */
	public void addNodeMovedListener(Listener<CNodePosition> position) {
		this.nodeMoved = position;
	}

	/**
	 * 
	 * @param change
	 */
	public void addLayoutChangedListener(Listener<CLayoutTransition> layout) {
		this.layoutChanged = layout;
	}


	/**
	 * 
	 * @param node
	 * @param tmp
	 */
	public void updateNodeAddedOperation(CNode node, CPoint tmp) {
		final GraphOperation operation = nodeAddedMap.get(node);
		if(operation==null){
			throw new IllegalStateException("Error while searching for already added node: "  + node);
		}
		if(operation.getParameters().size()==3){
			operation.getParameters().set(1, tmp.getX());
			operation.getParameters().set(2, tmp.getY());
		}else{
			operation.getParameters().add(tmp.getX());
			operation.getParameters().add(tmp.getY());
		}
	}


	/**
	 * @param workingUser the workingUser to set
	 */
	public final void setWorkingUser(final CUser workingUser) {
		this.workingUser = workingUser;
	}


	/**
	 * @return the workingUser
	 */
	public final CUser getWorkingUser() {
		return workingUser;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{		
		return cei.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTag(final CTag tag) 
	{		
		cei.addTag(tag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTag(final CTag tag) 
	{		
		cei.removeTag(tag);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CTag> getTags() 
	{	
		return cei.getTags();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAndAddTags(final List<CTag> tags) 
	{
		cei.clearAndAddTags(tags);		
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGroupOperation(){
		return isGroupOperation;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHistoryRevertOperation(){
		return isHistoryRevertOperation;
	}


	/**
	 * 
	 * @param name
	 * @return
	 */
	public List<CNode> findNodeByName(String name){
		final List<CNode> res = new ArrayList<CNode>();
		for(CNode node : getVertices()){
			if(node.getName().startsWith(name)) res.add(node);
		}
		return res;
	}

	/**
	 * 
	 * @return
	 */
	public List<CNode> getLeaves(){
		List<CNode> leaves = new ArrayList<CNode>();
		for(CNode node : getVertices()){
			if(getSuccessorCount(node)==0) leaves.add(node);
		}
		return leaves;
	}

	/**
	 * 
	 * @return
	 */
	public String nextUntitledVertexLabel(){
		return "Untitled("+(++untitledVertexCount)+")";
	}

	/**
	 * 
	 * @return
	 */
	public String nextUntitledEdgeLabel(){
		return "Untitled("+(++untitledEdgeCount)+")";
	}

	/**
	 * 
	 */
	public void resetUntitledVertexCount(){
		this.untitledVertexCount = 0;
	}


	/**
	 * 
	 */
	public void resetUntitledEdgeCount(){
		this.untitledEdgeCount = 0;
	}



	/**
	 * @return the untitledVertexCount
	 */
	public int getUntitledVertexCount() {
		return untitledVertexCount;
	}


	/**
	 * @return the untitledEdgeCount
	 */
	public int getUntitledEdgeCount() {
		return untitledEdgeCount;
	}


	/**
	 * @param untitledVertexCount the untitledVertexCount to set
	 */
	public void setUntitledVertexCount(int untitledVertexCount) {
		this.untitledVertexCount = untitledVertexCount;
	}


	/**
	 * @param untitledEdgeCount the untitledEdgeCount to set
	 */
	public void setUntitledEdgeCount(int untitledEdgeCount) {
		this.untitledEdgeCount = untitledEdgeCount;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() 
	{
		return cei.getId();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Long id) {
		this.cei.setId(id);
	}


	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return dirty;
	}


	/**
	 * @param dirty the dirty to set
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * 
	 * @return
	 */
	public Set<CVariable> getVariables() {
		return new HashSet<CVariable>(variables);
	}

	/**
	 * 
	 * @param vars
	 */
	public boolean setVariables(Set<CVariable> vars){
		final Set<CVariable> old = new HashSet<CVariable>(this.variables);
		this.variables.clear();
		for(CVariable var : vars){
			addVariable(var);
		}
		return !old.equals(vars);
	}

	/**
	 * 
	 * @param var
	 */
	public boolean addVariables(final Set<CVariable> vars){
		final Set<CVariable> old = new HashSet<CVariable>(this.variables);
		boolean operationsSuccess = true;
		for(final CVariable var : vars){
			operationsSuccess = operationsSuccess && addVariable(var);
		}
		return !old.equals(vars);
	}

	/**
	 * 
	 * @param var
	 */
	public boolean addVariable(final CVariable var){
		//Copy variables
		final List<CVariable> vars =new ArrayList<CVariable>();
		for(final CVariable v : this.variables){
			vars.add(new CVariable(v));
		}
		//---
		final int idx = vars.indexOf(var);
		if(idx != -1)
		{
			final CVariable ivar = vars.get(idx);
			if(!var.allEquals(ivar)){
				transform(Operation.VARIABLE_CHANGED, vars.get(idx), var);
			}
		}else{
			this.variables.add(var);
		}
		return lastOperationStatus;
	}

	/**
	 * 
	 * @param var
	 */
	public boolean removeVariable(CVariable var) {
		return this.variables.remove(var);
	}




}
