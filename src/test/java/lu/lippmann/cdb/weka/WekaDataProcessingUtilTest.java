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
public class WekaDataProcessingUtilTest 
{	
	@Test
	public void testBuildFilteredInstance()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);

			final Instances filteredInst=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(inst,new int[]{1});
			assertNotNull(filteredInst);
			assertEquals(1,filteredInst.numAttributes());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testRenameAttribute()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);

			final Instances newinst=WekaDataProcessingUtil.renameAttribute(inst,0,"blabla");
			assertNotNull(newinst);
			
			assertEquals("blabla",newinst.attribute(0).name());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
