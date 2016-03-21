/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;


/**
 * 
 * @author didry
 *
 */
public final class CInput {

	private CVariable var;
	private Object    value;
	
	/**
	 * 
	 * @param type
	 * @param value
	 */
	public CInput(CVariable var, Object value) {
		this.var   = var;
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the var
	 */
	public CVariable getVariable() {
		return var;
	}

	/**
	 * @param var the var to set
	 */
	public void setVariable(CVariable var) {
		this.var = var;
	}


	@Override
	public String toString() {
		return "CInput [" + var.getKey() + "+/"+var.getType()+"=" + value + "]";
	}

}
