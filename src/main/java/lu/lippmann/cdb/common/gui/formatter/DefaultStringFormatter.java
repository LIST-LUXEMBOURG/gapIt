/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.formatter;

/**
 * default implementation
 * @author wax
 *
 * @param <T>
 */
public class DefaultStringFormatter<T> implements StringFormatter<T> {

	private static final long serialVersionUID = -8160015553285244411L;

	@Override
	public String getStringFor(T object) {
		return object.toString();
	}
}
