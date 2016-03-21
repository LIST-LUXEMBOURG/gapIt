/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import static org.junit.Assert.*;
import lu.lippmann.cdb.DefaultModule;

import org.junit.Test;
import com.google.inject.Guice;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class DefaultModuleTest 
{
	@Test
	public void testgetFileContent()
	{
		try
		{
			assertNotNull(Guice.createInjector(new DefaultModule()));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
