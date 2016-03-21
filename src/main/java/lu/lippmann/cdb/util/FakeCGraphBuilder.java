/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.util;

import java.awt.Color;
import java.awt.geom.Point2D;

import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CTag.CNote;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;


/**
 * FakeCGraphBuilder.
 *
 * @author Olivier PARISOT
 */
public final class FakeCGraphBuilder 
{
	//
	// Constructors
	//
	
	/**
	 * Private constructor to avoid instantiation.
	 */
	private FakeCGraphBuilder() {}
	
	
	//
	// Static methods
	//
	
	public static GraphWithOperations buildGraphWithOperations()
	{
		return (GraphWithOperations)buildCGraphForExample().getInternalGraph();
	}
	
	public static CGraph buildCGraphForExample() 
	{
		final GraphWithOperations graph = new GraphWithOperations();
		graph.setWorkingUser(CUser.ANONYMOUS);
		
		final CNode debut = new CNode(1l,"Debut");
		debut.setShape(CShape.RECTANGLE);
		debut.setColor(Color.RED);
		
		final CNode finalNodeOui1  = new CNode(2l,"OUI");
		final CNode node  = new CNode(3l,"node");
		node.setShape(CShape.ELLIPSE);
		node.setColor(Color.GREEN);		
		final CTag tag=new CTag();
		tag.setUser(CUser.ANONYMOUS);
		tag.setTimestamp(System.currentTimeMillis());
		tag.setValue(new CTag.CValue(CNote.GOOD,"bla bla"));
		node.addTag(tag);
		final CNode finalNodeOui2 = new CNode(4l,"OUI");
		final CNode finalNodeNon = new CNode(5l,"NON");
				
		graph.addVertex(debut);
		graph.addVertex(finalNodeOui1);
		graph.addVertex(node);
		graph.addVertex(finalNodeOui2);
		graph.addVertex(finalNodeNon);

		final CEdge transition1 = new CEdge(1l,"t1","age > 18");
		final CEdge transition2 = new CEdge(2l,"t2","age <= 18");
		final CEdge transition3 = new CEdge(3l,"t3","sexe == 0");
		final CEdge transition4 = new CEdge(4l,"t4","sexe == 1");


		graph.addEdge(transition1, debut, finalNodeOui1);
		graph.addEdge(transition2, debut, node);
		graph.addEdge(transition3, node, finalNodeOui2);
		graph.addEdge(transition4, node, finalNodeNon);

		final CGraph cachedGraph = new CGraph();

		final Layout<CNode,CEdge> layout=new FRLayout<CNode,CEdge>(graph);		 
		new BasicVisualizationServer<CNode,CEdge>(layout); //build layout coordinates
		
		//update node coordinates in operations
		for(final CNode n : graph.getVertices()){
			final Point2D p = layout.transform(n);
			graph.updateNodeAddedOperation(n,new CPoint(p.getX(),p.getY()));
		}
		
		cachedGraph.setInternalLayout(layout);

		//GraphUtil.updateUsedVariablesInGraph(graph);		
		
		return cachedGraph;
	}
	
	public static GraphWithOperations buildGraphWithOperationsWithNodesWithSameFinalStates()
	{
		return (GraphWithOperations)buildCGraphForExampleWithNodesWithSameFinalStates().getInternalGraph();
	}
	
	public static CGraph buildCGraphForExampleWithNodesWithSameFinalStates() 
	{
		final GraphWithOperations graph = new GraphWithOperations();
		graph.setWorkingUser(CUser.ANONYMOUS);
		
		final CNode debut = new CNode(1l,"Debut");
		debut.setShape(CShape.RECTANGLE);
		debut.setColor(Color.RED);
		
		final CNode finalNodeOui1  = new CNode(2l,"OUI");
		final CNode node  = new CNode(3l,"node");
		node.setShape(CShape.ELLIPSE);
		node.setColor(Color.GREEN);		
		final CTag tag=new CTag();
		tag.setUser(CUser.ANONYMOUS);
		tag.setTimestamp(System.currentTimeMillis());
		tag.setValue(new CTag.CValue(CNote.GOOD,"bla bla"));
		node.addTag(tag);
		final CNode finalNodeOui2 = new CNode(4l,"OUI");
		final CNode finalNodeOui3 = new CNode(5l,"OUI");		
		final CNode finalNodeOui4 = new CNode(6l,"OUI");
				
		graph.addVertex(debut);
		graph.addVertex(finalNodeOui1);
		graph.addVertex(node);
		graph.addVertex(finalNodeOui2);
		graph.addVertex(finalNodeOui3);
		graph.addVertex(finalNodeOui4);

		final CEdge transition1 = new CEdge(1l,"t1","age > 18");
		final CEdge transition2 = new CEdge(2l,"t2","age <= 18");
		final CEdge transition3 = new CEdge(3l,"t3","sexe == 0");
		final CEdge transition4 = new CEdge(4l,"t4","sexe == 1");
		final CEdge transition5 = new CEdge(5l,"t4","sexe == 2");

		graph.addEdge(transition1, debut, finalNodeOui1);
		graph.addEdge(transition2, debut, node);
		graph.addEdge(transition3, node, finalNodeOui2);
		graph.addEdge(transition4, node, finalNodeOui3);
		graph.addEdge(transition5, node, finalNodeOui4);

		final CGraph cachedGraph = new CGraph();

		final Layout<CNode,CEdge> layout=new FRLayout<CNode,CEdge>(graph);		 
		new BasicVisualizationServer<CNode,CEdge>(layout); //build layout coordinates
		
		//update node coordinates in operations
		for(final CNode n : graph.getVertices()){
			final Point2D p = layout.transform(n);
			graph.updateNodeAddedOperation(n,new CPoint(p.getX(),p.getY()));
		}
		
		cachedGraph.setInternalLayout(layout);

		//GraphUtil.updateUsedVariablesInGraph(graph);		
		
		return cachedGraph;
	}
}
