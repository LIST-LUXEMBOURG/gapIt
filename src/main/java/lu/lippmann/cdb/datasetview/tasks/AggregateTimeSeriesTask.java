/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import javax.swing.*;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class AggregateTimeSeriesTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Aggregate by year/month/day";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/hierarchize.png"; // TODO: change icon
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		if (WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet)==-1)
		{
			throw new Exception("Only usable with time series!");
		}
					
		final JList<String> list=new JList<String>(WekaTimeSeriesUtil.FIELDS);
		JOptionPane.showMessageDialog(null, list, "Granularity", JOptionPane.PLAIN_MESSAGE);
		
		return WekaTimeSeriesUtil.changeGranularity(dataSet,list.getSelectedValuesList());		
	}

}
