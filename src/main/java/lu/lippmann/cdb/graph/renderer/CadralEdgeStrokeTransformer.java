/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.*;
import java.util.Map;

import lu.lippmann.cdb.models.CEdge;

import org.apache.commons.collections15.Transformer;

/**
 * 
 * @author Didry
 *
 */
public class CadralEdgeStrokeTransformer implements Transformer<CEdge,Stroke> {

	private static final float MIN_WIDTH     = 1.0f;
	private static final float MAX_WIDTH     = 15.0f;
	private Map<CEdge,Float> mapWeigth;
	
	/**
	 * Weigth of 1.0 is normal size (default width)
	 * @param mapWeight
	 */
	public CadralEdgeStrokeTransformer(final Map<CEdge,Float> mapWeight){
		this.mapWeigth = mapWeight;
	}

	@Override
	public Stroke transform(final CEdge edge) {
		if(mapWeigth==null||!mapWeigth.containsKey(edge)){
			return new BasicStroke(MIN_WIDTH);
		}else{
			float width = MAX_WIDTH * mapWeigth.get(edge);
			if(width < MIN_WIDTH){
				return new BasicStroke(MIN_WIDTH);
			}else{
				return new BasicStroke(width);
			}
		}
	}

}
