/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.io.File;
import javax.swing.JFileChooser;

import lu.lippmann.cdb.datasetview.*;
import lu.lippmann.cdb.event.ErrorOccuredEvent;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class ExportARFFTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Export ARFF";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/export-arff.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(final IDatasetView iv)
	{
		final JFileChooser fc=new JFileChooser("./test.arff");
		fc.showSaveDialog(iv.asComponent());
		final File file=fc.getSelectedFile();
		if (file!=null)
		{
			try 
			{
				WekaDataAccessUtil.saveInstancesIntoARFFFile(iv.getDataSet(),file);
			} 
			catch (Exception e1) 
			{											
				iv.getEventPublisher().publish(new ErrorOccuredEvent("Error during '"+getName()+"' task.",e1));
			}
		}	
	}



}
