/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.mvp;

import java.awt.Component;


/**
 * Display.
 *  
 * @author the ACORA team
 */
public interface Display 
{	
	Component asComponent();
	void setEnabled(boolean enabled);
}
