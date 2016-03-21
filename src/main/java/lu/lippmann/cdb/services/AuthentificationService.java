/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.services;

import lu.lippmann.cdb.models.CUser;


/**
 * AuthentificationService.
 *
 * @author Olivier PARISOT
 */
public interface AuthentificationService 
{
	CUser login(String login,String passwd) throws AuthentificationException;
	
	void logout(CUser user);
}
