/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.text.*;


/**
 * FormatterUtil.
 *
 * @author Olivier PARISOT
 */
public final class FormatterUtil 
{
	//
	// Static fields
	//
	
	/** Decimal formatter. */
	public static final DecimalFormat DECIMAL_FORMAT=new DecimalFormat("##.00");
	/** Decimal formatter. */
	public static final DecimalFormat DECIMAL_FORMAT_4=new DecimalFormat("##.0000");

	/** Date formatter. */
	public static final SimpleDateFormat DATE_FORMAT=new SimpleDateFormat("dd-MM-yyyy HH:mm");

	/** Date formatter. */
	public static final SimpleDateFormat DATE_FORMAT_WITH_SEC=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private FormatterUtil() {}
	
	
	//
	// Static methods
	//
	
	public static String buildStringFromArrayOfDoubles(final double[] r)
	{
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<r.length;i++) sb.append(r[i]).append(';');
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	
	public static String buildStringFromArrayOfBooleans(final boolean[] r)
	{
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<r.length;i++) sb.append(r[i]).append(';');
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	
	public static String buildStringFromArrayOfIntegers(final int[] r)
	{
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<r.length;i++) sb.append(r[i]).append(';');
		sb.setLength(sb.length()-1);
		return sb.toString();
	}

	public static String buildStringFromArrayOfStrings(final Object[] os,final char sep) 
	{
		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<os.length;i++) sb.append(os[i]).append(sep);
		sb.setLength(sb.length()-1);
		return sb.toString();	
	}
}