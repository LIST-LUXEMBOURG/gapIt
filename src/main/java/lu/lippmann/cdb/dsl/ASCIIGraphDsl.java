/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.util.*;

import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import edu.uci.ics.jung.graph.Graph;


/**
 * Definition of an ascii Graph DSL.
 *
 * @author Olivier PARISOT
 */
public final class ASCIIGraphDsl implements GraphDsl 
{
	//
	// Static fields
	//
	
	/** */
	private static final String TAB_SEPARATOR="| ";
	/** */
	private static final String FINAL_NODE_MARKER=": ";
	/** */
	private static final String LINE_SEPARATOR="\n";
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
		final CNode root=GraphUtil.getFirstRoot(gr);
		if (root==null) return "";
		final StringBuilder sb=getString(gr,root,0);		
		return sb.substring(1);
	}
	
	private StringBuilder getString(final Graph<CNode,CEdge> gr,final CNode cNode,final int level)
	{
		final StringBuilder txt=new StringBuilder();
		
		final Collection<CEdge> outEdges=gr.getOutEdges(cNode);
		if (outEdges.isEmpty())
		{
			txt.append(FINAL_NODE_MARKER).append(cNode.getName());
		}
		else
		{				
			for (final CEdge ce:outEdges)
			{
				txt.append(LINE_SEPARATOR)
					.append(buildLevelSeparator(level))
					.append(ce.getExpression())					
					.append(getString(gr,gr.getDest(ce),level+1));	   
			}	
		}
		
		return txt;
	}
	
	private StringBuilder buildLevelSeparator(final int level)
	{
		final StringBuilder sep=new StringBuilder(level);
		for (int i=0;i<level;i++) sep.append(TAB_SEPARATOR);
		return sep;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphDslParsingResult getGraphDslParsingResult(final String stringFormat)
	{
		final Map<Integer,String> linesWithError=new HashMap<Integer,String>();
		final GraphWithOperations gwo=new GraphWithOperations();
		
		if (!stringFormat.isEmpty())
		{
			final Map<Integer,CNode> rootsMap=new HashMap<Integer,CNode>();
			rootsMap.put(0,new CNode("root"));
			gwo.addVertex(rootsMap.get(0));
			final Scanner lineScanner=new Scanner(stringFormat).useDelimiter(LINE_SEPARATOR);
			int lineNumber=0;
			while (lineScanner.hasNext())
			{
				final String line=lineScanner.next();
				//System.out.println("parse here -> "+line);
				if (!line.isEmpty()&&!line.startsWith(COMMENT_MARKER))
				{
					try 
					{
						final Scanner scanner=new Scanner(line).useDelimiter(FINAL_NODE_MARKER);
						final String exprDesc=parseExpression(scanner.next());
						//System.out.println("expression -> "+exprDesc);
						final int level=computeLevel(line);
						if (scanner.hasNext())
						{							
							final CNode end=new CNode(scanner.next());
							gwo.addVertex(end);
							gwo.superAddEdge(new CEdge("feur",exprDesc),rootsMap.get(level),end);
						}
						else
						{
							final CNode newRoot=new CNode("subRoot");
							rootsMap.put(level+1,newRoot);
							gwo.addVertex(newRoot);
							gwo.superAddEdge(new CEdge("feur",exprDesc),rootsMap.get(level),newRoot);
						}
					} 
					catch (Throwable t) 
					{	
						t.printStackTrace();
						linesWithError.put(lineNumber,t.getMessage());
					}
				}
				lineNumber++;
			}
		}
		return new GraphDslParsingResult(gwo,linesWithError);
	}

	private int computeLevel(final String line0) 
	{
		String line=line0;
		int level=0;
		while (line.startsWith(TAB_SEPARATOR))
		{			
			level++;
			line=line.substring(TAB_SEPARATOR.length());
		}
		return level;
	}
	
	private String parseExpression(final String expr0) 
	{
		String expr=expr0;
		while (expr.startsWith(TAB_SEPARATOR))
		{						
			expr=expr.substring(TAB_SEPARATOR.length());
		}
		return expr;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getKeywords() 
	{		
		return new String[]{};
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
		return "[rule]: FINAL_STATE OR [rule]\\n| [rule] ...";
	}

}
