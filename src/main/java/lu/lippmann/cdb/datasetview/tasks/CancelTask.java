/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import lu.lippmann.cdb.datasetview.*;
import lu.lippmann.cdb.event.ErrorOccuredEvent;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class CancelTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Cancel";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/cancel.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void process(final IDatasetView iv)
	{
		try 
		{
			iv.notifyTransformation(null,null);
			iv.reinitDataCompleteness();
		} 
		catch (Exception e1) 
		{
			iv.getEventPublisher().publish(new ErrorOccuredEvent("Error when canceling transformations.",e1));
		}	
	}



}
