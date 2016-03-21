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


/**
 * Definition of a digraph DSL.
 * 
 * Format: [node-name]->[node-name] or [node-name] [desc]
 *
 * @author Olivier PARISOT
 */
public final class DiGraphDsl implements GraphDsl 
{
	//
	// Static fields
	//
	
	/** */
	private static final String ARROW_SEPARATOR="->";
	/** */
	private static final String LINE_SEPARATOR="\n";

	
	
	//
	// Instance methods
	//	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDslString(final Graph<CNode,CEdge> gr)
	{	
		throw new IllegalStateException("not implemented");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphDslParsingResult getGraphDslParsingResult(final String stringFormat)
	{
		//System.out.println(stringFormat);
		
		final Map<Integer,String> linesWithError=new HashMap<Integer,String>();
		final GraphWithOperations gwo=new GraphWithOperations();
		
		final Map<String,CNode> cnodeMap=new HashMap<String,CNode>();		
		final Scanner lineScanner=new Scanner(stringFormat).useDelimiter(LINE_SEPARATOR);
		int lineNumber=0;
		while (lineScanner.hasNext())
		{
			final String line=lineScanner.next();			
			try 
			{
				if (line.contains(ARROW_SEPARATOR))
				{
					//System.out.println(line);
					final Scanner scanner=new Scanner(line).useDelimiter(ARROW_SEPARATOR);
					final String sourceNodeName=scanner.next();
					final String targetNodeName=scanner.next();
					
					if (!cnodeMap.containsKey(sourceNodeName)) 
					{	
						final CNode firstNode=new CNode((long)sourceNodeName.hashCode(),sourceNodeName);
						cnodeMap.put(sourceNodeName,firstNode);
						gwo.addVertex(firstNode);
					}			
					if (!cnodeMap.containsKey(targetNodeName)) 
					{	
						final CNode secondNode=new CNode((long)targetNodeName.hashCode(),targetNodeName);
						cnodeMap.put(targetNodeName,secondNode);
						gwo.addVertex(secondNode);
					}
					
					if (!sourceNodeName.equals(targetNodeName))
					{
						final CEdge edge=new CEdge((long)line.hashCode(),line,line);
						gwo.superAddEdge(edge,cnodeMap.get(sourceNodeName),cnodeMap.get(targetNodeName));
					}
				}
			} 
			catch (Throwable t) 
			{				
				linesWithError.put(lineNumber,t.getMessage());
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
		return new String[]{ARROW_SEPARATOR};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCommentMarker() 
	{		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDslFormat() 
	{
		return "[node]->[node]";
	}


}
