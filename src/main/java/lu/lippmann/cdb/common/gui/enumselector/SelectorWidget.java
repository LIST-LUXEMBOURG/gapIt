/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.enumselector;

import java.util.List;

import lu.lippmann.cdb.common.gui.formatter.StringFormatter;
import lu.lippmann.cdb.common.mvp.*;

/**
 * default interface for a selector widget
 *
 * @author wax
 *
 * @param <T>
 */
interface SelectorWidget<T> extends Display {
	void setChoices(List<T> choices);

	void set(T obj);

	T get();

	void addOnSelectListener(Listener<T> listener);
	
	/**
	 * define formatter of string
	 * @param stringFormatter
	 */
	void setStringFormatter(StringFormatter<T> stringFormatter);
}
