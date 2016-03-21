/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class FillShortGapsInTimeSeriesByInterpolationTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Fill short gaps by interpolation";
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
		final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
		if (dateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		Instances newds=dataSet;
		for (int i=0;i<dataSet.numAttributes();i++)
		{
			System.out.println("Fill gaps for "+i);
			
			if (i==dateIdx) continue;
			final java.util.List<double[]> gaps=WekaTimeSeriesUtil.findGaps(newds,i);
			for (final double[] dd:gaps)
			{	
				if (dd[3]>1) continue;
				newds=WekaTimeSeriesUtil.fillGapWithInterpolation(newds,newds.attribute(i),(int)dd[2],(int)dd[3],1);
			}
		}
		
		return newds;
	}
}
