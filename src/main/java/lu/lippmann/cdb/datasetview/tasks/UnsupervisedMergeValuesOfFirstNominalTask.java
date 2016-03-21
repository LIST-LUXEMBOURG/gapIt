/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class UnsupervisedMergeValuesOfFirstNominalTask extends Task
{
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Merge values of first nominal (unsuperv.)";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/numerize.png"; // TODO: change
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		final int[] idxs=WekaDataStatsUtil.getNominalAttributesIndexes(dataSet);
		if (idxs.length>0)
		{
			int idx=idxs[0];
			if (idx==dataSet.classIndex()&&idxs.length>1) idx=idxs[1];
			final Instances newds=WekaDataProcessingUtil.buildDataSetWithUnsupervisedMergeNominalValues(dataSet,idx);
			return newds;
		}
		else
		{
			return dataSet;
		}
	}
}
