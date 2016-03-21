/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.awt.geom.Point2D;

/**
 * 
 * @author didry
 *
 */
public class CNodePosition {

	private CNode node;
	private Point2D point;
	
	/**
	 * 
	 * @param node
	 * @param point
	 */
	public CNodePosition(CNode node, Point2D point) {
		super();
		this.node = node;
		this.point = point;
	}
	/**
	 * @return the node
	 */
	public CNode getNode() {
		return node;
	}
	/**
	 * @param node the node to set
	 */
	public void setNode(CNode node) {
		this.node = node;
	}
	/**
	 * @return the point
	 */
	public Point2D getPoint() {
		return point;
	}
	/**
	 * @param point the point to set
	 */
	public void setPoint(Point2D point) {
		this.point = point;
	}
	
	
}
