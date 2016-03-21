/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.renderer;

import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

import lu.lippmann.cdb.models.CEdge;

import org.apache.commons.collections15.Transformer;

/**
 * 
 * @author Didry
 *
 */
public class CadralEdgeColorTransformer implements Transformer<CEdge,Paint> {

	private int nbColors = 20;

	private Color color1;
	private Color color2;
	private Map<CEdge,Float> mapWeigth;

	private final List<Color> colors; 


	private static Color rangeColor(Color colorMin,Color colorMax, BigDecimal grow,BigDecimal minValue,BigDecimal maxValue)
	{
		BigDecimal colorValue = grow;
		if(colorValue == null)
		{
			return Color.LIGHT_GRAY;
		}

		if(maxValue.compareTo(minValue) < 0){
			return rangeColor(colorMin, colorMax, grow, maxValue, minValue);
		}else{
			int rMax = colorMax.getRed();
			int gMax = colorMax.getGreen();
			int bMax = colorMax.getBlue();

			double color = 0.0;

			color = 255.0- grow.subtract(minValue).doubleValue()*255.0 / maxValue.subtract(minValue).doubleValue();

			int r = rMax + (int) ( ( 255 - rMax ) * color / 255.0 );
			int g = gMax + (int) ( ( 255 - gMax ) * color / 255.0 );
			int b = bMax + (int) ( ( 255 - bMax ) * color / 255.0 );
			
			if(r > 192 && g > 192 && b > 192) return Color.LIGHT_GRAY;
			
			return new Color(r, g, b);
		}

	}

	/**
	 * 
	 * @param color1
	 * @param color2
	 * @param nb
	 * @return
	 */
	private List<Color> getColorList()
	{

		final List<Color> res = new ArrayList<Color>();

		for(int i=0;i<nbColors;i++){
			res.add(rangeColor(color1,color2,
					BigDecimal.valueOf(i), 
					BigDecimal.ZERO, 
					BigDecimal.valueOf(nbColors)));
		}
		return res ;
	}

	/**
	 * Weigth of 1.0 is normal size (default width)
	 * @param mapWeight
	 */
	public CadralEdgeColorTransformer(final Map<CEdge,Float> mapWeight,final Color colorMin,final Color colorMax,final int nbColors){
		this.mapWeigth = mapWeight;
		this.color1 = colorMin;
		this.color2 = colorMax;
		this.nbColors = nbColors;
		this.colors = getColorList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color transform(final CEdge edge) {
		if(mapWeigth==null||!mapWeigth.containsKey(edge)){
			return Color.BLACK;
		}else{
			final int pos = (int) ( mapWeigth.get(edge) * (nbColors-1) );  
			return colors.get(pos);
		}
	}

}
