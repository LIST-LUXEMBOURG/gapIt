/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.models.CEdge;

/**
 * 
 * @author
 *
 */
public class SelectEdgeCommand  implements AbstractCommand {
	
	private CEdge edge;
	private int clickCount;

	/**
	 * 
	 * @param edge
	 */
	public SelectEdgeCommand(CEdge edge,int count) {
		this.edge = edge;
		this.clickCount = count;
	}

	/**
	 * 
	 * @param edge
	 */
	public void setEdge(CEdge edge) {
		this.edge = edge;
	}

	/**
	 * 
	 * @return
	 */
	public CEdge getEdge() {
		return edge;
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
