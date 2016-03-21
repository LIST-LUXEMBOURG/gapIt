/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.*;


/**
 * Combination.
 * 
 * @author Olivier PARISOT
 */
public final class Combination 
{
	//
	// Instance fields
	//
	
	/** */
	private final int n;
	/** */
	private final int p; 		 
	/** */  
	private final int[] array;
	/** */
	private final List<int[]> result;
	 
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public Combination(final int n,final int p)
	{
		this.n=n;
		this.p=p;
		this.array=new int[p];
		this.result=new ArrayList<int[]>();
		
		compute(0);
	}
	
	
	//
	// Instance methods
	//	
	
	private void compute(final int index) 
	{
		if (index>=p) 
		{
			result.add(Arrays.copyOf(this.array,this.array.length));
			return;
		}
	 
		final int start=0;	 
		for(int i=start;i<n;i++) 
		{
			array[index]=i;
			compute(index+1);
		}
	}
	 
	public List<int[]> getResult()
	{
		return result;
	}
	
	
	//
	// Static methods
	//
	
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(final String[] args) 
	{
		for (final int[] array:new Combination(8,4).getResult())
		{
			System.out.println(FormatterUtil.buildStringFromArrayOfIntegers(array));
		}
	}

}
