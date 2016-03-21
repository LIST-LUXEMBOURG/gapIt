/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import java.util.*;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * TimeSeriesGapFiller.
 *  
 * @author Olivier PARISOT
 */
public abstract class GapFiller
{
	//
	// Instance fields
	//
	
	/** */
	private final boolean wdt;
	/** */
	protected String model;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.	
	 */
	GapFiller(final boolean wdt)
	{
		this.wdt=wdt;
	}
	
	
	//
	// Instance methods
	//
	
	public boolean hasExplicitModel()
	{
		return false;
	}
	
	public final String getModel()
	{
		if (this.model==null) throw new IllegalStateException("Please fill gaps with a dataset before calling this method!");
		return this.model;
	}
	
	public final Instances fillGaps(final Instances ds) throws Exception
	{
		if (wdt) return fillAllGapsWithDiscretizedTime(ds);
		else return fillAllGaps(ds);
	}
	
	private Instances fillAllGaps(final Instances ds) throws Exception
	{
		Instances newds=new Instances(ds);			

		final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(newds);
		final String datename=newds.attribute(firstDateIdx).name();
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
				
		/* add a 'fake numerical' time field */
		newds.insertAttributeAt(new Attribute(datename+"_fake"),newds.numAttributes());		
		for (int i=0;i<newds.numInstances();i++)
		{
			newds.instance(i).setValue(newds.numAttributes()-1,newds.instance(i).value(firstDateIdx));
		}		
		
		/* remove the 'true' time field */
		newds.deleteAttributeAt(firstDateIdx);
		
		/* process the dataset */
		newds=fillGaps0(newds);
		
		/* re-add the 'true' time field according to the 'fake numerical' time field */
		final String df=ds.attribute(firstDateIdx).getDateFormat();
		newds.insertAttributeAt(new Attribute(datename+"_new",df),newds.numAttributes());		
		for (int i=0;i<newds.numInstances();i++)
		{
			newds.instance(i).setValue(newds.numAttributes()-1,newds.instance(i).value(newds.numAttributes()-2));
		}	
		
		/* delete the 'fake numerical' time field */
		newds.deleteAttributeAt(newds.numAttributes()-2);		
		
		newds.sort(newds.numAttributes()-1);
		
		return newds;
	}
	
	private Instances fillAllGapsWithDiscretizedTime(final Instances ds) throws Exception
	{
		int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(ds);
		final String datename=ds.attribute(firstDateIdx).name();
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		Instances newds=new Instances(ds);
		
		/* add discretized time */
		newds=WekaTimeSeriesUtil.buildDataSetWithDiscretizedTime(newds);
		
		/* add fake numerical time */
		newds.insertAttributeAt(new Attribute(datename+"_fake"),newds.numAttributes());		
		for (int i=0;i<newds.numInstances();i++)
		{
			newds.instance(i).setValue(newds.numAttributes()-1,newds.instance(i).value(firstDateIdx));
		}	
		
		/* remove 'true' date */
		while(firstDateIdx!=-1)
		{
			newds.deleteAttributeAt(firstDateIdx);
			firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(newds);
		}

		/* transform nominal as binaries */
		for (int iidx:WekaDataStatsUtil.getNominalAttributesIndexes(newds))
		{
			newds=WekaDataProcessingUtil.buildDataSetWithNominalAsBinary(newds,iidx);
		}
		
		/* rename attributes for which the name can occur issues in tree evaluation */
		for (int k=0;k<newds.numAttributes();k++)
		{
			String atn=newds.attribute(k).name();
			if (atn.contains("=")) atn=atn.replaceAll("=",(int)(Math.random()*1000)+"");
			if (atn.contains("<")) atn=atn.replaceAll("<",(int)(Math.random()*1000)+"");
			if (atn.contains(">")) atn=atn.replaceAll(">",(int)(Math.random()*1000)+"");
			if (atn.contains(".")) atn=atn.replace(".",(int)(Math.random()*1000)+""); 
			newds=WekaDataProcessingUtil.renameAttribute(newds,k,atn);
		}
		
		/* replace missing values */
		newds=fillGaps0(newds);
				
		/* reconstruct date according to discretized time */
		final String df=ds.attribute(WekaDataStatsUtil.getFirstDateAttributeIdx(ds)).getDateFormat();
		newds.insertAttributeAt(new Attribute(datename+"_new",df),newds.numAttributes());						
		final int newfirstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(newds);
		for (int i=0;i<newds.numInstances();i++)
		{
			final Instance inst=newds.instance(i);
			inst.setValue(newfirstDateIdx,newds.instance(i).value(newds.numAttributes()-2));
		}	
		
		/* sort by date ! */
		newds.sort(newfirstDateIdx);
		
		/* remove discretized time */
		final Set<String> toRemove=new HashSet<String>();
		for (int i=0;i<newds.numAttributes();i++)
		{
			if (newds.attribute(i).name().startsWith("t_")) toRemove.add(newds.attribute(i).name());
		}
		for (final String tr:toRemove) newds.deleteAttributeAt(newds.attribute(tr).index());
		
		/* delete the fake attribute time */
		newds.deleteAttributeAt(newds.numAttributes()-2);
		
		return newds;		
	}

	public final double evaluateMAEWithAFictiveGap(final Instances ds,final int begin,final int end,final int idx) throws Exception
	{
		/* build a new dataset with a new fictive gap */
		final Instances newds=new Instances(ds);		
		for (int i=Math.max(0,begin);i<Math.min(ds.numInstances()-1,end);i++)
		{
			newds.instance(i).setMissing(idx);
		}
		
		/* fill the gap */
		final Instances predictedds=fillGaps(newds);
		
		/* compute the MAE ;-) */
		return mae(ds,predictedds,idx,begin,end);
	}
	
	public final double evaluateMAEByEnlargingGap(final Instances ds,final int valuesToCheckForMAE) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=Math.max((int)gap[2]-valuesToCheckForMAE/2,0);
		final int end=Math.min((int)gap[2]+(int)gap[3]+valuesToCheckForMAE/2,ds.numInstances()-1);
		
		/* compute the error */
		return evaluateMAEWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateMAEByAddingAGapBefore(final Instances ds,final int valuesToCheckForMAE) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=((int)gap[2]-valuesToCheckForMAE)/2;
		final int end=((int)gap[2]+valuesToCheckForMAE)/2;
				
		/* compute the error */
		return evaluateMAEWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateMAEByAddingAGapAfter(final Instances ds,final int valuesToCheckForMAE) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=(int)gap[2]+(int)gap[3]+((int)gap[2]-valuesToCheckForMAE)/2;
		final int end=(int)gap[2]+(int)gap[3]+((int)gap[2]+valuesToCheckForMAE)/2;
				
		/* compute the error */
		return evaluateMAEWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateRMSEWithAFictiveGap(final Instances ds,final int begin,final int end,final int idx) throws Exception
	{
		/* build a new dataset with a new fictive gap */
		final Instances newds=new Instances(ds);		
		for (int i=Math.max(0,begin);i<Math.min(ds.numInstances()-1,end);i++)
		{
			newds.instance(i).setMissing(idx);
		}
		
		/* fill the gap */
		final Instances predictedds=fillGaps(newds);
		
		/* compute the RMSE ;-) */
		return rmse(ds,predictedds,idx,begin,end);
	}

	public final double evaluateRMSEByEnlargingGap(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=Math.max((int)gap[2]-valuesToCheck/2,0);
		final int end=Math.min((int)gap[2]+(int)gap[3]+valuesToCheck/2,ds.numInstances()-1);
		
		/* compute the error */
		return evaluateRMSEWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateRMSEByAddingAGapBefore(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=((int)gap[2]-valuesToCheck)/2;
		final int end=((int)gap[2]+valuesToCheck)/2;
				
		/* compute the error */
		return evaluateRMSEWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateRMSEByAddingAGapAfter(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=(int)gap[2]+(int)gap[3]+((int)gap[2]-valuesToCheck)/2;
		final int end=(int)gap[2]+(int)gap[3]+((int)gap[2]+valuesToCheck)/2;
				
		/* compute the error */
		return evaluateRMSEWithAFictiveGap(ds,begin,end,idx);
	}

	public final double evaluateNSWithAFictiveGap(final Instances ds,final int begin,final int end,final int idx) throws Exception
	{
		/* build a new dataset with a new fictive gap */
		final Instances newds=new Instances(ds);		
		for (int i=Math.max(0,begin);i<Math.min(ds.numInstances()-1,end);i++)
		{
			newds.instance(i).setMissing(idx);
		}
		
		/* fill the gap */
		final Instances predictedds=fillGaps(newds);
		
		/* compute the NS ;-) */
		return nashSutcliffe(ds,predictedds,idx,begin,end);
	}

	public final double evaluateNSByEnlargingGap(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=Math.max((int)gap[2]-valuesToCheck/2,0);
		final int end=Math.min((int)gap[2]+(int)gap[3]+valuesToCheck/2,ds.numInstances()-1);
		
		/* compute the error */
		return evaluateNSWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateNSByAddingAGapBefore(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=((int)gap[2]-valuesToCheck)/2;
		final int end=((int)gap[2]+valuesToCheck)/2;
				
		/* compute the error */
		return evaluateNSWithAFictiveGap(ds,begin,end,idx);
	}
	
	public final double evaluateNSByAddingAGapAfter(final Instances ds,final int valuesToCheck) throws Exception
	{
		/* identify the attr with missing values */
		final int idx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(ds);
		if (idx==-1) throw new IllegalStateException("No attribute with missing value?");
		
		/* identify the first gap */
		final double[] gap=WekaTimeSeriesUtil.findGaps(ds,idx).get(0);
		
		/* define the fictive gap */
		final int begin=(int)gap[2]+(int)gap[3]+((int)gap[2]-valuesToCheck)/2;
		final int end=(int)gap[2]+(int)gap[3]+((int)gap[2]+valuesToCheck)/2;
				
		/* compute the error */
		return evaluateNSWithAFictiveGap(ds,begin,end,idx);
	}
	
	
	
	//
	// Abstract methods
	//
	
	abstract Instances fillGaps0(final Instances newds) throws Exception;
	
	
	//
	// Static methods
	//
		
	/**
	 * TODO: refactor it in order to use MathsUtil
	 */
	public static double rmse(final Instances expected,final Instances predicted,final int idx,final int begin,final int end)
	{
		int trueN=0;
        double rmse=0d;        
		for (int i=Math.max(0,begin);i<Math.min(expected.numInstances()-1,end);i++) 
		{
			if (expected.instance(i).hasMissingValue()) continue;
			final double diff = expected.instance(i).value(idx) - predicted.instance(i).value(idx);
            rmse += diff * diff;
            trueN++;
        }
        rmse=Math.sqrt(rmse/trueN);
        return rmse;	
    }
		
	/**
	 * TODO: refactor it in order to use MathsUtil
	 */	
	public static double mae(final Instances expected,final Instances predicted,final int idx,final int begin,final int end)
	{
		int trueN=0;		
		double sumErr=0d;
		for (int i=Math.max(0,begin);i<Math.min(expected.numInstances()-1,end);i++) 
		{
			if (expected.instance(i).hasMissingValue()) continue;
			final double diff=Math.abs(expected.instance(i).value(idx)-predicted.instance(i).value(idx));
			sumErr+=diff;
			trueN++;
		}
		return sumErr/trueN;
	}
	
	/**
	 * TODO: refactor it in order to use MathsUtil
	 */	
	public static double nashSutcliffe(final Instances expected,final Instances predicted,final int idx,final int begin,final int end)
	{
		int trueN=0;		
		double mean=0d;
		final int l2=Math.min(expected.numInstances()-1,end);
		final int l1=Math.max(0,begin);
		for (int i=l1;i<l2;i++) 
		{
			if (expected.instance(i).hasMissingValue()) continue;
			mean+=expected.instance(i).value(idx);
			trueN++;
		}
		
		if (trueN==0) /*throw new IllegalStateException();*/ return Double.NaN;
		
		mean/=trueN;
		
		double sumDiffs=0d;
		double sumDiffsMean=0d;
		for (int i=l1;i<l2;i++) 
		{
			if (expected.instance(i).hasMissingValue()) continue;
			
			final double diff=Math.abs(expected.instance(i).value(idx)-predicted.instance(i).value(idx));			
			sumDiffs+=diff*diff;

			final double diffMean=Math.abs(expected.instance(i).value(idx)-mean);			
			sumDiffsMean+=diffMean*diffMean;
		}
		return 1d-(sumDiffs/sumDiffsMean);
	}
}
