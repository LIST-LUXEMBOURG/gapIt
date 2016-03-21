/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.*;

import lu.lippmann.cdb.models.CNode;

import org.apache.commons.collections15.Transformer;

/**
 * 
 * @author didry
 *
 */
public class CadralVertexColorRenderer implements Transformer<CNode, Paint> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Paint transform(CNode node) {
		return new GradientPaint(0, 0, node.getColor(), 40, 40,node.getColor());
	}

}
