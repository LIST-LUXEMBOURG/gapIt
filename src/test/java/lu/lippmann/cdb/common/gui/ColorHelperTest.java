/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import static org.junit.Assert.*;
import java.awt.Color;
import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class ColorHelperTest 
{	
	@Test
	public void testMDS()
	{
		try
		{
			for (int i=0;i<10;i++)
			{
				final Color c=ColorHelper.getRandomBrightColor();
				assertNotNull(c);
			}
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}

}
