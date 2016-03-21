/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import javax.swing.JOptionPane;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class SortAttributesTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Sort attributes";
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
		
		final String s=(String)JOptionPane.showInputDialog(null,"Select an attribute:\n",
				"Attribute selection",
				JOptionPane.PLAIN_MESSAGE,
				null,
				WekaDataStatsUtil.getAttributeNames(dataSet).toArray(),
				"");
		
		if (s!=null) return WekaDataProcessingUtil.buildDataSetSortedByAttribute(dataSet,dataSet.attribute(s).index());
		else return dataSet;
	}

}
