/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;


/**
 * EventPublisher.
 *
 * @author Olivier PARISOT
 */
public interface EventPublisher 
{
	/**
	 * Publish an event.	 
	 */
	void publish(AbstractEvent event);
	
	/**
	 * Mak object as event listener.	 
	 */
	void markAsEventListener(Object o);
}
