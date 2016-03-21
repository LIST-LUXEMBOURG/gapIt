/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories;

import lu.lippmann.cdb.models.CGraph;


/**
 * Abstract definition of repository.
 *
 * @author Olivier PARISOT
 */
public interface CGraphRepository 
{
	CGraph getGraph(String graphName);
	
	String[] getAvailableGraphsNames();

	void saveGraph(String graphName,CGraph cadralGraph);
}
