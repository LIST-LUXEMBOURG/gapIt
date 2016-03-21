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
public final class SetAttributeAsTimestampTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Set attr as timestamp";
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
				WekaDataStatsUtil.getNumericAttributesNames(dataSet).toArray(),
				"");
		
		if (s!=null)
		{
			final Instances newds=new Instances(dataSet);
			newds.insertAttributeAt(new Attribute("date","dd-MM-yyyy HH:mm"),newds.numAttributes());
			
			final int sidx=newds.attribute(s).index();
			
			for (int i=0;i<newds.numInstances();i++)
			{
				newds.instance(i).setValue(newds.numAttributes()-1,newds.instance(i).value(sidx));
			}
			
			return newds;
		}
		else return dataSet;
	}

}
