/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * TimeSeriesGapFillerInterpolation.
 * 
 * @author Olivier PARISOT
 */
public class GapFillerInterpolation extends GapFiller 
{
	//
	// Constructors
	//
	
	/**
	 * Constructor.	 
	 */
	GapFillerInterpolation(final boolean wdt) 
	{
		super(wdt);		
	}

	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances fillGaps0(Instances newds) throws Exception 
	{
		return WekaTimeSeriesUtil.fillAllGapsWithInterpolation(newds);
	}

}
