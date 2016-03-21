/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.*;


/**
 * MathsUtil.
 *
 * @author Olivier PARISOT
 */
public final class MathsUtil 
{
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private MathsUtil() {}
	
	
	//
	// Static fields
	//

	public static double mean(final double[] expected,final int begin,final int end)
	{
		int trueN=0;
		double mean=0d;
		for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
		{
			mean+=expected[i];		
			trueN++;
		}
		
		if (trueN==0) throw new IllegalStateException();

		mean/=(double)trueN;
		
		return mean;
	}
	
	public static double indexOfAgreement(final double[] expected,final double[] predicted,final int begin,final int end)
	{
		if (expected.length!=predicted.length) throw new IllegalArgumentException();
		if (expected.length==0) throw new IllegalArgumentException();

		final double mse=mse(expected,predicted,begin,end);
		
		final double mean=mean(expected,begin,end);
		
		double potentialError=0d;
		for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
		{
			final double val=Math.abs(predicted[i]-mean)+Math.abs(expected[i]-mean);
			potentialError+=val*val;
		}
		
		return 1d-(mse/potentialError);
	}
	
	public static double nashSutcliffe(final double[] expected,final double[] predicted,final int begin,final int end)
	{
		if (expected.length!=predicted.length) throw new IllegalArgumentException();
		if (expected.length==0) throw new IllegalArgumentException();
		
		final double mean=mean(expected,begin,end);
		
		double sumDiffs=0d;
		double sumDiffsMean=0d;
		for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
		{
			final double diff=(expected[i]-predicted[i]);			
			sumDiffs+=diff*diff;

			final double diffMean=(expected[i]-mean);			
			sumDiffsMean+=diffMean*diffMean;
		}
		
		if (sumDiffs<=0d&&sumDiffsMean<=0d) 
		{
			System.out.println("nashSutcliffe: expected and predicted series only contain zero values, so ns=1");
			return 1d;
		}
		
		return 1d-(sumDiffs/sumDiffsMean);
	}	
	
	public static double rmse(final double[] predicted,final double[] expected,final int begin,final int end) 
	{
		if (predicted.length!=expected.length) throw new IllegalArgumentException();
		
		int trueN=0;
        double rmse=0d;        
        for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
        {
        	final double diff=expected[i]-predicted[i];
            rmse+=diff*diff;
            trueN++;
        }
        rmse=Math.sqrt(rmse/trueN);
        return rmse;
	}

	public static double mse(final double[] predicted,final double[] expected,final int begin,final int end) 
	{
		if (predicted.length!=expected.length) throw new IllegalArgumentException();
		
        double mse=0d;        
        for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
        {
        	final double diff=expected[i]-predicted[i];
            mse+=diff*diff;
        }
        return mse;
	}
	
	public static double rsr(final double[] predicted,final double[] expected,final int begin,final int end) 
	{
		final double rmse=rmse(predicted,expected,begin,end);		
        final double std=std(expected,begin,end);
        //System.out.println("std -> "+std);
		return rmse/std;
	}
	
	public static double pbias(final double[] predicted,final double[] expected,final int begin,final int end) 
	{
		double sumDiff=0d;
		double sumExpected=0d;
        for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
        {
        	sumDiff+=(expected[i]-predicted[i])*100d;
        	sumExpected+=expected[i];
        }                
		return sumDiff/sumExpected;
	}
	
	public static double mae(final double[] expected,final double[] predicted,final int begin,final int end)
	{
		int trueN=0;		
		double sumErr=0d;
		for (int i=Math.max(0,begin);i<Math.min(expected.length-1,end);i++) 
		{
			final double diff=Math.abs(expected[i]-predicted[i]);
			sumErr+=diff;
			trueN++;
		}
		return sumErr/trueN;
	}	
	
	public static double std(final double[] x,final int begin,final int end)
	{
		int N=0;
		double mean=0d;
		for (int i=Math.max(0,begin);i<Math.min(x.length-1,end);i++)
		{
			mean+=x[i];
			N++;
		}		
		mean/=N;
		
		double std=0d;
		for (int i=Math.max(0,begin);i<Math.min(x.length-1,end);i++)
		{
			std+=(x[i]-mean)*(x[i]-mean);
		}
		
		return Math.sqrt(std/N);
	}
	
	public static double std(final double[] x)
	{
		final int N = x.length;

		final double avg=avg(x);
		
		double std = 0;
		for(int i = 0 ; i < N; i++)
		{
			std +=(x[i]-avg)*(x[i]-avg);
		}
		
		return Math.sqrt(std/N);
	}
	
    public static double distance(final double[] t1,final double[] t2)
    {
    	if (t1.length!=t2.length) throw new IllegalStateException("distance() -> "+t1.length+"<>"+t2.length);
    	double sum=0d;
    	for (int i=0;i<t1.length;i+=1)
    	{
    		sum+=(t1[i]-t2[i])*(t1[i]-t2[i]);
    	}
    	return Math.sqrt(sum);
    }
    
    public static void normalize(final double[] doubles) 
    {
    	final int len=doubles.length;
    	double min=Double.MAX_VALUE;
    	double max=Double.MIN_VALUE;  	
		for (int i=0;i<len;i++)
		{
			if (doubles[i]<min) min=doubles[i];
			if (doubles[i]>max) max=doubles[i];
		}
        for (int i = 0; i < len; i++) 
        {	
        	doubles[i]=(doubles[i]-min)/max;
        }
    }
    
    public static double sum(final double... values) 
    {
    	double result=0d;
    	for (final double value:values) result += value;
    	return result;
    }
    
    public static long sum(final long... values) 
    {
    	long result=0;
    	for (final long value:values) result += value;
    	return result;
    }

	public static double[] replace(final double[] doubleArray,final int classIndex,final int i) 
	{		
		doubleArray[classIndex]=i;
		return doubleArray;
	}

	public static double avg(final double... values) 
	{		
		return sum(values)/values.length;
	}

	public static double median(final double... values)
	{
		final double[] nvalues=new double[values.length];
		System.arraycopy(values, 0, nvalues, 0, values.length);
		Arrays.sort(nvalues);
		final int middle = ((nvalues.length) / 2);
		final double median;
		if(nvalues.length % 2 == 0)
		{
			final double medianA = nvalues[middle];
			final double medianB = nvalues[middle-1];
			median = (medianA + medianB) / 2;
		} 
		else
		{
			median = nvalues[middle + 1];
		}
		return median;
	}
	
	/**
	 * Source: the 68/95/99.7 rule (see wikipedia).
	 */
	public static boolean isOutlier(final double[] vals,final int index)
	{		
		final double min=avg(vals)-2*std(vals);
		final double max=avg(vals)+2*std(vals);
		return (vals[index]<min)||(vals[index]>max);
	}
	
	public static boolean eq(final double a,final double b) 
	{
		return (Math.abs(a-b))<1e-8;
	}
	
	public static double rmse(final List<Double> expected,final List<Double> computed)
	{
		if (expected==null) throw new IllegalArgumentException();
		if (computed==null) throw new IllegalArgumentException();		
		
		final int s=expected.size();
		if (s!=computed.size()) throw new IllegalArgumentException();
		
		final double[] expectedArray=new double[s];
		final double[] computedArray=new double[s];
		
		for (int i=0;i<s;i++)
		{
			expectedArray[i]=expected.get(i);
			computedArray[i]=computed.get(i);
		}
				
		return MathsUtil.rmse(computedArray,expectedArray,0,s);
	}
	
	public static double mae(final List<Double> expected,final List<Double> computed)
	{
		if (expected==null) throw new IllegalArgumentException();
		if (computed==null) throw new IllegalArgumentException();
		
		final int s=expected.size();
		if (s!=computed.size()) throw new IllegalArgumentException();
		
		final double[] expectedArray=new double[s];
		final double[] computedArray=new double[s];
		
		for (int i=0;i<s;i++)
		{
			expectedArray[i]=expected.get(i);
			computedArray[i]=computed.get(i);
		}
				
		return MathsUtil.mae(computedArray,expectedArray,0,s);
	}	
}
