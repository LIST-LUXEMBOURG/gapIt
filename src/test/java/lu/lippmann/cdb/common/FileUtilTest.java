/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import static org.junit.Assert.*;
import lu.lippmann.cdb.common.FileUtil;

import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class FileUtilTest 
{
	@Test
	public void testgetFileContent()
	{
		try
		{
			final StringBuilder sb=FileUtil.getFileContent("test.xml");
			assertNotNull(sb);
			assertTrue(sb.length()>0);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testgetFileContentEmpty()
	{
		try
		{
			final StringBuilder sb=FileUtil.getFileContent("empty.xml");
			assertNotNull(sb);
			assertTrue(sb.length()==0);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
