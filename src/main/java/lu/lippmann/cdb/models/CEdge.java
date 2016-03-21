/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;
import java.util.Random;


/**
 * 
 * 
 * @author
 *
 */
public class CEdge extends CGraphElementImpl implements Serializable  {

	private static final long serialVersionUID = 428839330584598L;
	
	private String expression;

	private static final Random RANDOM = new Random();
	
	/**
	 * 
	 */
	public CEdge(){
		this("","");
	}
	
	/**
	 * 
	 * @param name
	 */
	public CEdge(String name) {
		this(name,"");
	}
	
	/**
	 * 
	 * @param name
	 */
	public CEdge(String name, String expression) {
		this(RANDOM.nextLong(),name,expression);
	}
	
	/**
	 * 
	 * @param name
	 */
	public CEdge(Long id,String name, String expression) {
		super(id,name);
		this.expression = expression;
	}
	
	/**
	 * 
	 * @param cpy
	 */
	public CEdge(CEdge cpy){
		super(cpy.id,cpy.name);
		this.expression=cpy.expression;
	}
	
	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	/**
	 * All the fields are the same ?
	 * @param edge
	 * @return
	 */
	public boolean containsSameFieldsThat(CEdge edge){
		return id.equals(edge.id) && 
			   name.equals(edge.name) &&
			   expression.equals(edge.expression);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "CEdge [id=" + id + ", name=" + name + ", expression="
				+ expression + "]";
	}

}
