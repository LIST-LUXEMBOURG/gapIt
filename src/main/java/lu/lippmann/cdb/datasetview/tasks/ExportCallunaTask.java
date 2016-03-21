/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.io.*;

import javax.swing.JFileChooser;

import lu.lippmann.cdb.datasetview.*;
import lu.lippmann.cdb.event.ErrorOccuredEvent;
import lu.lippmann.cdb.weka.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class ExportCallunaTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Export for Calluna";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/export-calluna.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(final IDatasetView iv)
	{
		final JFileChooser fc=new JFileChooser("./test.csv");
		fc.showSaveDialog(iv.asComponent());
		final File file=fc.getSelectedFile();
		if (file!=null)
		{
			try 
			{
				final BufferedWriter out=new BufferedWriter(new FileWriter(file));							
				out.write(WekaDataProcessingUtil.buildDataSetInStringFormatForCalluna(iv.getDataSet()));
				out.close();
			} 
			catch (Exception e1) 
			{											
				iv.getEventPublisher().publish(new ErrorOccuredEvent("Error during '"+getName()+"' task.",e1));
			}
		}	
	}



}
