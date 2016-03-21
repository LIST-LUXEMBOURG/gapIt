/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.repositories;

import java.util.Set;

import lu.lippmann.cdb.models.CVariable;


/**
 * 
 *
 *
 * @author Olivier PARISOT
 */
public interface CVariablesRepository 
{
	Set<CVariable> getCadralVariables();
	
	void addOrUpdateCadralVariable(CVariable cv);
	
	void removeCadralVariable(CVariable cv);
	
	boolean contains(CVariable cv);
}
