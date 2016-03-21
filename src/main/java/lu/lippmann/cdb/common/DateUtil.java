/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.Calendar;


/**
 * DateUtil.
 * 
 * @author Olivier PARISOT
 */
public final class DateUtil 
{
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private DateUtil() {}

	//
	// Static methods
	//
	
	public static String getSeason(final int month, final int day) 
	{
		if ((month == 1) || (month == 2))
			return ("Winter");

		else if ((month == 4) || (month == 5))
			return ("Spring");

		else if ((month == 7) || (month == 8))
			return ("Summer");

		else if ((month == 10) || (month == 11))
			return ("Autumn");

		else if ((month == 3) && (day <= 19))
			return ("Winter");

		else if ((month == 3) && (day >= 20))
			return ("Spring");

		else if ((month == 6) && (day <= 20))
			return ("Spring");

		else if ((month == 6) && (day >= 21))
			return ("Summer");

		else if ((month == 9) && (day <= 20))
			return ("Summer");

		else if ((month == 9) && (day >= 21))
			return ("Autumn");

		else if ((month == 12) && (day <= 21))
			return ("Autumn");

		else if ((month == 12) && (day >= 22))
			return ("Winter");
		else
			throw new IllegalStateException("month="+month+",day="+day);
	}
	
	public static String getSeason(final long timestamp)
	{
		final Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(timestamp);				
		return getSeason(cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
	}

	public static int getYear(final long timestamp) 
	{
		final Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(timestamp);				
		return cal.get(Calendar.YEAR);
	}
}
