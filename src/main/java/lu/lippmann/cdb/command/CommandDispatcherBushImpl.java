/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import java.lang.reflect.Method;
import java.util.*;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.*;


/**
 * CommandDispatcher Bush impl.
 * 
 * @author Olivier PARISOT
 */
public final class CommandDispatcherBushImpl implements CommandDispatcher
{	
	/** */
	private final Map<Class<?>,Object> map=new HashMap<Class<?>,Object>(); 
	
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void dispatch(final AbstractCommand command)
	{
		EventBus.publish(command);
	}
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void markAsCommandHandler(final Object objAsCommandHandler) 
	{	
		boolean hasMethodForCommandHandling=false;
		for (final Method m:objAsCommandHandler.getClass().getMethods())
		{
			final EventSubscriber ch=m.getAnnotation(EventSubscriber.class);
			if (ch!=null&&ch.eventClass().getInterfaces()[0].equals(AbstractCommand.class))
			{
				if (map.containsKey(ch.eventClass())) 
				{	
					throw new IllegalStateException("More than one handler for "+ch.eventClass());
				}
				else 
				{	
					map.put(ch.eventClass(),objAsCommandHandler);
					System.out.println("CommandDispatcherBushImpl.markAsCommandHandler(): "+objAsCommandHandler+" "+" "+ch.eventClass());
					hasMethodForCommandHandling=true;
				}
			}			
		}
		if (!hasMethodForCommandHandling) 
		{	
			throw new IllegalStateException("Try to mark class as command handler, but no method to handle command! -> "+objAsCommandHandler);
		}
		AnnotationProcessor.process(objAsCommandHandler);				
	}
	
}