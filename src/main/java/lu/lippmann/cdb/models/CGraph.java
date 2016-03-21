/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;


/**
 * Cadral graph.
 *
 * TODO: current issue -> layout is not serializable, so cgraph is not really serializable ...
 *
 * @author Olivier PARISOT
 *
 */
public final class CGraph implements Serializable
{
	//
	// Static fields
	//

	/** */
	private static final long serialVersionUID=51926837557716L;

	
	//
	// Instance fields
	//
	
	/** */
	private Layout<CNode,CEdge> internalLayout;
	
	
	//
	// Instance methods
	//

	/**
	 * 
	 * @return the internalGraph
	 */
	public Graph<CNode,CEdge> getInternalGraph() 
	{
		return internalLayout.getGraph();
	}

	/**
	 * 
	 * @param internalLayout the internalLayout to set
	 */
	public void setInternalLayout(final Layout<CNode,CEdge> internalLayout) 
	{
		this.internalLayout=internalLayout;
	}

	/**
	 * 
	 * @return the internalLayout
	 */
	public Layout<CNode,CEdge> getInternalLayout() 
	{
		return internalLayout;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() 
	{
		return internalLayout.getGraph().toString();
	}

}
