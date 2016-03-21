/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories.impl.neo4j;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CTag.CNote;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import lu.lippmann.cdb.repositories.CGraphRepository;
import lu.lippmann.cdb.util.FakeCGraphBuilder;

import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.util.Pair;


/**
 * Neo4j implementation of repository.
 *
 * @author Olivier PARISOT
 */
public final class EmbeddedNeo4jCGraphRepositoryImpl implements CGraphRepository
{
	//
	// Static fields
	//

	/** */
	private static final String NODE_ID_FIELD    = "nodeId";
	/** */
	private static final String NODE_NAME_FIELD  = "nodeName";
	/** */
	private static final String NODE_POSITION    = "nodePos";
	/** */
	private static final String NODE_COLOR_FIELD = "nodeColor";
	/** */
	private static final String NODE_SHAPE_FIELD = "nodeShape";
	/** */
	private static final String CE_DB_PATTERN="graph-";
	/** */
	private static final String CE_DB_PATH="ce-db/";
	/** */
	private static final String EXAMPLE_GRAPHNAME="example";
	/** */
	private static final String EDGE_ID_FIELD 		  = "edgeId";
	/** */
	private static final String EDGE_NAME_FIELD	 	  = "edgeName";
	/** */
	private static final String EDGE_EXPRESSION_FIELD = "edgeExpression";
	/** */
	private static final String TAGS_FIELD="tag";


	//
	// Enums
	//

	/** */
	private static enum EdgeRelationshipType implements RelationshipType {ISLINKEDTO}


	//
	// Constructors
	//

	/**
	 * Constructor. 
	 */
	public EmbeddedNeo4jCGraphRepositoryImpl()
	{
		saveGraph(EXAMPLE_GRAPHNAME,FakeCGraphBuilder.buildCGraphForExample());
	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getAvailableGraphsNames() 
	{
		try 
		{
			final File dir=new File(CE_DB_PATH);
			final FilenameFilter filter=new FilenameFilter() 
			{
				@Override
				public boolean accept(final File dir,final String name) 
				{
					return name.startsWith(CE_DB_PATTERN);
				}
			};
			final String[] children=dir.list(filter);
			final String[] r=new String[children.length];
			for (int i=0;i<children.length;i++) r[i]=children[i].substring(CE_DB_PATTERN.length());
			return r;
		} 
		catch (Exception ioe) 
		{
			ioe.printStackTrace();
		}
		return new String[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveGraph(final String graphName,final CGraph cadralGraph) 
	{
		//check if the directory already exists 
		boolean dbExists = new File(CE_DB_PATH+CE_DB_PATTERN+graphName).exists();

		final GraphDatabaseService graphDb=new EmbeddedGraphDatabase(CE_DB_PATH+CE_DB_PATTERN+graphName);
		final Transaction tx=graphDb.beginTx();

		//Clean existing nodes with their relationship
		if(dbExists){
			final Iterator<Node> existingNodes = graphDb.getAllNodes().iterator();
			while(existingNodes.hasNext())
			{
				final Node node = existingNodes.next();
				final Iterator<Relationship> rels = node.getRelationships().iterator();
				while(rels.hasNext())
				{
					final Relationship rel = rels.next();
					rel.delete();
				}
				node.delete();
			}
		}

		try 
		{			
			final Map<Long,Node> m=new HashMap<Long,Node>();
			for (final CNode cnode:cadralGraph.getInternalGraph().getVertices())
			{
				final Node node=graphDb.createNode();
				node.setProperty(NODE_ID_FIELD,cnode.getId());
				node.setProperty(NODE_NAME_FIELD,cnode.getName());
				node.setProperty(NODE_SHAPE_FIELD,cnode.getShape().ordinal()); //dangerous ...
				node.setProperty(NODE_COLOR_FIELD,cnode.getColor().getRGB());
				final Point2D position = cadralGraph.getInternalLayout().transform(cnode);
				node.setProperty(NODE_POSITION,new String[]{""+position.getX(),""+position.getY()});
				node.setProperty(TAGS_FIELD,buildStringFromTags(cnode.getTags()));
				m.put(cnode.getId(),node);
			}
			for (final CEdge cedge:cadralGraph.getInternalGraph().getEdges())
			{
				final Pair<CNode> endpoints=cadralGraph.getInternalGraph().getEndpoints(cedge);
				final Relationship relationship=m.get(endpoints.getFirst().getId()).createRelationshipTo(m.get(endpoints.getSecond().getId()),EdgeRelationshipType.ISLINKEDTO);
				relationship.setProperty(EDGE_ID_FIELD,cedge.getId());
				relationship.setProperty(EDGE_NAME_FIELD,cedge.getName());
				relationship.setProperty(EDGE_EXPRESSION_FIELD,cedge.getExpression());
				relationship.setProperty(TAGS_FIELD,buildStringFromTags(cedge.getTags()));
				//relationship.setProperty("Variables",GraphUtil.used arg1)
			}
			tx.success();
		}
		finally 
		{
			tx.finish();
			graphDb.shutdown();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CGraph getGraph(final String graphName) 
	{
		final CGraph cgraph = new CGraph();
		final GraphWithOperations gwo = new GraphWithOperations();
		final StaticLayout<CNode,CEdge> layout = new StaticLayout<CNode, CEdge>(gwo);
		cgraph.setInternalLayout(layout);
		final GraphDatabaseService graphDb=new EmbeddedGraphDatabase(CE_DB_PATH+CE_DB_PATTERN+graphName);
		final Iterator<Node> nodes = graphDb.getAllNodes().iterator();
		try {
			while(nodes.hasNext()){
				final Node  tmpNode = nodes.next();
				/** avoid taking into account another nodes **/
				if(tmpNode.hasProperty(NODE_ID_FIELD)){
					final CNode node    = createCNodeFromNode(tmpNode);
					gwo.addVertex(node);
					final Iterator<Relationship> relations = tmpNode.getRelationships().iterator();
					while(relations.hasNext()){
						final Relationship relation = relations.next();
						final CEdge edge = new CEdge((Long)relation.getProperty(EDGE_ID_FIELD),(String)relation.getProperty(EDGE_NAME_FIELD),(String)relation.getProperty(EDGE_EXPRESSION_FIELD));
						for (CTag t:buildTagsFromStringFromTags((String)relation.getProperty(TAGS_FIELD))) edge.addTag(t);
						gwo.addEdge(edge, createCNodeFromNode(relation.getStartNode()), createCNodeFromNode(relation.getEndNode()));
					}
					final String[] pos=  (String[])tmpNode.getProperty(NODE_POSITION);
					final Point2D position = new Point2D.Double(Double.valueOf(pos[0]),Double.valueOf(pos[1]));
					layout.setLocation(node,position);
					//Update operation with position
					gwo.updateNodeAddedOperation(node, new CPoint(position.getX(),position.getY())); 
				}
			}
		}
		finally{
			graphDb.shutdown();
		}
		return cgraph;
	}


	/**
	 * 
	 * @param tmpNode
	 */
	private CNode createCNodeFromNode(final Node tmpNode) {
		final CNode node = new CNode((Long)tmpNode.getProperty(NODE_ID_FIELD),(String)tmpNode.getProperty(NODE_NAME_FIELD));
		node.setShape(CShape.values()[(Integer)tmpNode.getProperty(NODE_SHAPE_FIELD)]);
		node.setColor(new Color((Integer)tmpNode.getProperty(NODE_COLOR_FIELD)));
		for (CTag t:buildTagsFromStringFromTags((String)tmpNode.getProperty(TAGS_FIELD))) node.addTag(t);
		return node;
	}

	private String buildStringFromTags(final List<CTag> tags) 
	{
		final StringBuilder sb=new StringBuilder();
		for (CTag t:tags)
		{
			sb.append(t.getValue().getNote().ordinal()).append(';')
			.append(t.getValue().getDesc()).append(';')
			.append(t.getUser().getName()).append(';')
			.append(t.getUser().getHostName()).append(';')
			.append(t.getTimestamp()).append('\n');
		}
		//System.out.println("built -> "+sb.toString());
		return sb.toString();
	}

	private List<CTag> buildTagsFromStringFromTags(final String s) 
	{
		//System.out.println("to parse -> "+s);
		final List<CTag> tags=new ArrayList<CTag>();
		final Scanner scanner=new Scanner(s).useDelimiter("\n");
		while (scanner.hasNext())
		{
			final String tagStringed=scanner.next();
			final Scanner subscanner=new Scanner(tagStringed).useDelimiter(";");
			final CTag tag=new CTag();
			tag.setValue(new CTag.CValue(CNote.values()[Integer.valueOf(subscanner.next())],subscanner.next()));
			tag.setUser(new CUser(subscanner.next(),subscanner.next()));
			tag.setTimestamp(Long.valueOf(subscanner.next()));
			tags.add(tag);
		}
		return tags;
	}


}
