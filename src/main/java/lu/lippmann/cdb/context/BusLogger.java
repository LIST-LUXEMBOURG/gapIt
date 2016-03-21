/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.context;

import java.util.logging.Logger;

import lu.lippmann.cdb.command.AbstractCommand;
import lu.lippmann.cdb.event.*;

import org.bushe.swing.event.annotation.*;
import com.google.inject.Inject;


/**
 * Log all events and commands on the bus.
 * 
 * @author Jerome WAX, Olivier PARISOT
 */
public final class BusLogger 
{
	@Inject
	private Logger logger;

	/**
	 * Constructor.
	 */
	@Inject
	public BusLogger(final EventPublisher eventPublisher) 
	{		
		eventPublisher.markAsEventListener(this);
	}

	//@EventListener
	@EventSubscriber
	public void onEventReceived(final AbstractEvent o) 
	{
		logger.info("=> EVENT: " + o.getClass().getSimpleName());
	}
	
	@EventSubscriber
	public void onCommandReceived(final AbstractCommand o) 
	{
		logger.info("=> COMMAND: " + o.getClass().getSimpleName());
	}
}
