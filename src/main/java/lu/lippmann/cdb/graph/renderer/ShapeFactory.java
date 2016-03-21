/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.Shape;
import java.awt.geom.*;

/**
 * 
 * @author didry
 *
 */
public final class ShapeFactory {

	private ShapeFactory(){

	}

	/**
	 * 
	 * @param shape
	 * @return
	 */
	public static Shape createShape(CShape shape){
		return createShape(shape,1);
	}

	
	public static Shape createShape(CShape shape, double scale){
		final Shape res;
		final int w = (int)(shape.getWidth()*scale);
		final int h = (int)(shape.getHeight()*scale);
		switch(shape){
		case CARRE:
			res = new RoundRectangle2D.Double(-w/2, -h/2, w, h, 5, 5);
			break;
		case RECTANGLE:
			res = new RoundRectangle2D.Double(-w/2, -h/2, w, h, 5, 5);
			break;
		case CERCLE:
			res = new Ellipse2D.Double(-w/2, -h/2, w, h);					
			break;
		case ELLIPSE:
			res = new Ellipse2D.Double(-w/2, -h/2, w, h);
			break;
		default:
			res = null;
			throw new IllegalStateException("Unknown shape !");
		}
		return res;
	}
}
