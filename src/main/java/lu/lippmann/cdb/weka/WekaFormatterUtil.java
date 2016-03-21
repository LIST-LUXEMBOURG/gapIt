/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.Calendar;

import lu.lippmann.cdb.common.FormatterUtil;
import weka.core.Attribute;


/**
 * Weka utility class.
 *
 * @author the WP1/ACORA team
 */
public final class WekaFormatterUtil 
{
	//
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private WekaFormatterUtil() {}


	//
	// Static methods
	//

	public static final String formatAttributeValue(final Attribute attr,final double d)
	{
		if (attr.isDate())
		{
			final Calendar cal=Calendar.getInstance();
			cal.setTimeInMillis((long)d);						
			return FormatterUtil.DATE_FORMAT.format(cal.getTime());
		}
		else if (attr.isNumeric())
		{
		    if (d==(int)d) return String.format("%d",(int)d);
		    else return String.format("%s",d);
		}
		else throw new IllegalStateException();
	}
}
