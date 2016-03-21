/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;


/**
 * EventPublisherBushImpl.
 *
 * @author Olivier PARISOT
 */
public class EventPublisherBushImpl implements EventPublisher
{
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void publish(final AbstractEvent event)
	{
		EventBus.publish(event);
	}
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void markAsEventListener(final Object o) 
	{
		//System.out.println("EventPublisherBushImpl.markAsEventListener(): "+o);
		AnnotationProcessor.process(o);		
	}
}
