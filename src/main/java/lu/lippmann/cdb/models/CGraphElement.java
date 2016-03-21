/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.util.List;


/**
 * CGraph element: ancestor for CNode, CEdge, and GraphWithOperation!
 *
 * @author Olivier PARISOT
 */
public interface CGraphElement 
{
	Long getId();
	
	void setId(Long id);
	
	String getName();
	
	void addTag(CTag tag);
	
	void removeTag(CTag tag);
	
	List<CTag> getTags();

	void clearAndAddTags(List<CTag> tags);	
}