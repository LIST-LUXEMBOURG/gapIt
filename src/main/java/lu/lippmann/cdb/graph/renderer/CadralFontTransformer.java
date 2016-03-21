/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.Font;

import org.apache.commons.collections15.Transformer;

public class CadralFontTransformer<E,F> implements Transformer<E,Font>{

	private Font font = new Font("Helvetica", 0, 12);
	
	private int INITIAL_SIZE = 12;
		
	@Override
	public Font transform(E arg0) {		
		return font;
	}
	
	public void setScale(double scale) {
		if(scale > 1.0)
			font = font.deriveFont((float)(INITIAL_SIZE*scale));
		else
			font = font.deriveFont((float)(INITIAL_SIZE));
	}

}
