/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import java.util.List;

import lu.lippmann.cdb.models.CEdge;


/**
 * 
 * 
 * @author didry
 *
 */
public class HighlightCommand implements AbstractCommand {

	private final List<CEdge> path;
	
	public HighlightCommand(List<CEdge> path){
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public List<CEdge> getPath() {
		return path;
	}

	
}
