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
public final class UnsupervisedDiscretizeFirstNumericTask extends Task
{
	//
	// Instance fields
	//
	
	/** */
	final int n;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.	 
	 */
	public UnsupervisedDiscretizeFirstNumericTask(final int n)
	{
		this.n=n;
	}
	
	
	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Discretize first numeric ("+n+",unsuperv.)";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/discretize.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		final int[] idxs=WekaDataStatsUtil.getNumericAttributesIndexesAsArray(dataSet);
		if (idxs.length>0)
		{
			return WekaDataProcessingUtil.buildDiscretizedDataSetUnsupervised(dataSet,idxs[0],n);
		}
		else
		{
			return dataSet;
		}
	}
}
