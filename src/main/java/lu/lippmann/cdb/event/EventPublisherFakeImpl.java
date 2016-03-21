/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;


/**
 * EventPublisherFakeImpl.
 *
 * @author Olivier PARISOT
 */
public class EventPublisherFakeImpl implements EventPublisher
{
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void publish(AbstractEvent event) {}
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void markAsEventListener(final Object o) {}
}
