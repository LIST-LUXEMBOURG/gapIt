/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;



/**
 * CommandDispatcher.
 *
 * @author Olivier PARISOT
 */
public interface CommandDispatcher 
{
	/**
	 * Dispatch command.
	 */
	void dispatch(final AbstractCommand command);
	
	/**
	 * Mak object as event listener.	 
	 */
	void markAsCommandHandler(Object o);
}
