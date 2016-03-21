/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.beta.shih;

import lu.lippmann.cdb.common.Tuple;

/**
 * 
 * @author didry
 *
 */
public class TupleSI extends Tuple<String,Integer> implements Comparable<Tuple<String,Integer>>{

	/**
	 * 
	 * @param value
	 * @param columnIdx
	 */
	public TupleSI(String value, Integer columnIdx) {
		super(value, columnIdx);
	}

	@Override
	public int compareTo(Tuple<String, Integer> t) {
		return (getY()-t.getY()) + getX().compareTo(t.getX());
	}

}
