/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import weka.core.*;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class WekaDataStatsUtilTest 
{
	@Test
	public void testIsInteger()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			assertFalse(WekaDataStatsUtil.isInteger(inst, inst.attribute("income").index()));
			assertTrue(WekaDataStatsUtil.isInteger(inst, inst.attribute("children").index()));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}

}
