/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import lu.lippmann.cdb.weka.WekaDataProcessingUtil;
import weka.core.Instances;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class SupervisedFeatureSelectionTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Simplify (superv.)";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/simplify.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		if (dataSet.classIndex()==-1) throw new Exception("Need a selected class!");
		return WekaDataProcessingUtil.buildDataSetWithBestAttributes(dataSet);
	}
}
