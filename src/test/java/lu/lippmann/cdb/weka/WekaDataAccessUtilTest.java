/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class WekaDataAccessUtilTest 
{
	
	@Test
	public void testBuildInstancesFromCSVFile()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());			
			
			assertNotNull(WekaDataAccessUtil.loadInstancesFromCSVFile(f,true));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildInstancesFromARFFOrCSVFile()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());			
			
			assertNotNull(WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildInstancesFromARFFOrCSVFile2()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.arff").getPath());			
			
			assertNotNull(WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildInstancesFromCSVString()
	{
		try
		{
			WekaDataAccessUtil.loadInstancesFromCSVString("f,e,u,r\n1,2,3,4\n",true);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
