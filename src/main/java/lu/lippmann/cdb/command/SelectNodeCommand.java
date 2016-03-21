/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.models.CNode;

/**
 * 
 * @author
 *
 */
public class SelectNodeCommand  implements AbstractCommand {
	
	private CNode node;
	private int clickCount;

	/**
	 * 
	 * @param node
	 */
	public SelectNodeCommand(CNode node,int count) {
		this.node = node;
		this.clickCount = count;
	}

	/**
	 * 
	 * @param node
	 */
	public void setNode(CNode node) {
		this.node = node;
	}

	/**
	 * 
	 * @return
	 */
	public CNode getNode() {
		return node;
	}

	/**
	 * @return the clickCount
	 */
	public int getClickCount() {
		return clickCount;
	}

	/**
	 * @param clickCount the clickCount to set
	 */
	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}
	
	
}
