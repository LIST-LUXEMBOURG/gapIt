/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import static org.junit.Assert.*;
import java.io.File;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;
import org.junit.Test;
import weka.core.Instances;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class MDSTest 
{
	private MDSResult buildMDSResult() throws Exception
	{
		final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
		assertNotNull(f);
		
		final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
		assertNotNull(inst);
								
		final MDSResult mdsResult=ClassicMDS.doMDS(inst,MDSDistancesEnum.EUCLIDEAN,2,500,true,false);
		assertNotNull(mdsResult);
		assertNotNull(mdsResult.getCoordinates());	
		
		return mdsResult;
	}
	
	@Test
	public void testMDS()
	{
		try
		{
			buildMDSResult();					
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testMDSKruskallStress()
	{
		try
		{
			final MDSResult mdsResult=buildMDSResult();
			final double ks=ClassicMDS.getKruskalStressFromMDSResult(mdsResult);
			assertTrue(ks>0);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
