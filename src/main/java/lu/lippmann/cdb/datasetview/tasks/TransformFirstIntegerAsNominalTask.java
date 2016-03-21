/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.util.List;

import lu.lippmann.cdb.weka.*;
import weka.core.Instances;

/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class TransformFirstIntegerAsNominalTask extends Task 
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
		return "Transform first integer as nominal";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/nominalize.png"; // TODO: change
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		final List<Integer> l=WekaDataStatsUtil.getIntegerAttributesIndexes(dataSet);
		if (l.size()>0)
		{
			final int idx=l.get(0);
			final Instances newds=WekaDataProcessingUtil.buildNominalizedDataSet(dataSet,new int[]{idx});
			return newds;
		}
		else
		{
			return dataSet;
		}
	}
}
