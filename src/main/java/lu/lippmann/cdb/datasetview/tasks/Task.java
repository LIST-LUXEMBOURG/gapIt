/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.awt.event.ActionEvent;
import javax.swing.*;

import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.datasetview.*;
import lu.lippmann.cdb.event.ErrorOccuredEvent;
import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * Abstract Task.
 * 
 * @author the WP1 team
 */
public abstract class Task 
{
	//
	// Instance methods
	//
	void process(final IDatasetView iv)
	{
		new AbstractSimpleAsync<Void>(true) 
		{
			@Override
			public Void execute() throws Exception 
			{
				final Instances changedDataSet=process0(iv.getDataSet());
				if(iv.isComputingOfDataCompletnessEnabled())
				{
					final int dc=computeDataCompleteness(iv.getDataCompleteness(),iv.getInitialDataSet(),changedDataSet);
					iv.notifyTransformation(changedDataSet,"'"+getName()+"' done");			
					iv.setDataCompleteness(dc);
				}
				else
				{
					iv.notifyTransformation(changedDataSet,"'"+getName()+"' done");			
				}
				return null;
			}

			@Override
			public void onSuccess(Void result) {}

			@Override
			public void onFailure(Throwable caught) 
			{
				iv.getEventPublisher().publish(new ErrorOccuredEvent("Error during '"+getName()+"' task.",caught));

			}			
		}.start();	
	}

	public final AbstractAction buildAction(final IDatasetView iv)
	{
		if (iv==null) throw new IllegalStateException();

		return new AbstractAction() 
		{
			/** */
			private static final long serialVersionUID=1996739L;

			{
				putValue(Action.NAME, getName());				
				/*final String iconPath=getIconPath();
				System.out.println(iconPath);
				final Icon cachedIcon=ResourceLoader.getAndCacheIcon(iconPath);
				putValue(Action.SMALL_ICON, cachedIcon);*/					
			}

			public void actionPerformed(ActionEvent e) 
			{
				process(iv);
			}
		};
	}

	Instances process0(final Instances dataset) throws Exception 
	{
		return dataset;
	}

	int computeDataCompleteness(final int currentDataCompleteness,final Instances initial,final Instances after)
	{
		return new CompletenessComputer(initial).computeUnchangedCellsCount(after);
	}


	//
	// Abstract methods
	//

	abstract String getName();
	abstract String getIconPath();
}
