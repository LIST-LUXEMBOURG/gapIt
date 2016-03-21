/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.util.Map;


/**
 * 
 * @author didry
 *
 */
public class CLayoutTransition {

	private Map<CNode,CPoint> oldLayout;
	private Map<CNode,CPoint> newLayout;
	
	/**
	 * 
	 * @param oldLayout
	 * @param newLayout
	 */
	public CLayoutTransition(Map<CNode, CPoint> oldLayout,Map<CNode, CPoint> newLayout) {
		super();
		this.oldLayout = oldLayout;
		this.newLayout = newLayout;
	}
	/**
	 * @return the oldLayout
	 */
	public Map<CNode, CPoint> getOldLayout() {
		return oldLayout;
	}
	/**
	 * @param oldLayout the oldLayout to set
	 */
	public void setOldLayout(Map<CNode, CPoint> oldLayout) {
		this.oldLayout = oldLayout;
	}
	/**
	 * @return the newLayout
	 */
	public Map<CNode, CPoint> getNewLayout() {
		return newLayout;
	}
	/**
	 * @param newLayout the newLayout to set
	 */
	public void setNewLayout(Map<CNode, CPoint> newLayout) {
		this.newLayout = newLayout;
	}
	
	
}
