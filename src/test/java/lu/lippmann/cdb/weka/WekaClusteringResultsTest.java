/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import weka.clusterers.*;
import weka.core.Instances;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class WekaClusteringResultsTest 
{	
	@Test
	public void testkmeans2()
	{
		try
		{
			testWithAClusterer(2,WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(2));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testkmeans3()
	{
		try
		{
			testWithAClusterer(3,WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(3));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	private void testWithAClusterer(final int k,final Clusterer kmeans1) throws Exception
	{
		final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
		assertNotNull(f);
		
		final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
		assertNotNull(inst);
		
		inst.setClassIndex(-1);
		
		final WekaClusteringResult cr=WekaMachineLearningUtil.computeClusters(kmeans1,inst);

		assertNotNull(cr);
		assertNotNull(cr.getAss());
		assertNotNull(cr.getClustersList());
		assertEquals(k,cr.getClustersList().size());
		
		for (int i=0;i<inst.numInstances();i++) 
		{	
			assertEquals(inst.instance(i).toString(),cr.getInstance(i).toString());
		}
	}

}
