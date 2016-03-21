/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.dataset;

import java.util.Calendar;
import org.apache.commons.lang.StringEscapeUtils;

import lu.lippmann.cdb.common.FormatterUtil;
import weka.core.Instance;


/**
 * InstanceFormatter.
 * 
 * @author the WP1 team
 */
public final class InstanceFormatter 
{
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private InstanceFormatter() {}
	
	
	//
	// Static methods
	//
	
	public static String htmlFormat(final Instance inst,final boolean withHTMLHeader)
	{
		final StringBuilder sb=new StringBuilder();
		if (withHTMLHeader) sb.append("<html><body>");		
		for (int i=0;i<inst.numAttributes();i++)
		{
			sb.append(StringEscapeUtils.escapeHtml(inst.attribute(i).name())).append(" = ");
			sb.append("<b>");
			if (inst.attribute(i).isNominal()||inst.attribute(i).isString())
			{
				sb.append(StringEscapeUtils.escapeHtml(inst.stringValue(i)));
			}
			else if (inst.attribute(i).isDate())
			{
				final Calendar cal=Calendar.getInstance();
				cal.setTimeInMillis((long)inst.value(i));						
				sb.append(FormatterUtil.DATE_FORMAT.format(cal.getTime()));
			}
			else if (inst.attribute(i).isNumeric())
			{
				sb.append(inst.value(i));
			}
			sb.append("</b>");
			sb.append("<br/>");
		}
		if (withHTMLHeader) sb.append("</body></html>");		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param inst
	 * @return
	 */
	public static String shortHtmlFormat(final Instance inst){
		return inst.toString();//.stringValue(0);
	}
}
