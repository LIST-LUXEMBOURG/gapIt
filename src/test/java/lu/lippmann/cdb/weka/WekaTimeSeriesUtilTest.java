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
import weka.core.*;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class WekaTimeSeriesUtilTest 
{	
	@Test
	public void testBuildDataSetWithDiscretizedTime()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			
			final Instances filteredInst=WekaTimeSeriesUtil.buildDataSetWithDiscretizedTime(inst);
			assertNotNull(filteredInst);
			assertTrue(filteredInst.numAttributes()>inst.numAttributes());
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testchangeGranularity()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			
			System.out.println(inst);
			
			final String[] arr=new String[]{WekaTimeSeriesUtil.YEAR,WekaTimeSeriesUtil.QUARTER};
			final Instances filteredInst=WekaTimeSeriesUtil.changeGranularity(inst,Arrays.asList(arr));
			assertNotNull(filteredInst);
			
			System.out.println(filteredInst);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildMergedDataSet()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			
			final Instances mergedInst=WekaTimeSeriesUtil.buildMergedDataSet(inst,inst);
			assertNotNull(mergedInst);
			assertEquals(2*inst.numAttributes()-1,mergedInst.numAttributes());
			
			//System.out.println(mergedInst);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testBuildClusteredDataSet()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			
			final Instances clusteredInst=WekaTimeSeriesUtil.buildClusteredDataSet(inst,2,"cc");
			assertNotNull(clusteredInst);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testsplit()
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			
			final Instances clusteredInst=WekaTimeSeriesUtil.buildClusteredDataSet(inst,2,"cc");
			assertNotNull(clusteredInst);
			//System.out.println(clusteredInst);
			
			final List<double[]> l=WekaTimeSeriesUtil.split(clusteredInst,clusteredInst.numAttributes()-1);
			assertNotNull(l);
			assertTrue(l.size()>0);
			
			final Calendar cal=Calendar.getInstance();
			for (final double[] dd:l) 
			{	
				cal.setTimeInMillis((long)dd[0]);
				final Date start=cal.getTime();
				cal.setTimeInMillis((long)dd[1]);
				final Date end=cal.getTime();
				System.out.println(start+" -> "+end);
			}
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testdiff()
	{
		try
		{
			final File f_full=new File(ClassLoader.getSystemResource("appleStocks2011.arff").getPath());
			assertNotNull(f_full);
			
			final Instances inst_full=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f_full);
			assertNotNull(inst_full);
			
			final File f_withmissing=new File(ClassLoader.getSystemResource("appleStocks2011-withmissing.arff").getPath());
			assertNotNull(f_withmissing);
			
			final Instances inst_withmissing=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f_withmissing);
			assertNotNull(inst_withmissing);
			
			final Instances diff=WekaTimeSeriesUtil.buildDiff(inst_withmissing,inst_full);
			assertNotNull(diff);
			assertNotEquals(inst_full.numAttributes()*inst_full.numInstances()-WekaDataStatsUtil.getCountOfMissingValues(inst_withmissing),WekaDataStatsUtil.getCountOfMissingValues(diff));		
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}						
	}
			
}
