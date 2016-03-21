/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories.impl.memory;

import java.util.*;

import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.CGraph;
import lu.lippmann.cdb.repositories.CGraphRepository;
import lu.lippmann.cdb.util.FakeCGraphBuilder;


/**
 * Memory implementation of repository.
 *
 * @author Jérôme Wax, Yoann Didry, Olivier PARISOT
 */
public final class MemoryCGraphRepositoryImpl implements CGraphRepository
{
	//
	// Static fields
	//
	
	/** */
	private static final String EXAMPLE_GRAPHNAME="example";
	
	
	//
	// Instance fields
	//

	/** */
	private Map<String,CGraph> map;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor. 
	 */
	public MemoryCGraphRepositoryImpl()
	{
		this.map=new HashMap<String,CGraph>();
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
		final Set<String> keySet=map.keySet();
		final String[] t=new String[keySet.size()];
		keySet.toArray(t);
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveGraph(final String graphName,final CGraph cadralGraph) 
	{
		map.put(graphName,cadralGraph);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CGraph getGraph(final String graphName) 
	{
		return GraphUtil.copyCGraph(map.get(graphName));		
	}





}
