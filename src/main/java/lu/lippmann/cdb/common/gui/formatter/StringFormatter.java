/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.formatter;

import java.io.Serializable;

/**
 * Transform an object into string
 * @author wax
 *
 * @param <T>
 */
public interface StringFormatter<T> extends Serializable {
	String getStringFor(T object);
}
