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
public final class RemoveOutliersTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Remove outliers";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/remove-outliers.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		return WekaDataProcessingUtil.buildDataSetWithoutOutliers(dataSet);
	}
}
