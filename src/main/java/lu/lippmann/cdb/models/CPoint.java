/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;

/**
 * 
 * @author didry
 *
 */
public class CPoint implements Serializable {

	private static final long serialVersionUID = -9038609585289137096L;
	
	private double x;
	private double y;
	
	/**
	 * 
	 */
	public CPoint(){
		this(0,0);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public CPoint(double x,double y){
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	
	
}
