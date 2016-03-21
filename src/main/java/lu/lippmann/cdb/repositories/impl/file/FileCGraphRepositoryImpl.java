/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories.impl.file;

import lu.lippmann.cdb.common.FileUtil;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.CGraph;
import lu.lippmann.cdb.repositories.CGraphRepository;


/**
 * File implementation of repository.
 *
 * @author Jérôme Wax, Yoann Didry, Olivier PARISOT
 */
public final class FileCGraphRepositoryImpl implements CGraphRepository
{
	//
	// Static fields
	//
	
	/** */
	private static final String GRAPH_FILENAME="graph.xml";
	
	
	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CGraph getGraph(final String graphname) 
	{
		final CGraph cgraph=new CGraph();
		cgraph.setInternalLayout(GraphUtil.getLayoutFromXML(FileUtil.getFileContent(GRAPH_FILENAME).toString()));
		return cgraph;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getAvailableGraphsNames() 
	{
		return new String[]{GRAPH_FILENAME};
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveGraph(final String graphName,final CGraph cadralGraph) 
	{
		throw new IllegalStateException("not implemented!");
	}

}
