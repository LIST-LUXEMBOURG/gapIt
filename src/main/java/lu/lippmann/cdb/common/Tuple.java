/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

/**
 * 
 * @author didry
 *
 */
public class Tuple<U,V>  {

	private U x;
	private V y;


	/**
	 * 
	 * @param value
	 * @param columnIdx
	 */
	public Tuple(U x, V y) {
		this.x = x;
		this.y = y;
	}

	


	/**
	 * @return the x
	 */
	public U getX() {
		return x;
	}




	/**
	 * @param x the x to set
	 */
	public void setX(U x) {
		this.x = x;
	}




	/**
	 * @return the y
	 */
	public V getY() {
		return y;
	}




	/**
	 * @param y the y to set
	 */
	public void setY(V y) {
		this.y = y;
	}




	@Override
	public String toString() {
		return "("+x + ";" + y+")";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result+((y == null) ? 0 : y.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple<U,V> other = (Tuple<U,V>) obj;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		return true;
	}


}
