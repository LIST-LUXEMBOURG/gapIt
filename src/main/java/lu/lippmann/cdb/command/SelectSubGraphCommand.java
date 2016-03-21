/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.models.CNode;

/**
 * 
 * @author didry
 *
 */
public class SelectSubGraphCommand implements AbstractCommand {

	private CNode node;
	private boolean addToSelection;


	public SelectSubGraphCommand(CNode node,boolean addToSelection) {
		this.node = node;
		this.addToSelection = addToSelection;
	}

	/**
	 * @return the node
	 */
	public CNode getNode() {
		return node;
	}
	
	/**
	 * @return the addToSelection
	 */
	public boolean isAddToSelection() {
		return addToSelection;
	}	
	
	
}
