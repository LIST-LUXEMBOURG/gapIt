/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.commandlist;

import lu.lippmann.cdb.common.mvp.*;

/**
 * 
 * @author heinesch
 * 
 */
public interface CommandList extends Display {
	/**
	 * Adds a listener to the buttons of the commandlist. The listeners are
	 * notified which button was pressed by passing the {@link CommandButton}
	 * that is associated with it.
	 * 
	 * @param listener
	 */
	void addListener(Listener<CommandButton> listener);
}
