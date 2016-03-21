/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.awt.Color;
import java.util.*;


/**
 * ArraysUtil.
 *
 * @author Olivier PARISOT
 */
public final class ArraysUtil 
{
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private ArraysUtil() {}
	
	
	//
	// Static methods
	//
	
	public static Color[] concat(final Color[] first,final Color[] second) 
	{ 
		final Color[] result=Arrays.copyOf(first, first.length + second.length); 
		System.arraycopy(second, 0, result, first.length, second.length); 
		return result; 
	}
	
	public static double[] concat(final double[] first,final double[] second) 
	{ 
		final double[] result=Arrays.copyOf(first, first.length + second.length); 
		System.arraycopy(second, 0, result, first.length, second.length); 
		return result; 
	}

	public static int[] concat(final int[] first,final int[] second) 
	{ 
		final int[] result=Arrays.copyOf(first, first.length + second.length); 
		System.arraycopy(second, 0, result, first.length, second.length); 
		return result; 
	}
	
	public static boolean contains(final int[] array,final int item)
	{
		for (int i=0;i<array.length;i++) if (array[i]==item) return true;
		return false;
	}
	
	public static int[] transform(final List<Integer> l)
	{
		final int[] array=new int[l.size()];
		for (int i=0;i<l.size();i++) array[i]=l.get(i).intValue();	
		return array;
	}

	public static double[] select(final double[] array,final int[] indexes) 
	{
		final double [] r=new double[indexes.length];
		int i=0;
		for (int k=0;k<indexes.length;k++)
		{
			r[i]=array[indexes[k]];
			i++;
		}		
		return r;
	}
	
	public static double[] remove(final double[] array,final int idx) 
	{
		final double [] r=new double[array.length-1];
		int i=0;
		for (int k=0;k<array.length;k++)
		{
			if (k!=idx)
			{
				r[i]=array[k];
				i++;
			}
		}		
		return r;
	}

	public static double[] lastValues(double[] array,final int maxValues) 
	{
		if (maxValues>array.length) throw new IllegalArgumentException();
		
		final double[] res=new double[maxValues];
		System.arraycopy(array,array.length-1-maxValues,res,0,maxValues);
		return res;
	}
	
	public static double[] firstValues(double[] array,final int maxValues) 
	{
		if (maxValues>array.length) throw new IllegalArgumentException();
		
		final double[] res=new double[maxValues];
		System.arraycopy(array,0,res,0,maxValues);
		return res;
	}
}
