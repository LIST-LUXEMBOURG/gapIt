/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

import org.codehaus.jackson.annotate.JsonIgnore;

import lu.lippmann.cdb.graph.renderer.CShape;


/**
 * 
 * 
 * 
 * @author
 *
 */
public final class CNode extends CGraphElementImpl implements Serializable {
	
	private static final long serialVersionUID = 519267383757716L;
	
	private CShape shape = CShape.RECTANGLE;
	
	private Color  color = Color.WHITE;
	
	private static final Random RANDOM = new Random(); 
	
	/**
	 * 
	 */
	public CNode(){
		this("empty");
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public CNode(String name) {
		this(RANDOM.nextLong(),name);
	}
	
	/**
	 * 
	 * @param name
	 */
	public CNode(Long id,String name) {
		super(id,name);
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param color2
	 * @param shape2
	 */
	public CNode(String name, Color color, CShape shape) {
		this(RANDOM.nextLong(),name,color,shape);
	}
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param color2
	 * @param shape2
	 */
	public CNode(Long id, String name, Color color, CShape shape) {
		super(id,name);
		this.color = color;
		this.shape = shape;
	}

	/**
	 * @return the shape
	 */
	public CShape getShape() {
		return shape;
	}

	/**
	 * @param shape the shape to set
	 */
	public void setShape(CShape shape) {
		this.shape = shape;
	}
	
	/**
	 * @return the color
	 */
	@JsonIgnore
	public Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "CNode [id=" + id + ", name=" + name + "]";
	}



}
