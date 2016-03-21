/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.enumselector;

import java.util.List;


/**
 * 
 * @author
 *
 * @param <T>
 */
interface ToggleButtonWidget<T> extends SelectorWidget<T> {
	
	/**
	 * Sets the item's status to the given select status
	 * @param item
	 * @param select
	 */
	void set(T item, boolean select);
	
	void setSelectable(boolean selectable);
	
	List<T> getChoices();
	
	void setMultiChoice(boolean enable);

	T findItemByText(String text);
}
