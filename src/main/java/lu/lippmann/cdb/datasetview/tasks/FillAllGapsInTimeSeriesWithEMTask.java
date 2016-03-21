/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import lu.lippmann.cdb.ext.hydviga.gaps.GapFillerEM;
import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class FillAllGapsInTimeSeriesWithEMTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Fill all gaps with EM";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/replace-missing.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		final Instances wrv=new GapFillerEM(true).fillGaps(dataSet);		
		return WekaTimeSeriesUtil.buildMergedDataSet(dataSet,WekaTimeSeriesUtil.buildDiff(dataSet,wrv));		
	}
}
