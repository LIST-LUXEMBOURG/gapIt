/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.timeseries;

import java.util.*;
import java.util.concurrent.*;

import lu.lippmann.cdb.weka.WekaDataProcessingUtil;
import weka.core.*;


/**
 * WekaTimeSeriesSimilarityUtil.
 * 
 * @author Olivier PARISOT
 */
public final class WekaTimeSeriesSimilarityUtil 
{
	//
	// Static fields
	//
	
	/** */
	private static final ExecutorService EXECUTOR_SERVICE=Executors.newFixedThreadPool(Math.max(2,Math.max(1,Runtime.getRuntime().availableProcessors()-1)));
	
	
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private WekaTimeSeriesSimilarityUtil() {}
	
	
	//
	// Static methods
	//
	
	public static String findMostSimilarTimeSerie(final Instances testds,final Attribute attr,final List<String> attrNames,final boolean THREADED_NN_LOOKUP)
	{
		//System.out.println("findMostSimilarTimeSerie: for "+attr.name()+" in "+attrNames);
		
		//System.out.println("findMostSimilarTimeSerie: (before) rows -> "+testds.numInstances());
		final List<String> attrNamesWithCurrent=new ArrayList<>(attrNames);
		attrNamesWithCurrent.add(attr.name());
		final Instances reducedds=WekaDataProcessingUtil.buildDataSetWithoutRowsWithMissingValues(testds,attrNamesWithCurrent); // TODO: optimize remove into this method
		//System.out.println(reducedds.numAttributes());
		//System.out.println("findMostSimilarTimeSerie: (after) rows -> "+reducedds.numInstances());
		final double[] startArray1=reducedds.attributeToDoubleArray(reducedds.attribute(attr.name()).index());
		if (startArray1.length==0) System.out.println("no val for "+attr.name()+"??");
		
		double min=Double.POSITIVE_INFINITY;
		int idx=-1;	
		
		if (!THREADED_NN_LOOKUP)
		{			
			for (int i=0;i<reducedds.numAttributes();i++)
			{	
				//System.out.println(i);
				if (!attrNames.contains(reducedds.attribute(i).name())) 
				{	
					//System.out.println(reducedds.attribute(i).name()+" not in "+attrNames);
					continue;
				}
				final double[] startArray2=reducedds.attributeToDoubleArray(i);
				final double distance=new DynamicTimeWarping(startArray1,startArray2).getDistance();
				if (distance<min)
				{
					min=distance;
					idx=i;
				}
				//System.out.println("\t to "+reducedds.attribute(i).name()+": "+distance);
			}
		}
		else
		{
			final List<Callable<Object[]>> tasks=new ArrayList<Callable<Object[]>>();
			for (int i=0;i<reducedds.numAttributes();i++)
			{	
				if (!attrNames.contains(reducedds.attribute(i).name())) continue;
				final int ii=i;
				tasks.add(new Callable<Object[]>() 
				{
					@Override
					public Object[] call() throws Exception 
					{					
						final double[] startArray2=reducedds.attributeToDoubleArray(ii);
						final double distance=new DynamicTimeWarping(startArray1,startArray2).getDistance();
						//System.out.println("\t to "+reducedds.attribute(ii).name()+": "+distance);
						return new Object[]{distance,ii};
					}
				});					
			}
			
			try 
			{
				final List<Future<Object[]>> res=EXECUTOR_SERVICE.invokeAll(tasks);
				for (final Future<Object[]> r:res) 
				{	
					final Object[] oo=r.get();
					final double distance=(Double)oo[0];
					final int curridx=(Integer)oo[1];						
					if (distance<min)
					{
						min=distance;
						idx=curridx;
					}
				}				
			} 
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}

		final String foundSerie=(idx!=-1)?reducedds.attribute(idx).name():null;
		//System.out.println("found -> "+foundSerie);
		return foundSerie;						
	}
}
