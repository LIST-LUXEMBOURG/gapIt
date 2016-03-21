/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import static org.junit.Assert.*;
import java.io.File;
import java.util.List;
import org.junit.Test;
import weka.core.*;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class WekaMachineLearningUtilTest 
{
	@Test
	public void testBuildEMClusterer()
	{
		try
		{
			assertNotNull(WekaMachineLearningUtil.buildEMClusterer());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildSimpleKMeansClusterer()
	{
		try
		{
			assertNotNull(WekaMachineLearningUtil.buildSimpleKMeansClusterer());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildJ48Classifier()
	{
		try
		{
			assertNotNull(WekaMachineLearningUtil.buildJ48Classifier(true,2,3,0.2d));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeCentroid()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);

			assertNotNull(WekaMachineLearningUtil.computeCentroid(true,new EuclideanDistance(inst),inst));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeKNN()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			final Instances nn=WekaMachineLearningUtil.computeNearestNeighbours(inst,inst.instance(0),2);
			assertNotNull(nn);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeManualKNN()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("example.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			final Instances nn=WekaMachineLearningUtil.computeManualNearestNeighbours(inst, inst.instance(0),2);
			assertNotNull(nn);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeClustersAssignment()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			final List<Instances> clustersList=WekaMachineLearningUtil.computeClusters(WekaMachineLearningUtil.buildSimpleKMeansClustererWithManhattanDistance(3),inst).getClustersList();
			assertNotNull(clustersList);
			
			final Instances clustersAssignement=WekaMachineLearningUtil.buildDataSetExplainingClustersAssignment(clustersList,"cluster",true);
			assertNotNull(clustersAssignement);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeAttributesEvaluation()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			assertNotNull(WekaMachineLearningUtil.computeInfoGainEvaluation(inst));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testComputeUnsupervisedFeaturesSelection()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			testComputeUnsupervisedFeaturesSelection0(inst,2);
			testComputeUnsupervisedFeaturesSelection0(inst,3);
			testComputeUnsupervisedFeaturesSelection0(inst,4);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}

	private void testComputeUnsupervisedFeaturesSelection0(final Instances inst,final int k) throws Exception 
	{
		final List<Integer> usupervisedFS=WekaMachineLearningUtil.computeUnsupervisedFeaturesSelection(inst,k);
		assertNotNull(usupervisedFS);
		assertEquals(k,usupervisedFS.size());
	}

}
