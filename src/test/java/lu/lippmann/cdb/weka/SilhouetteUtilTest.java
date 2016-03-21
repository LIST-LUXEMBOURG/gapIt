/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import static org.junit.Assert.*;
import java.io.File;
import java.util.*;

import org.junit.Test;
import weka.clusterers.*;
import weka.core.Instances;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class SilhouetteUtilTest 
{	
	@Test
	public void testGeneral()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			inst.setClassIndex(-1);
			
			final SimpleKMeans kmeans2=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(2);
			final WekaClusteringResult cr2=WekaMachineLearningUtil.computeClusters(kmeans2,inst);			
			final Map<Integer,List<Double>> sil2=SilhouetteUtil.computeSilhouette(inst,cr2);
			assertNotNull(sil2);
			assertFalse(sil2.isEmpty());
			assertEquals(2,sil2.size());
			
			final SimpleKMeans kmeans3=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(3);
			final WekaClusteringResult cr3=WekaMachineLearningUtil.computeClusters(kmeans3,inst);			
			final Map<Integer,List<Double>> sil3=SilhouetteUtil.computeSilhouette(inst,cr3);
			assertNotNull(sil3);
			assertFalse(sil3.isEmpty());
			assertEquals(3,sil3.size());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
