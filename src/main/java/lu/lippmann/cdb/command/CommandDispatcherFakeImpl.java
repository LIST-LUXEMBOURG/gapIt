/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;


/**
 * Fake impl.
 */
public class CommandDispatcherFakeImpl implements CommandDispatcher
{		
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void dispatch(final AbstractCommand command) {}

	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void markAsCommandHandler(Object o) {}
}