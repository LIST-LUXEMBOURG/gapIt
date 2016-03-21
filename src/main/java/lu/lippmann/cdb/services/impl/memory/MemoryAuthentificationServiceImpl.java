/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.services.impl.memory;

import java.net.InetAddress;

import lu.lippmann.cdb.models.CUser;
import lu.lippmann.cdb.services.*;


/**
 * Memory implementation of AuthentificationService.
 *
 * @author Olivier PARISOT
 */
public class MemoryAuthentificationServiceImpl implements AuthentificationService 
{
	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public CUser login(final String login,final String passwd) throws AuthentificationException 
	{
		String hostName="not found?";
		try
		{
			hostName=InetAddress.getLocalHost().getHostName();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return new CUser(login,hostName);
	}

	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public void logout(final CUser user) {}

}
