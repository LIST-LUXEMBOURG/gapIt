/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.util.*;

import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;


/**
 * Definition of a simple Graph DSL.
 * 
 * Format: [node-name] -> [node-name]: [edge-name] {[rule]}
 *
 * @author Olivier PARISOT
 */
public final class SimpleGraphDsl implements GraphDsl 
{
	//
	// Static fields
	//
	
	/** */
	private static final String SPACE_SEPARATOR=" ";
	/** */
	private static final String DP_SEPARATOR=":";
	/** */
	private static final String ARROW_SEPARATOR="->";
	/** */
	private static final String LINE_SEPARATOR="\n";
	/** */
	private static final String OPENING_ACC_SEPARATOR="{";
	/** */
	private static final String CLOSING_ACC_SEPARATOR="}";
	/** */
	private static final String COMMENT_MARKER="#";
	
	
	//
	// Instance methods
	//	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDslString(final Graph<CNode,CEdge> gr)
	{	
		if (gr==null) throw new IllegalArgumentException("graph should not be null!");
		
		final List<CEdge> sortedList=new ArrayList<CEdge>();
		sortedList.addAll(gr.getEdges());
		Collections.sort(sortedList);
		
		final StringBuilder txt=new StringBuilder();
		txt.append(COMMENT_MARKER).append("comments\n");
		for (CEdge ce:sortedList)
		{
			final Pair<CNode> endpoints=gr.getEndpoints(ce);
			txt.append(endpoints.getFirst().getName())
			   .append(SPACE_SEPARATOR)
			   .append(ARROW_SEPARATOR)
			   .append(SPACE_SEPARATOR)
			   .append(endpoints.getSecond().getName())
			   .append(DP_SEPARATOR)
			   .append(SPACE_SEPARATOR)
			   .append(ce.getName())
			   .append(SPACE_SEPARATOR)
			   .append(OPENING_ACC_SEPARATOR)
			   .append(ce.getExpression())
			   .append(CLOSING_ACC_SEPARATOR)
			   .append(SPACE_SEPARATOR)			   
			   .append(LINE_SEPARATOR);
		}
		return txt.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphDslParsingResult getGraphDslParsingResult(final String stringFormat)
	{
		final Map<Integer,String> linesWithError=new HashMap<Integer,String>();
		final GraphWithOperations gwo=new GraphWithOperations();
		
		final Map<String,CNode> cnodeMap=new HashMap<String,CNode>();		
		final Scanner lineScanner=new Scanner(stringFormat).useDelimiter(LINE_SEPARATOR);
		int lineNumber=0;
		while (lineScanner.hasNext())
		{
			final String line=lineScanner.next();
			if (line.length()>0&&!line.startsWith(COMMENT_MARKER))
			{
				try 
				{
					final Scanner scanner=new Scanner(line).useDelimiter(DP_SEPARATOR);
					final String nodesDesc=scanner.next();
					final Scanner nodesScanner=new Scanner(nodesDesc).useDelimiter(ARROW_SEPARATOR);
					if (!nodesScanner.hasNext()) throw new Exception("Missing first node name!");
					final String firstNodeName=nodesScanner.next().trim();
					if (firstNodeName.length()==0) throw new Exception("Empty first node name!");
					if (!nodesScanner.hasNext()) throw new Exception("Missing second node name!");
					final String secondNodeName=nodesScanner.next().trim();
					if (secondNodeName.length()==0) throw new Exception("Empty second node name!");
					if (firstNodeName.compareTo(secondNodeName)==0) throw new Exception("The two nodes can not be the same!");
					if (!cnodeMap.containsKey(firstNodeName)) 
					{	
						final CNode firstNode=new CNode((long)firstNodeName.hashCode(),firstNodeName);
						cnodeMap.put(firstNodeName,firstNode);
						gwo.addVertex(firstNode);
					}			
					if (!cnodeMap.containsKey(secondNodeName)) 
					{	
						final CNode secondNode=new CNode((long)secondNodeName.hashCode(),secondNodeName);
						cnodeMap.put(secondNodeName,secondNode);
						gwo.addVertex(secondNode);
					}
					if (!scanner.hasNext()) throw new Exception("Missing edge desc!");
					final String edgeDesc=scanner.next().trim();
					if (edgeDesc.length()==0) throw new Exception("Incomplete edge desc!");
					final int openingBracketIdx=edgeDesc.indexOf(OPENING_ACC_SEPARATOR);
					if (openingBracketIdx>0)
					{
						final int closingBracketIdx=edgeDesc.indexOf(CLOSING_ACC_SEPARATOR);
						if (closingBracketIdx<0) throw new Exception("Missing closing bracket!");
						final String edgeName=edgeDesc.substring(0,openingBracketIdx).trim();
						final String edgeRule=edgeDesc.substring(openingBracketIdx+1,closingBracketIdx).trim();
						final CEdge edge=new CEdge((long)edgeName.hashCode(),edgeName,edgeRule);
						gwo.superAddEdge(edge,cnodeMap.get(firstNodeName),cnodeMap.get(secondNodeName));
					}
					else
					{
						final String edgeName=edgeDesc.trim();
						final CEdge edge=new CEdge((long)edgeName.hashCode(),edgeName,"");
						gwo.superAddEdge(edge,cnodeMap.get(firstNodeName),cnodeMap.get(secondNodeName));				
					}
				} 
				catch (Throwable t) 
				{				
					linesWithError.put(lineNumber,t.getMessage());
				}
			}
			lineNumber++;
		}
		return new GraphDslParsingResult(gwo,linesWithError);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getKeywords() 
	{		
		return new String[]{ARROW_SEPARATOR,DP_SEPARATOR};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCommentMarker() 
	{		
		return COMMENT_MARKER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDslFormat() 
	{
		return "[node] -> [node]: [edge] {[rule]}";
	}


}
