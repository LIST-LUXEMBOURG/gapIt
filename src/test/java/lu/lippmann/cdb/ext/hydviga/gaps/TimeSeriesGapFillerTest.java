/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import static org.junit.Assert.*;
import java.io.File;

import lu.lippmann.cdb.ext.hydviga.gaps.*;
import lu.lippmann.cdb.ext.hydviga.gaps.GapFillerFactory.Algo;
import lu.lippmann.cdb.weka.*;
import org.junit.Test;
import weka.core.*;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class TimeSeriesGapFillerTest 
{	
	/*@Test
	public void testInterp() {test0("appleStocks2011-withmissing.arff",Algo.Interpolation);}*/

	/*@Test
	public void testEM() {test0("appleStocks2011-withmissing.arff",Algo.EM);}*/
	
	/*@Test
	public void testEM_WDT() {test0("appleStocks2011-withmissing.arff",Algo.EM_WITH_DISCR_TIME);}*/
	
	/*@Test
	public void testRegressions() {test0("appleStocks2011-withmissing-single.arff",Algo.REG);}
	
	@Test
	public void testRegressions_WDT() {test0("appleStocks2011-withmissing-single.arff",Algo.REG_WITH_DISCR_TIME);}*/
	
	@Test
	public void testM5P() {test0("appleStocks2011-withmissing-single.arff",Algo.M5P);}
	
	@Test
	public void testM5P_WDT() {test0("appleStocks2011-withmissing-single.arff",Algo.M5P_WITH_DISCR_TIME);}

	@Test
	public void testZeroR() {test0("appleStocks2011-withmissing-single.arff",Algo.ZeroR);}

	@Test
	public void testANN() {test0("appleStocks2011-withmissing-single.arff",Algo.ANN);}

	@Test
	public void testANN_WDT() {test0("appleStocks2011-withmissing-single.arff",Algo.ANN_WITH_DISCR_TIME);}

	
	private void test0(final String fn,final Algo algo)
	{
		try
		{
			final File f=new File(ClassLoader.getSystemResource(fn).getPath());
			assertNotNull(f);
			
			final Instances inst=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(f);
			assertNotNull(inst);
			assertNotEquals(0,WekaDataStatsUtil.getCountOfMissingValues(inst));
			
			final GapFiller gapFiller=GapFillerFactory.getGapFiller(algo);
			assertNotNull(gapFiller);
			
			final Instances correctedInst=gapFiller.fillGaps(inst);
			assertNotNull(correctedInst);
			assertEquals(0,WekaDataStatsUtil.getCountOfMissingValues(correctedInst));
			
			assertNotEquals("",correctedInst.attribute(WekaDataStatsUtil.getFirstDateAttributeIdx(correctedInst)).getDateFormat());
			
			final double mae=gapFiller.evaluateMAEByEnlargingGap(inst,100);
			assertTrue(mae>0d);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(algo+" -> "+e.getMessage());
		}	
	}
	


			
}
