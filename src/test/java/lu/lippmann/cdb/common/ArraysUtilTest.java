/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;


/**
 * Tests class.
 *
 * @author Olivier PARISOT
 */
public class ArraysUtilTest 
{
	@Test
	public void testconcat()
	{
		try
		{
			final double[] val=ArraysUtil.concat(new double[]{1d,2d},new double[]{3d,4d});
			assertEquals(4,val.length);
			assertEquals(1d,val[0],0.00001d);
			assertEquals(2d,val[1],0.00001d);
			assertEquals(3d,val[2],0.00001d);
			assertEquals(4d,val[3],0.00001d);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testContains()
	{
		try
		{
			final int[] val=new int[]{1,2};
			assertTrue(ArraysUtil.contains(val,1));
			assertTrue(ArraysUtil.contains(val,2));
			assertFalse(ArraysUtil.contains(val,3));
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testSelect()
	{
		try
		{
			final double[] val=new double[]{1,2,3,4};
			final int[] indexes=new int[]{0,3};			
			final double[] select=ArraysUtil.select(val,indexes);
			assertEquals(2,select.length);
			assertEquals(1d,select[0],0.0001d);
			assertEquals(4d,select[1],0.0001d);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
	
	@Test
	public void testTransform()
	{
		try
		{
			final List<Integer> l=Arrays.asList(new Integer[]{1,2});
			final int[] val=ArraysUtil.transform(l);
			assertEquals(2,val.length);
			assertEquals(1,val[0]);
			assertEquals(2,val[1]);			
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			fail(e.getMessage());
		}					
	}
}
