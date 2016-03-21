/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

/**
 * 
 * @author didry
 *
 */
public enum CShape {
	
	RECTANGLE("rectangle",100,40),
	CARRE("square",100,100),
	CERCLE("circle",100,100),
	ELLIPSE("ellipse",100,40);
	
	private String label;	
	private int width;
	private int height;
	
	private CShape(String label,int width, int height){
		this.label = label;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString(){
		return label;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
}
