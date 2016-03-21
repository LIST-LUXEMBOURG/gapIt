/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.util.*;

import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.graph.Graph;


/**
 * 
 *
 *
 * @author Olivier PARISOT
 */
public class GraphDslParsingResult 
{
	//
	// Instance fields
	//
	
	/** */
	private final Graph<CNode,CEdge> graph;
	/** */
	private final Map<Integer,String> linesWithError;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public GraphDslParsingResult(final Graph<CNode, CEdge> graph,final Map<Integer,String> linesWithError) 
	{		
		this.graph = graph;
		this.linesWithError = linesWithError;
	}

	
	//
	// Instance methods
	//

	/**
	 * @return the graph
	 */
	public Graph<CNode,CEdge> getGraph() 
	{
		return graph;
	}

	/**
	 * @return the linesWithError
	 */
	public Map<Integer,String> getLinesWithError()
	{
		return linesWithError;
	}
}
