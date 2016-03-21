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
public class WekaClusteringResultsComparisonTest 
{
	@Test
	public void testComputePairsProximityMatrix()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			inst.setClassIndex(-1);
			
			final SimpleKMeans kmeans=WekaMachineLearningUtil.buildSimpleKMeansClusterer();
			kmeans.buildClusterer(inst);
			final ClusterEvaluation eval=new ClusterEvaluation();
			eval.setClusterer(kmeans);
			eval.evaluateClusterer(inst);	
			
			final boolean[][] r=WekaClusteringResultsComparison.computePairsProximityMatrix(eval.getClusterAssignments());
			assertNotNull(r);			
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
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
			
			final SimpleKMeans kmeans1=WekaMachineLearningUtil.buildSimpleKMeansClustererWithManhattanDistance(2);
			final WekaClusteringResult cr1=WekaMachineLearningUtil.computeClusters(kmeans1,inst);
			
			final SimpleKMeans kmeans2=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(2);
			final WekaClusteringResult cr2=WekaMachineLearningUtil.computeClusters(kmeans2,inst);

			final WekaClusteringResultsComparison crComparison=new WekaClusteringResultsComparison(cr1,cr2);
			assertNotNull(crComparison);			
			assertNotNull(crComparison.toString());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testDiff()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("bank_test.csv").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromCSVFile(f,true);
			assertNotNull(inst);
			
			inst.setClassIndex(-1);
			
			final SimpleKMeans kmeans1=WekaMachineLearningUtil.buildSimpleKMeansClustererWithManhattanDistance(2);
			final WekaClusteringResult cr1=WekaMachineLearningUtil.computeClusters(kmeans1,inst);
			
			final SimpleKMeans kmeans2=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(2);
			final WekaClusteringResult cr2=WekaMachineLearningUtil.computeClusters(kmeans2,inst);

			final Instances diff=WekaClusteringResultsComparison.buildDiff(cr1,cr2);
			assertNotNull(diff);			
			assertEquals(inst.numInstances(),diff.numInstances());
			assertEquals(inst.numAttributes()+1,diff.numAttributes());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
