/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.SwingUtilities;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CVariable.CadralType;
import lu.lippmann.cdb.models.history.*;

import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.tools.ant.filters.StringInputStream;
import weka.core.*;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;


/**
 * Utility class for graph management.
 * 
 * @author the WP1 team
 */
public final class GraphUtil {

	//
	// Static fields
	//

	/** Max nodes count. */
	private static final int MAX_NODES_COUNT = 20000;

	/** */
	private static final Pattern NODE_PATTERN = Pattern.compile("\n(N[0-9]+) \\[label=\"([^\"]+)\"");
	/** */
	private static final Pattern REGRESSION_NODE_PATTERN = Pattern.compile("\n(\\w+) \\[label=\"([^\"]+)\"");
	/** */
	private static final Pattern EDGE_PATTERN = Pattern.compile("(\\w+)\\->(\\w+) \\[label=\"(.+)\".*\\]");
	/** */
	private static final Pattern CONDITION_PATTERN = Pattern.compile("\\s+(.+)");
	/** */
	private static final Pattern CLEAN_VARIABLES_PATTERN = Pattern.compile("'([^']+)'");

	//
	// Inner enums
	//

	/** operator used in expression inside the graph **/
	public static enum Operator {
		TERNARY("?"), NE("!="), AND("&&"), OR("||"), GE(">="), LE("<="), GT(">"), LT(
				"<"), EQ("="), ADD("+"), SUB("-"), DIV("/"), REMAINDER("%"), MUL(
						"*"), NEG("-"), ABS(" abs "), POW(" pow "), INT("int ");

		Operator(String label) {
			this.label = label;
		}

		String getLabel() {
			return label;
		}

		final String label;
	};

	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private GraphUtil() {
		throw new IllegalStateException();
	}

	//
	// Static methods
	//

	public static String[] usedVariablesNamesInGraph(final CGraph graph) {
		final Set<CVariable> variables = getUsedVariablesInGraph(graph.getInternalGraph());
		final String[] variablesNames = new String[variables.size()];
		int i = 0;
		for (CVariable v : variables)
			variablesNames[i++] = v.getKey();
		return variablesNames;
	}



	/**
	 * Import decision tree in editor !
	 */
	public static DecisionTree importDecisionTreeInEditor(final DecisionTreeFactory dtFactory,final Instances dataSet,final ApplicationContext applicationContext,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher){		
		try
		{
			final DecisionTree dt=dtFactory.buildDecisionTree(dataSet);
			return importDecisionTreeInEditor(dt,dataSet, applicationContext,eventPublisher,commandDispatcher);
		}
		catch (Exception e1) 
		{
			eventPublisher.publish(new ErrorOccuredEvent("Error when trying to open model in editor!",e1));
		}
		return null;
	}

	/**
	 * Import decision tree in editor !
	 */
	public static DecisionTree importDecisionTreeInEditor(final DecisionTree dt,final Instances dataSet,final ApplicationContext applicationContext,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher){

		final CGraph graph=buildNewCGraphWithFRLayout(dataSet,dt.getGraphWithOperations());
		
		((GraphWithOperations) graph.getInternalGraph()).setWorkingUser(applicationContext.getUser());

		applicationContext.setCadralGraph(graph);
		System.out.println("Publishing graph : " + applicationContext.getCadralGraph());

		SwingUtilities.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				eventPublisher.publish(new GraphReloadedEvent(false));	
				commandDispatcher.dispatch(new EnableAllMenusCommand());

				commandDispatcher.dispatch(new SelectAllCommand());
				commandDispatcher.dispatch(new ClusterGraphCommand());
				commandDispatcher.dispatch(new DeselectAllCommand());

				commandDispatcher.dispatch(new AutoFitCommand());

				commandDispatcher.dispatch(new ClickViewModeCommand(ViewMode.Edit));
			}
		});

		return dt;
	}

	/**
	 * 
	 * @param dataSet
	 * @param gr
	 * @return
	 */
	private static CGraph buildNewCGraphWithFRLayout(final Instances dataSet,final GraphWithOperations gr) 
	{
		final CGraph res = buildNewCGraphWithFRLayout(gr);
		if (dataSet!=null)
		{
			updateVariables(dataSet,gr);
		} 
		else throw new IllegalStateException();
		return res;
	}

	public static void updateVariables(final Instances dataSet,final GraphWithOperations gr)
	{
		for(final CVariable var : gr.getVariables())
		{
			final Attribute attribute = dataSet.attribute(var.getKey());
			if (attribute==null) throw new IllegalStateException("Attribute '"+var.getKey()+"' not found in dataset!?");
			if(attribute.isNominal()){
				var.setType(CadralType.ENUMERATION);
				final List<String> values = new ArrayList<String>();
				final Enumeration<?> eval = attribute.enumerateValues();
				while(eval.hasMoreElements())
				{
					values.add((String)eval.nextElement());
				}
				var.setValues(values);
			}else if(attribute.isNumeric()){
				var.setType(CadralType.NUMERIC);
			}else{
				var.setType(CadralType.UNKNOWN);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static Set<CVariable> getUsedVariablesInGraph(final Graph<CNode,CEdge> graph){
		if(graph instanceof GraphWithOperations){
			return ((GraphWithOperations)graph).getVariables();
		}else{
			throw new IllegalStateException();
		}
	}

	/**
	 * Method to remove undefined variables.
	 */
	public static String cleanVariables(final String expression) {
		String res = expression;
		Matcher m;
		while ((m = CLEAN_VARIABLES_PATTERN.matcher(res)).find()) {
			final String var = Pattern.quote(m.group(0));
			res = res.replaceAll(var, "" + m.group(1).hashCode());
		}
		return res;
	}

	/**
	 * 
	 * @param g
	 * @return
	 */
	public static String saveGraphWithOperation(final GraphWithOperations g) {
		final ArrayList<GraphOperation> operations = new ArrayList<GraphOperation>(
				g.getOperations());
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.setExceptionListener(new ExceptionListener() {
			public void exceptionThrown(final Exception e) {
				throw new IllegalStateException(e);
			}
		});

		xmlEncoder.writeObject(new GraphTO(g.getId(), operations, g
				.getUntitledVertexCount(), g.getUntitledEdgeCount(), g
				.getVariables()));
		xmlEncoder.close();
		return baos.toString();
	}

	/**
	 * 
	 * @param xmlString
	 * @return
	 */
	public static Layout<CNode, CEdge> getLayoutFromXML(final String xmlString) {
		final GraphWithOperations graph = new GraphWithOperations();
		final XMLDecoder in = new XMLDecoder(new BufferedInputStream(
				new StringInputStream(xmlString)));
		final GraphTO gto = (GraphTO) in.readObject();
		applyOperationsToGraph(graph,
				new ArrayList<GraphOperation>(gto.getOperations()));
		graph.setUntitledEdgeCount(gto.getUntitledEdgeCount());
		graph.setUntitledVertexCount(gto.getUntitledVertexCount());
		graph.setVariables(gto.getVariables());
		return getLayoutFromGraphWithOperations(graph);
	}

	/**
	 * 
	 * @param graph
	 * @param operations
	 */
	public static void applyOperationsToGraph(final GraphWithOperations graph,
			final List<GraphOperation> operations) {
		Long lastIdGroup = null;
		for (final GraphOperation o : operations) {
			createGroupFlags(graph, lastIdGroup, o);
			graph.transform(true, false, o.getUser(), o.getOperation(), o
					.getParameters().toArray());
			lastIdGroup = o.getIdGroup();
		}
	}


	/**
	 * 
	 * @param graph
	 * @param lastIdGroup
	 * @param o
	 */
	public static void createGroupFlags(final GraphWithOperations graph,
			Long lastIdGroup, GraphOperation o) {
		if (o.getIdGroup() != null) {
			if (lastIdGroup == null) {
				if (o.getIdGroup() >= 0) {
					graph.startGroupOperation();
				} else {
					graph.startHistoryRevertOperation();
				}
			} else if (!lastIdGroup.equals(o.getIdGroup())) {
				if (lastIdGroup >= 0) {
					graph.stopGroupOperation();
				} else {
					graph.stopHistoryRevertOperation();
				}
				if (o.getIdGroup() >= 0) {
					graph.startGroupOperation();
				} else {
					graph.startHistoryRevertOperation();
				}
			}
		} else {
			if (lastIdGroup != null && lastIdGroup >= 0) {
				graph.stopGroupOperation();
			} else {
				graph.stopHistoryRevertOperation();
			}
		}
	}

	/**
	 * 
	 * @param graph
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Layout<CNode, CEdge> getLayoutFromGraphWithOperations(
			final GraphWithOperations graph) {
		final Layout<CNode, CEdge> layout = new StaticLayout<CNode, CEdge>(
				graph);
		for (GraphOperation o : graph.getOperations()) {
			if (o.getOperation().equals(Operation.NODE_ADDED)) {
				final CNode node = (CNode) o.getParameters().get(0);
				if (o.getParameters().size() != 3) {
					throw new IllegalStateException("Missing node position !");
				}
				final Point2D point = new Point2D.Double((Double) o
						.getParameters().get(1), (Double) o.getParameters()
						.get(2));
				layout.setLocation(node, point);
			} else if (o.getOperation().equals(Operation.NODE_MOVED)) {
				final CNode node = (CNode) o.getParameters().get(0);
				if (o.getParameters().size() != 5) {
					throw new IllegalStateException("Missing node position !");
				}
				final Point2D point = new Point2D.Double((Double) o
						.getParameters().get(3), (Double) o.getParameters()
						.get(4));
				layout.setLocation(node, point);
			} else if (o.getOperation().equals(Operation.LAYOUT_CHANGED)) {
				final Map<CNode, CPoint> newPos = (Map<CNode, CPoint>) o
						.getParameters().get(1);
				for (CNode node : newPos.keySet()) {
					final CPoint tmp = newPos.get(node);
					layout.setLocation(node,
							new Point2D.Double(tmp.getX(), tmp.getY()));
				}
			}
		}
		return layout;
	}

	/**
	 * FIXME : 0,0 for counters and no variables
	 * 
	 * @param g1
	 * @param g2
	 * @return
	 */
	public static List<GraphOperation> diff(final CUser user,
			final Graph<CNode, CEdge> g1, final Graph<CNode, CEdge> g2) {
		final List<GraphOperation> res = new ArrayList<GraphOperation>();
		final Collection<CNode> nodes1 = g1.getVertices();
		final Collection<CNode> toBeRemoved = new ArrayList<CNode>(nodes1);
		final Collection<CNode> nodes2 = g2.getVertices();
		final Collection<CNode> toBeAdded = new ArrayList<CNode>(nodes2);
		final Collection<CEdge> edges1 = g1.getEdges();
		final Collection<CEdge> edgesToBeRemoved = new ArrayList<CEdge>(
				g1.getEdges());
		final Collection<CEdge> edges2 = g2.getEdges();
		final Collection<CEdge> edgesToBeAdded = new ArrayList<CEdge>(
				g2.getEdges());

		toBeRemoved.removeAll(nodes2);
		toBeAdded.removeAll(nodes1);

		/** removing useless nodes **/
		for (CNode node : toBeRemoved) {
			res.add(new GraphOperation(0, 0, user, Operation.NODE_REMOVED,
					new ArrayList<Object>(Arrays.asList(node))));
		}
		/** adding new nodes **/
		for (CNode node : toBeAdded) {
			res.add(new GraphOperation(0, 0, user, Operation.NODE_ADDED,
					new ArrayList<Object>(Arrays.asList(node))));
		}

		/**
		 * removing edges in G1 that are in G2 (with the same id/name/condition)
		 **/
		for (CEdge edge : edges2) {
			final CEdge foundEdge = g1.findEdge(g2.getSource(edge),
					g2.getDest(edge));
			if (foundEdge != null) {
				edgesToBeRemoved.remove(foundEdge);
				if (!edge.containsSameFieldsThat(foundEdge)) {
					res.add(new GraphOperation(0, 0, user,
							Operation.EDGE_DATA_UPDATED,
							new ArrayList<Object>(Arrays.asList(foundEdge,
									foundEdge.getName(), edge.getName(),
									foundEdge.getExpression(),
									edge.getExpression(), foundEdge.getTags(),
									edge.getTags()))));
				}
			}
		}

		/**
		 * removing edges in G2 that are in G1 (with the same id/name/condition)
		 **/
		for (CEdge edge : edges1) {
			final CEdge foundEdge = g2.findEdge(g1.getSource(edge),
					g1.getDest(edge));
			if (foundEdge != null) {
				edgesToBeAdded.remove(foundEdge);
			}
		}

		/** removing nodes edges **/
		for (CEdge edge : edgesToBeRemoved) {
			res.add(new GraphOperation(0, 0, user, Operation.EDGE_REMOVED,
					new ArrayList<Object>(Arrays.asList(edge,
							g2.getSource(edge), g2.getDest(edge)))));
		}

		/** adding usefull edges **/
		for (CEdge edge : edgesToBeAdded) {
			res.add(new GraphOperation(0, 0, user, Operation.EDGE_ADDED,
					new ArrayList<Object>(Arrays.asList(edge,
							g2.getSource(edge), g2.getDest(edge)))));
		}
		return res;
	}

	/**
	 * 
	 * @param xmlString
	 * @return
	 */
	public static CGraph getCGraphFrom(final String xmlString) {
		return buildNewCGraphFromLayout(getLayoutFromXML(xmlString));
	}

	/**
	 * @return
	 */
	public static CGraph buildNewLocalCGraph() {
		return buildNewCGraphWithFRLayout(new GraphWithOperations());

	}
	/**
	 * @return
	 */
	public static CGraph buildNewCGraphWithFRLayout(final Graph<CNode, CEdge> gr) {
		return buildNewCGraphFromLayout(new FRLayout<CNode, CEdge>(gr));
	}
	
	public static <V,E> GenericCGraph<V,E> buildNewCGenericGraphWithFRLayout(final Graph<V,E> gr) {
		return buildNewCGraphFromGenericLayout(new FRLayout<V,E>(gr));
	}
	

	private static <V,E> GenericCGraph<V,E> buildNewCGraphFromGenericLayout(final Layout<V,E> layout) {
		final GenericCGraph<V,E> cgraph = new GenericCGraph<V,E>();
		cgraph.setInternalLayout(layout);
		return cgraph;
	}
	
	/**
	 * 
	 * @param layout
	 * @return
	 */
	private static CGraph buildNewCGraphFromLayout(final Layout<CNode, CEdge> layout) {
		final CGraph cgraph = new CGraph();
		cgraph.setInternalLayout(layout);
		return cgraph;
	}





	/**
	 * 
	 */
	public static CGraph copyCGraph(final CGraph cgr) {
		final CGraph cgraph = buildNewLocalCGraph();
		final List<GraphOperation> l = diff(CUser.ANONYMOUS,
				cgraph.getInternalGraph(), cgr.getInternalGraph());
		applyOperationsToGraph((GraphWithOperations) cgraph.getInternalGraph(),
				l);
		return cgraph;
	}

	/**
	 * 
	 */
	public static GraphWithOperations copyGraph(final GraphWithOperations graph) {
		final GraphWithOperations newGraph = new GraphWithOperations();
		final List<GraphOperation> l = graph.getOperations();
		applyOperationsToGraph(newGraph, l);
		return newGraph;
	}



	/***
	 * 
	 * @param gwo
	 * @param filterFinalState
	 * @return
	 * @throws Exception
	 */
	public static GraphWithOperations filterGraphWithFinalState(
			GraphWithOperations gwo, CNode filterFinalState) throws Exception {
		final GraphWithOperations res = new GraphWithOperations();
		filterGraphWithFinalState(gwo, res, filterFinalState);
		return res;
	}

	/**
	 * 
	 * @param gwo
	 * @param filterFinalStates
	 * @return
	 * @throws Exception
	 */
	public static List<GraphWithOperations> filterGraphWithFinalStates(
			GraphWithOperations gwo, List<CNode> filterFinalStates)
					throws Exception {
		final List<GraphWithOperations> list = new ArrayList<GraphWithOperations>();

		for (CNode filterFinalState : filterFinalStates) {
			list.add(filterGraphWithFinalState(gwo, new GraphWithOperations(),
					filterFinalState));
		}

		return list;
	}

	/**
	 * 
	 * @param gwo
	 * @param res
	 * @param node
	 * @return
	 * @throws Exception
	 */
	private static GraphWithOperations filterGraphWithFinalState(
			GraphWithOperations gwo, GraphWithOperations res, CNode node)
					throws Exception {
		// System.out.println("Entering with node : " + node);
		Collection<CNode> predecedors = gwo.getPredecessors(node);

		if (predecedors == null)
			return gwo;

		for (CNode predecedor : predecedors) {
			res.addVertex(predecedor);
			CEdge edge = gwo.findEdge(predecedor, node);
			res.addEdge(edge, predecedor, node);
			filterGraphWithFinalState(gwo, res, predecedor);
		}
		return res;
	}



	/**
	 * 
	 */
	public static GraphWithOperations buildGraphWithOperationsFromWekaStringLight(
			final String graphStr) throws Exception {
		final Matcher nodeMatch = NODE_PATTERN.matcher(graphStr);
		final Matcher edgeMatch = EDGE_PATTERN.matcher(graphStr);

		final GraphWithOperations gwo = new GraphWithOperations();

		final Map<String, CNode> map = new HashMap<String, CNode>();

		while (nodeMatch.find()) {
			final String id = nodeMatch.group(1);
			final String variableName = nodeMatch.group(2);
			final int pointCharIdx = variableName.indexOf('(');
			final CNode node = new CNode((long) id.hashCode(),
					pointCharIdx > 0 ? variableName.substring(0, pointCharIdx)
							: variableName);
			map.put(id, node);
			gwo.addVertex(node);
		}

		while (edgeMatch.find()) {
			final String from = edgeMatch.group(1);
			final String to = edgeMatch.group(2);

			final String condition = edgeMatch.group(3);

			final String fromNodeName = map.get(from).getName();

			gwo.superAddEdge(
					new CEdge(from + " -> " + to, fromNodeName + condition),
					map.get(from), map.get(to));
		}

		return gwo;
	}

	/**
	 * 
	 */
	public static GraphWithOperations buildGraphWithOperationsFromWekaRegressionString(final String graphStr) throws Exception 
	{
		final Matcher nodeMatch = REGRESSION_NODE_PATTERN.matcher(graphStr);
		final Matcher edgeMatch = EDGE_PATTERN.matcher(graphStr);

		final GraphWithOperations gwo = new GraphWithOperations();

		final Map<String, CNode> map = new HashMap<String, CNode>();

		while (nodeMatch.find()) 
		{
			final String id = nodeMatch.group(1);
			final String variableName = nodeMatch.group(2);
			final int pointCharIdx = variableName.indexOf('(');
			final CNode node = new CNode((long) id.hashCode(),pointCharIdx > 0 ? variableName.substring(0, pointCharIdx): variableName);
			map.put(id, node);
			gwo.addVertex(node);
		}

		while (edgeMatch.find()) 
		{
			final String from = edgeMatch.group(1);
			final String to = edgeMatch.group(2);

			final String condition = edgeMatch.group(3);

			final String fromNodeName = map.get(from).getName();

			gwo.superAddEdge(new CEdge(from + " -> " + to, fromNodeName + condition),map.get(from), map.get(to));
		}

		for (final CNode n:gwo.getVertices())
		{
			n.setName(n.getName().substring(n.getName().indexOf(':')+1));
		}
		for (final CEdge e:gwo.getEdges())
		{
			e.setExpression(e.getExpression().substring(e.getExpression().indexOf(':')+1));
		}
		
		return gwo;
	}
	
	public static int computeMinPathLength(final Graph<CNode, CEdge> graph) {
		final CNode startNode = GraphUtil.getFirstRoot(graph);
		return computeMinPathLength(graph, startNode, 1);
	}

	public static int computeMinPathLength(final Graph<CNode, CEdge> graph,
			final CNode node, final int currentDc) {
		final Collection<CEdge> ces = graph.getOutEdges(node);
		int res = 0;
		if (ces.size() == 0)
			res = currentDc;
		else {
			int min = Integer.MAX_VALUE;
			for (CEdge ce : ces) {
				final CNode nextNode = graph.getEndpoints(ce).getSecond();
				final int dc = computeMinPathLength(graph, nextNode,
						currentDc + 1);
				if (dc < min)
					min = dc;
			}
			res = min;
		}
		return res;
	}

	public static int computeMaxPathLength(final Graph<CNode, CEdge> graph) 
	{
		if (graph.getVertexCount()==0) return 0;
		final CNode startNode = GraphUtil.getFirstRoot(graph);
		return computeMaxPathLength(graph, startNode, 1);
	}

	public static int computeMaxPathLength(final Graph<CNode, CEdge> graph,
			final CNode node, final int currentDc) 
	{
		final Collection<CEdge> ces = graph.getOutEdges(node);
		int res = 0;
		if (ces.size() == 0)
			res = currentDc;
		else {
			int max = 0;
			for (CEdge ce : ces) {
				final CNode nextNode = graph.getEndpoints(ce).getSecond();
				final int dc = computeMaxPathLength(graph, nextNode,
						currentDc + 1);
				if (dc > max)
					max = dc;
			}
			res = max;
		}
		return res;
	}

	/**
	 * Get the (first) root of a given graph.
	 */
	public static CNode getFirstRoot(final Graph<CNode, CEdge> graph) {
		if (graph == null)
			throw new IllegalArgumentException("graph null!");
		CNode res = null;
		for (final CNode n : graph.getVertices()) {
			if (graph.getInEdges(n).isEmpty()) {
				res = n;
				break;
			}
		}
		return res;
	}

	/**
	 * Get the roots of a given graph.
	 */
	public static List<CNode> getRoots(final Graph<CNode, CEdge> graph) {
		final List<CNode> res = new ArrayList<CNode>();
		if (graph == null)
			throw new IllegalArgumentException("graph null!");
		for (final CNode n : graph.getVertices()) {
			if (graph.getInEdges(n).isEmpty()) {
				res.add(n);
			}
		}
		return res;
	}

	private static String getRulesFromGraph(final Graph<CNode, CEdge> graph,
			final CNode startNode) {
		final StringBuilder res = new StringBuilder();
		final List<List<CEdge>> paths = new ArrayList<List<CEdge>>();
		final CNode srcNode = getFirstRoot(graph);
		findNodes(srcNode, graph, paths, new ArrayList<CEdge>());
		final int pathsSize = paths.size();
		int i = 0;
		for (final List<CEdge> edgePath : paths) {
			final int pathElementSize = edgePath.size();
			int j = 0;
			res.append("(");
			for (final CEdge pathElement : edgePath) {
				res.append(pathElement.getExpression());
				if (j < pathElementSize - 1)
					res.append(" ").append(Operator.AND.getLabel()).append(" ");
				j++;
			}
			if (i < pathsSize - 1)
				res.append(")\n ").append(Operator.OR.getLabel())
				.append(" \n(");
			else
				res.append(")\n");
			i++;
		}
		return res.toString();
	}

	/**
	 * 
	 * @param srcNode
	 * @param graph
	 * @param result
	 * @param path
	 */
	private static void findNodes(final CNode srcNode,
			Graph<CNode, CEdge> graph, List<List<CEdge>> result,
			List<CEdge> path) {
		if (graph.getOutEdges(srcNode).isEmpty()) {
			Collections.sort(path, new Comparator<CEdge>() {
				@Override
				public int compare(CEdge o1, CEdge o2) {
					return o1.getExpression().compareTo(o2.getExpression());
				}
			}); // alphabetic order for expression
			result.add(path);
		} else {
			for (final CEdge edge : graph.getOutEdges(srcNode)) {
				path.add(edge);
				findNodes(graph.getDest(edge), graph, result, path);
			}
		}

	}

	/**
	 * 
	 * @param graph
	 * @return
	 */
	public static String getRulesFromGraph(final Graph<CNode, CEdge> graph) {
		return getRulesFromGraph(graph, GraphUtil.getFirstRoot(graph));
	}

	/**
	 * 
	 * @param graph
	 * @return
	 */
	public static final List<String> getFinalStates(final Graph<CNode, CEdge> graph) 
	{
		final Set<String> s = new HashSet<String>();
		for (final CNode n : graph.getVertices()) 
		{
			if (graph.getSuccessorCount(n) == 0) 
			{
				final int idx=n.getName().indexOf('(');
				if (idx>=0) s.add(n.getName().substring(0, idx).trim());
				else s.add(n.getName().trim());
			}
		}
		return new ArrayList<String>(s);
	}
	
	public static final List<String> getFinalStates(final Graph<CNode, CEdge> graph,final CNode root) 
	{
		final Set<String> s = new HashSet<String>();
		for (final CNode n : graph.getSuccessors(root)) 
		{
			if (graph.getSuccessorCount(n) == 0) 
			{
				final int idx=n.getName().indexOf('(');
				if (idx>=0) s.add(n.getName().substring(0, idx).trim());
				else s.add(n.getName().trim());
			}
		}
		return new ArrayList<String>(s);
	}
	
	/**
	 * 
	 * @param graph
	 * @return
	 */
	public static boolean isGraphCyclic(final Graph<CNode, CEdge> graph) {
		boolean cycle = false;
		ArrayList<CNode> alreadyVisited = new ArrayList<CNode>();
		ArrayList<CNode> criticalList = new ArrayList<CNode>();
		Iterator<CNode> e = graph.getVertices().iterator();
		CNode tmpNode = null;
		while (e.hasNext() && !cycle) {
			tmpNode = e.next();
			if (!alreadyVisited.contains(tmpNode)) {
				cycle = visit(graph, tmpNode, criticalList);
			}
			alreadyVisited.add(tmpNode);
		}
		return cycle;
	}

	/**
	 * 
	 * @param id
	 * @param critical
	 * @return
	 */
	private static boolean visit(final Graph<CNode, CEdge> graph,
			CNode tmpNode, List<CNode> critical) {
		boolean cycle = false;
		if (critical.contains(tmpNode)) {
			return true;
		} else {
			final List<CNode> succ = new ArrayList<CNode>(
					graph.getSuccessors(tmpNode));
			if (succ != null) {
				critical.add(tmpNode);
				for (int i = 0; i < succ.size() && !cycle; i++) {
					cycle = visit(graph, succ.get(i), critical);
				}
				critical.remove(tmpNode);
			}
		}
		return cycle;
	}

	/**
	 * 
	 * @param layout
	 * @return
	 */
	public static Point2D getCenter(Set<CNode> picked,
			Layout<CNode, CEdge> layout) {
		double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

		for (final CNode n : picked) {
			final Point2D point = layout.transform(n); // center of the node
			if (point.getX() < minX) {
				minX = point.getX();
			}
			if (point.getX() > maxX) {
				maxX = point.getX();
			}
			if (point.getY() < minY) {
				minY = point.getY();
			}
			if (point.getY() > maxY) {
				maxY = point.getY();
			}
		}

		final int graphWidth = (int) (maxX - minX);
		final int graphHeigth = (int) (maxY - minY);

		return new Point2D.Double(minX + graphWidth / 2.0f, minY + graphHeigth
				/ 2.0f);
	}

	/**
	 * Get the sub-graph that includes node
	 * 
	 * @param node
	 * @return
	 */
	public static Graph<CNode, CEdge> getSubGraph(CNode node,
			Graph<CNode, CEdge> graph) {
		final GraphConnexity<CNode,CEdge> cn = new GraphConnexity<CNode,CEdge>(graph);
		return cn.getSubGraph(node);
	}

	/**
	 * return if background of a node is dark or not
	 * 
	 * @param v
	 * @return
	 */
	public static boolean isDarkNode(final CNode v) {
		final Color c = v.getColor();
		return ((c.getBlue() + c.getGreen() + c.getRed()) / 3 < 128);
	}



	/**
	 * 
	 * @param layout
	 * @param picked
	 */
	public static Layout<CNode,CEdge> reorganize(Layout<CNode,CEdge> layout,Set<CNode> picked) 
	{

		Layout<CNode, CEdge> subLayout = null;
		
		//create map that copy the transformer of existing layout
		final HashMap<CNode,CPoint> mapTransform1 = new LinkedHashMap<CNode, CPoint>();
		for(CNode v : layout.getGraph().getVertices()){
			final Point2D tmp = layout.transform(v);
			mapTransform1.put(v,new CPoint(tmp.getX(),tmp.getY()));
		}
		//

		final Graph<CNode, CEdge> graph = layout.getGraph();

		final AggregateLayout<CNode,CEdge> clusteringLayout = new AggregateLayout<CNode,CEdge>(layout);

			// put the picked vertices into a new sublayout

			//final Set<CNode> picked = vv.getPickedVertexState().getPicked();
			if (picked !=null && picked.size() >= 1) {

				final Point2D initCenter = GraphUtil.getCenter(picked,layout);

				final Graph<CNode, CEdge> subGraph = new DirectedSparseGraph<CNode, CEdge>();
				try {
					for (CNode vertex : picked) {
						subGraph.addVertex(vertex);
						final Collection<CEdge> incidentEdges = graph.getIncidentEdges(vertex);
						if(incidentEdges != null){
							for (final CEdge edge : incidentEdges) {
								final Pair<CNode> endpoints = graph.getEndpoints(edge);
								if (picked.containsAll(endpoints)) {
									subGraph.addEdge(edge, endpoints.getFirst(),endpoints.getSecond());
								}
							}
						}
					}

					subLayout = buildMinimumSpanningForestLayout(subGraph);

					subLayout.setInitializer(layout);

					if(!(subLayout instanceof TreeLayout)){
						subLayout.setSize(clusteringLayout.getSize());
					}


					final Point2D subGraphCenter   = GraphUtil.getCenter(picked, subLayout);
					final Point2D subLayoutCenter  = new Point2D.Double(subLayout.getSize().getWidth()/2,subLayout.getSize().getHeight()/2);
					//System.out.println("Initial init center : " + initCenter);
					initCenter.setLocation(initCenter.getX()+(subLayoutCenter.getX()-subGraphCenter.getX()), 
							initCenter.getY()+(subLayoutCenter.getY()-subGraphCenter.getY()));
					//System.out.println("Corrected init center : " + initCenter);

					clusteringLayout.put(subLayout, initCenter);

					//create map that copy the transformer of new layout
					final HashMap<CNode,CPoint> mapTransform2 = new LinkedHashMap<CNode, CPoint>();
					for(CNode v : layout.getGraph().getVertices()){
						final Point2D tmp = clusteringLayout.transform(v);
						mapTransform2.put(v,new CPoint(tmp.getX(),tmp.getY()));
					}
					//
					/** save the new layout and historize it !! */
					((GraphWithOperations)layout.getGraph()).changeLayout(mapTransform1,mapTransform2);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return subLayout;
	}


	/**
	 * Build Minimum Spanning Forest layout
	 * @param graph
	 * @return
	 */
	public static Layout<CNode, CEdge> buildMinimumSpanningForestLayout(final Graph<CNode, CEdge> graph) {

		final Graph<CNode,CEdge> copyGraph = new DirectedSparseGraph<CNode, CEdge>();
		for(final CNode node : graph.getVertices()){
			copyGraph.addVertex(node);
		}
		for(final CEdge edge : graph.getEdges()){
			copyGraph.addEdge(edge,graph.getSource(edge),graph.getDest(edge));
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Layout<CNode, CEdge> subLayout = new TreeLayout<CNode,CEdge>(
				new MinimumSpanningForest2<CNode, CEdge>(
						copyGraph, new DelegateForest<CNode, CEdge>(),
						DelegateTree.<CNode, CEdge> getFactory(),
						new ConstantTransformer(1.0)).getForest(),250,250);
		return subLayout;
	}

	/**
	 * 
	 * @param gwo
	 * @return
	 */
	public static CGraph buildNewCGraphReorganizedLayout(GraphWithOperations gwo) {
		final CGraph cgraph = new CGraph();
		final Layout<CNode,CEdge> def = new StaticLayout<CNode,CEdge>(gwo);
		final Layout<CNode,CEdge> layout = GraphUtil.reorganize(def,new HashSet<CNode>(gwo.getVertices()));
		for(CNode node : gwo.getVertices()){
			def.setLocation(node,layout.transform(node));
		}
		cgraph.setInternalLayout(def);
		return cgraph;
	}

	public static final CNode findNodeWithNameStartingWith(final GraphWithOperations gwo,final String name)
	{
		for (CNode cn:gwo.getVertices())
		{
			if (cn.getName().startsWith(name)) return cn;
		}
		return null;
	}
	
	public static final int computeDistWithRoot(final GraphWithOperations gwo,final CNode cn)
	{
		final CNode root=GraphUtil.getFirstRoot(gwo);
		
		CNode visitor=cn;		
		int i=0;
		while (!visitor.equals(root))
		{
			final Collection<CEdge> edges=gwo.getInEdges(visitor);
			if (edges.size()!=1) throw new IllegalStateException();
			final CEdge edge=edges.iterator().next();
			visitor=gwo.getSource(edge);
			i++;	
		}		
		return i;
	}
	
	public static final CNode findAncestorAtLevel(final GraphWithOperations gwo,final CNode cn,final int level)
	{
		CNode visitor=cn;
		while (computeDistWithRoot(gwo,visitor)>level)
		{
			final Collection<CEdge> edges=gwo.getInEdges(visitor);
			if (edges.size()!=1) throw new IllegalStateException();
			final CEdge edge=edges.iterator().next();
			visitor=gwo.getSource(edge);		
		}						
		return visitor;
	}
	
	public static List<CNode> getNodesAtLevel(final GraphWithOperations gwo,final int level)
	{
		final Collection<CNode> nodes=gwo.getVertices();		
		final List<CNode> l=new ArrayList<CNode>();
		for (final CNode cn:nodes)
		{
			if (GraphUtil.computeDistWithRoot(gwo,cn)==level) l.add(cn);
		}
		return l;
	}
	
	public static boolean isNodeSimplifiable(final Graph<CNode,CEdge> graph,final CNode node)
	{
		boolean isBeforeFinalStates=(graph.getSuccessorCount(node)>0);
		for (final CNode next:graph.getSuccessors(node))
		{
			isBeforeFinalStates&=(graph.getSuccessorCount(next)==0);
		}
		if (!isBeforeFinalStates) return false;
		
		final Set<String> s=new HashSet<String>(getFinalStates(graph,node));
		return (s.size()==1);
	}
	
	public static CNode getFirstNodeSimplifiable(final Graph<CNode,CEdge> graph)
	{
		for (final CNode n:graph.getVertices())
		{
			if (isNodeSimplifiable(graph,n)) return n;
		}
		return null;
	}
	
	public static void simplify(final Graph<CNode,CEdge> graph)
	{
		CNode n=null;
		while ((n=getFirstNodeSimplifiable(graph))!=null)
		{
			final Collection<CEdge> toRemove=new ArrayList<CEdge>(graph.getOutEdges(n));
			String newname=null;
			for (final CEdge ce:toRemove)
			{
				newname=graph.getDest(ce).getName();
				graph.removeVertex(graph.getDest(ce));
				graph.removeEdge(ce);
			}
			n.setName(newname);
		}
	}

}
