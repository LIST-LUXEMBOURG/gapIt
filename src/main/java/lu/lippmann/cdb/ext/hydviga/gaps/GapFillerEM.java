/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import lu.lippmann.cdb.weka.WekaDataProcessingUtil;
import weka.core.Instances;


/**
 * TimeSeriesGapFillerEM.
 * 
 * @author Olivier PARISOT
 *
 */
public class GapFillerEM extends GapFiller 
{
	//
	// Constructors
	//
	
	/**
	 * Constructor.	 
	 */
	public GapFillerEM(final boolean wdt) 
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
	Instances fillGaps0(final Instances ds) throws Exception 
	{
		return WekaDataProcessingUtil.buildDataSetWithMissingValuesReplacedUsingEM(ds);
	}

}
