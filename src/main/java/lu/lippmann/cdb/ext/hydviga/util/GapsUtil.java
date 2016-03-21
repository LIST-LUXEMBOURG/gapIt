/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.util;

import java.util.*;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * GapsUtil.
 * 
 * @author the HYDVIGA team
 */
public final class GapsUtil 
{
	//
	// Static fields
	//
	
	/**	The max count of values before and after the gap. */
	private static final int MAX_VALUES_BEFORE_AND_AFTER=2000;
	/** The count of values before and after the gap should be X* the size of the gap. */
	public static final int VALUES_BEFORE_AND_AFTER_RATIO=4;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	private GapsUtil() {}
	
	
	//
	// Static methods
	//
	
	public static int getCountOfValuesBeforeAndAfter(final int gapsize)
	{
		return Math.min(MAX_VALUES_BEFORE_AND_AFTER,gapsize*VALUES_BEFORE_AND_AFTER_RATIO);
	}

	public static boolean isDuringRising(final Instances dataSet,final int gapsize,final int position,final int[] indexes) throws Exception
	{		
		final Instances dsToCheckRising=WekaDataProcessingUtil.buildDataSetWithMissingValuesReplaced(WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,indexes));		
		final int cvbaForRising=gapsize*2;
		final int startForRisingTest=Math.max(position-cvbaForRising,cvbaForRising);
		final int endForRisingTest=Math.min(position+gapsize+cvbaForRising,dsToCheckRising.numInstances());		
		final boolean isDuringRising=WekaTimeSeriesUtil.isDuringRising(startForRisingTest,endForRisingTest,dsToCheckRising);
		return isDuringRising;
	}	
	
	public static String measureHighMiddleLowInterval(final Instances ds,final int attrIdx,final int pos)	
	{
		final double[] minmax=WekaDataStatsUtil.getMinMaxForAttributeAsArrayOfDoubles(ds,attrIdx);
		final double min=minmax[0];
		final double max=minmax[1];
		final double stepmin=((max-min)/3)+min;
		final double stepmax=(2*(max-min)/3)+min;				
		
		final double val=ds.instance(pos).value(attrIdx);
		String res="n/a";
		if (val>stepmax) res="high";
		else if (val>stepmin) res="middle";
		else res="low";
		
		//System.out.println(ds.toSummaryString());
		//System.out.println("idx="+attrIdx+" pos="+pos+" min="+min+" max="+max+" -> stepmin="+stepmin+" stepmax="+stepmax+" -> val="+val+" res="+res);
		
		return res;
	}
	
	public static Instances buildGapsDescription(final StationsDataProvider gcp,final Instances dataSet,final int dateIdx) 
	{
		final StringBuilder sb=new StringBuilder("@relation blabla\n");
		sb.append("@attribute 'Time serie' string\n");
		sb.append("@attribute 'Start date of the gap' string\n");
		sb.append("@attribute 'Season' string\n");
		sb.append("@attribute 'End date of the gap' string\n");
		//sb.append("@attribute 'End season' string\n");
		sb.append("@attribute 'Gap size' numeric\n");
		sb.append("@attribute 'Gap Position' numeric\n");
		sb.append("@attribute 'Most similar serie (w.g.)' string\n");
		sb.append("@attribute 'Nearest station' string\n");
		sb.append("@attribute 'Upstream station' string\n");
		sb.append("@attribute 'Downstream station' string\n");
		sb.append("@attribute 'Rising?' string\n");
		sb.append("@attribute 'Flow' string\n");
		sb.append("@data\n");
		
		final Calendar cal=Calendar.getInstance();
		for (int i=0;i<dataSet.numAttributes();i++)
		{
			if (i==dateIdx) continue;
			try 
			{				
				final java.util.List<double[]> r=WekaTimeSeriesUtil.findGaps(dataSet,i);
				
				for (double[] dd:r)
				{					
					cal.setTimeInMillis((long)dd[0]);
					final Date start=cal.getTime();	
					final String startSeason=DateUtil.getSeason(cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
					cal.setTimeInMillis((long)dd[1]);
					final String endSeason=DateUtil.getSeason(cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
					final Date end=cal.getTime();
					final String stationName=dataSet.attribute(i).name();
					final String upstream=gcp.findUpstreamStation(stationName);
					final String downstream=gcp.findDownstreamStation(stationName);
					final int size=(int)dd[3];
					final int position=(int)dd[2];
					final String startFlow=(position>0)?GapsUtil.measureHighMiddleLowInterval(dataSet,i,position-1):"";
					final String endFlow=(position+size+1<dataSet.numInstances())?GapsUtil.measureHighMiddleLowInterval(dataSet,i,position+size+1):"";										
					String flow=startFlow;
					if (endFlow.length()>0&&!endFlow.equals(startFlow))
					{
						if (flow.length()>0) flow+="/";
						flow+=endFlow;
					}
					sb.append(stationName)
					  .append(",'")
					  .append(FormatterUtil.DATE_FORMAT.format(start))
					  .append("','")
					  .append(startSeason.equals(endSeason)?startSeason:(startSeason+"/"+endSeason))
					  .append("','")
					  .append(FormatterUtil.DATE_FORMAT.format(end))
					  .append("',")
					  .append(size)
					  .append(",")
					  .append(position)					  
					  .append(",?")
					  .append(",").append(gcp.findNearestStation(stationName))
					  .append(",").append(upstream!=null?upstream:"n/a")
					  .append(",").append(downstream!=null?downstream:"n/a")					  
					  .append(",'n/a'")
					  .append(",").append("'"+flow+"'").append("\n");
				}				
			} 
			catch (Exception e) 
			{				
				e.printStackTrace();
			}
		}

		try 
		{
			return WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),false,false);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
		return null;
	}


}
