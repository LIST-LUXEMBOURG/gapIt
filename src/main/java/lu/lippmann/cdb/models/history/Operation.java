/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models.history;

/**
 * 
 * @author didry
 *
 */
public enum Operation {
	NODE_ADDED,
	EDGE_ADDED,
	NODE_DATA_UPDATED,
	EDGE_DATA_UPDATED,
	NODE_REMOVED,
	EDGE_REMOVED,		
	NODE_MOVED,				/* node(s)position moved */
	LAYOUT_CHANGED,			/* layout changed */
	VARIABLE_CHANGED;
}