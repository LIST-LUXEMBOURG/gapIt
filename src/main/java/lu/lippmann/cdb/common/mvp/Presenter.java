/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.mvp;

/**
 * 
 * @author
 * 
 * @param <D>
 */
public interface Presenter<D extends Display> {
	void init();
	D getDisplay();
}
