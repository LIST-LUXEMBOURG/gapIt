/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.text.SimpleDateFormat;
import java.util.*;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.lab.timeseries.DynamicTimeWarping;
import weka.clusterers.FarthestFirst;
import weka.core.*;


/**
 * WekaTimeSeriesUtil.
 * 
 * @author the WP1 team
 */
public final class WekaTimeSeriesUtil 
{
	//
	// Static fields
	//
	
	/** */
	private static final String PATTERN="t__";
	/** */
	public static final String YEAR=PATTERN+"year__";
	/** */
	public static final String QUARTER=PATTERN+"quarter__";
	/** */
	public static final String MONTH=PATTERN+"month__";
	/** */
	public static final String WEEK=PATTERN+"week__";
	/** */
	public static final String DAY=PATTERN+"day__";
	/** */
	public static final String TIMESLOT=PATTERN+"time_slot__";
	/** */
	public static final String HOUR=PATTERN+"hour__";
	/** */
	public static final String MIN=PATTERN+"min__";
	/** */
	public static final String SEC=PATTERN+"sec__";

	public static final String[] FIELDS=new String[]{YEAR,QUARTER,MONTH,WEEK,DAY,TIMESLOT,HOUR,MIN,SEC};
	
	
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private WekaTimeSeriesUtil() {}


	//
	// Static methods
	//
	
	public static List<String> getNamesOfAttributesWithoutGap(final Instances ds)
	{
		final List<String> attrNames=WekaDataStatsUtil.getNumericAttributesNames(ds);
		final java.util.List<String> torem=new ArrayList<String>();
		try 
		{
			for (final String s:attrNames)
			{
				if (findGaps(ds,ds.attribute(s).index()).size()>0) torem.add(s);
			}
		} 
		catch (final Exception e) 
		{			
			e.printStackTrace();
		}
		attrNames.removeAll(torem);
		return attrNames;
	}
	
	public static Instances buildDataSetWithoutDates(final Instances ds) throws Exception
	{
		final List<Integer> l=new ArrayList<Integer>();
		for (int i=0;i<ds.numAttributes();i++) l.add(i);
		l.removeAll(WekaDataStatsUtil.getDateAttributeIndexes(ds));
		final int[] arr=new int[l.size()];
		for (int k=0;k<l.size();k++) arr[k]=l.get(k);				
		return WekaDataProcessingUtil.buildFilteredByAttributesDataSet(ds,arr);
	}


	public static Instances buildDataSetSortedByTimeSeries(final Instances ds,final int idx) throws Exception
	{
		final int numAttributes=ds.numAttributes();
		final List<WekaDataProcessingUtil.AttributeDesc> attrValuesList=new ArrayList<WekaDataProcessingUtil.AttributeDesc>(numAttributes);
		for (int i=0;i<numAttributes;i++)
		{
			attrValuesList.add(new WekaDataProcessingUtil.AttributeDesc(i,ds.attributeToDoubleArray(i)));
		}
		final WekaDataProcessingUtil.AttributeDesc base=attrValuesList.get(idx);
		Collections.sort(attrValuesList,new Comparator<WekaDataProcessingUtil.AttributeDesc>()
		{
			@Override
			public int compare(final WekaDataProcessingUtil.AttributeDesc o1,final WekaDataProcessingUtil.AttributeDesc o2) 
			{
				final double distance1=new DynamicTimeWarping(base.getArray(),o1.getArray()).getDistance();
				final double distance2=new DynamicTimeWarping(base.getArray(),o2.getArray()).getDistance();
				
				return Double.valueOf(distance1).compareTo(Double.valueOf(distance2));
			}
		});
		final int[] indexes=new int[numAttributes];		
		for (int k=0;k<numAttributes;k++)
		{
			indexes[k]=attrValuesList.get(k).getIdx();
		}
		
		return WekaDataProcessingUtil.buildFilteredByAttributesDataSet(ds,indexes);
	}		
	
	public static Instances buildDataSetWithDiscretizedTime(final Instances dataSet) throws Exception 
	{
		final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
						
		String yearAttrName=YEAR;
		while (dataSet.attribute(yearAttrName)!=null) yearAttrName+="_"; 
		String quarterAttrName=QUARTER;
		while (dataSet.attribute(quarterAttrName)!=null) quarterAttrName+="_"; 
		String monthAttrName=MONTH;
		while (dataSet.attribute(monthAttrName)!=null) monthAttrName+="_"; 
		String weekAttrName=WEEK;
		while (dataSet.attribute(weekAttrName)!=null) weekAttrName+="_"; 		
		String dayAttrName=DAY;
		while (dataSet.attribute(dayAttrName)!=null) dayAttrName+="_"; 
		String timeslotAttrName=TIMESLOT;
		while (dataSet.attribute(timeslotAttrName)!=null) timeslotAttrName+="_"; 
		String hourAttrName=HOUR;
		while (dataSet.attribute(hourAttrName)!=null) hourAttrName+="_"; 
		String minAttrName=MIN;
		while (dataSet.attribute(minAttrName)!=null) minAttrName+="_"; 
		String secAttrName=SEC;
		while (dataSet.attribute(secAttrName)!=null) secAttrName+="_"; 
		
		final List<String> timeSlotValues=Arrays.asList(new String[]{"0<=.<6","6<=.<12","12<=.<18","18<=.<24"});
		
		final Instances newds=new Instances(dataSet);
		newds.insertAttributeAt(new Attribute(yearAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(quarterAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(monthAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(weekAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(dayAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(timeslotAttrName,timeSlotValues),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(hourAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(minAttrName),newds.numAttributes());
		newds.insertAttributeAt(new Attribute(secAttrName),newds.numAttributes());
				
		final SimpleDateFormat f=new SimpleDateFormat(newds.attribute(firstDateIdx).getDateFormat());
		final Calendar cal=Calendar.getInstance();
		final int numInstances=newds.numInstances();
		for (int i=0;i<numInstances;i++)
		{			
			final Instance inst=newds.instance(i);
			
			final Date d=f.parse(inst.stringValue(firstDateIdx));
			cal.setTime(d);
			
			inst.setValue(newds.attribute(yearAttrName),cal.get(Calendar.YEAR));
			inst.setValue(newds.attribute(quarterAttrName),((cal.get(Calendar.MONTH)+1)/4)+1);
			inst.setValue(newds.attribute(monthAttrName),cal.get(Calendar.MONTH)+1);
			inst.setValue(newds.attribute(weekAttrName),cal.get(Calendar.WEEK_OF_YEAR));
			inst.setValue(newds.attribute(dayAttrName),cal.get(Calendar.DAY_OF_MONTH));
			inst.setValue(newds.attribute(timeslotAttrName),timeSlotValues.get(cal.get(Calendar.HOUR_OF_DAY)/6));
			inst.setValue(newds.attribute(hourAttrName),cal.get(Calendar.HOUR_OF_DAY));
			inst.setValue(newds.attribute(minAttrName),cal.get(Calendar.MINUTE));
			inst.setValue(newds.attribute(secAttrName),cal.get(Calendar.SECOND));
		}
		
		return newds;
	}

	public static Instances removeAllAttributesInsteadOfDiscretizedTimeAndClass(final Instances dataSet)
	{
		final Instances newds=new Instances(dataSet);		
		for (int i=0;i<newds.numAttributes();i++)
		{
			if (!newds.attribute(i).name().startsWith(PATTERN)&&newds.classIndex()!=i)
			{								
				newds.deleteAttributeAt(newds.attribute(newds.attribute(i).name()).index());
				i--;
			}
		}
		return newds;
	}

	public static Instances removeAllDiscretizedTimeAttributes(final Instances dataSet)
	{
		final Instances newds=new Instances(dataSet);		
		for (int i=0;i<newds.numAttributes();i++)
		{
			if (newds.attribute(i).name().startsWith(PATTERN)&&newds.classIndex()!=i)
			{								
				newds.deleteAttributeAt(newds.attribute(newds.attribute(i).name()).index());
				i--;
			}
		}		
		return newds;
	}
	
	public static List<Instances> buildDataSetsListSplittedByTime(final Instances ds,final String featureName) throws Exception
	{
		final List<Instances> l=new ArrayList<Instances>();
		
		final int idx=ds.attribute(featureName).index();
		
		for (final String val:WekaDataStatsUtil.getPresentValuesForNominalAttribute(ds,idx))
		{
			l.add(WekaDataProcessingUtil.filterDataSetOnNominalValue(ds,idx,val));
		}
		return l;		
	}

	public static Instances buildDataSetWithFakeTime(final Instances dataSet,final long firstTimeInMillisec,final long stepInMillisec) throws Exception 
	{
		String ftAttrName="__faketime__";
		while (dataSet.attribute(ftAttrName)!=null) ftAttrName+="_"; 
		
		final Instances newds=new Instances(dataSet);
		newds.insertAttributeAt(new Attribute(ftAttrName,FormatterUtil.DATE_FORMAT_WITH_SEC.toPattern()),newds.numAttributes());		
		
		final int numInstances=newds.numInstances();
		for (int i=0;i<numInstances;i++)
		{			
			final Instance inst=newds.instance(i);
			inst.setValue(newds.attribute(ftAttrName),firstTimeInMillisec+i*stepInMillisec);
		}
		
		return newds;
	}

	public static Instances buildDataSetWithFakeTime(final Instances dataSet) throws Exception
	{
		final Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(0);
		return buildDataSetWithFakeTime(dataSet,cal.getTimeInMillis(),1000);
	}

	public static Instances fillAllGapsWithInterpolation(final Instances ds) throws Exception
	{		
		/*final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(ds);
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}*/
		
		final Instances newds=new Instances(ds);
		return weka.classifiers.timeseries.core.Utils.replaceMissing(newds,WekaDataStatsUtil.getAttributeNames(newds),null,false,null,null);		
	}	

	public static Instances fillGapWithInterpolation(final Instances dataSet,final Attribute attr,final int position,final int gapsize,final int valuesBeforeAndAfter) throws Exception	
	{	
		final int dateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
		if (dateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		final Instances correctedDataSet=new Instances(dataSet);
		final int corrNumInstances=correctedDataSet.numInstances();
		
		Instances filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,new int[]{attr.index(),dateIdx});									
		filteredDs=WekaDataProcessingUtil.buildFilteredDataSet(filteredDs,0,filteredDs.numAttributes()-1,Math.max(0,position-valuesBeforeAndAfter),Math.min(position+gapsize+valuesBeforeAndAfter,filteredDs.numInstances()-1));
		
		final Instances completedds=WekaTimeSeriesUtil.fillAllGapsWithInterpolation(filteredDs);
		final Instances diff=WekaTimeSeriesUtil.buildDiff(filteredDs,completedds);
		
		//System.out.println("Build a corrected dataset ...");
		//System.out.println(correctedDataSet.toSummaryString());
		
		for (int k=0;k<diff.numInstances();k++)
		{
			final Instance diffInstanceK=diff.instance(k);
			if (diffInstanceK.isMissing(0)) continue;
			
			final long timestamp=(long)diffInstanceK.value(1);
			
			for (int h=0;h<corrNumInstances;h++)
			{
				if ((long)correctedDataSet.instance(h).value(dateIdx)==timestamp)
				{
					correctedDataSet.instance(h).setValue(attr,diffInstanceK.value(0));
					break;
				}
			}
		}
		
		//System.out.println("... corrected dataset built!");
		//System.out.println(correctedDataSet.toSummaryString());
		
		return correctedDataSet;
	}
	
	public static Instances buildDiff(final Instances pbefore,final Instances pafter) throws Exception
	{
		final int firstDateIdxBefore=WekaDataStatsUtil.getFirstDateAttributeIdx(pbefore);
		if (firstDateIdxBefore==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		final int firstDateIdxAfter=WekaDataStatsUtil.getFirstDateAttributeIdx(pafter);
		if (firstDateIdxAfter==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		if (pbefore.size()!=pafter.size()) throw new IllegalStateException();
		
		Instances diff=new Instances(pafter);
		final int diffNumInstances=diff.numInstances();
		final int diffNumAttributes=diff.numAttributes();
		for (int i=0;i<diffNumInstances;i++)
		{
			/* only keep replaced values */
			for (int j=0;j<diffNumAttributes;j++)
			{
				if (j==firstDateIdxAfter) continue;								
				final int jIndexInPBefore=pbefore.attribute(diff.attribute(j).name()).index();
				if (!pbefore.instance(i).isMissing(jIndexInPBefore)) 
				{	
					final int i_after=i+1;				
					final int i_before=i-1;
					
					/* this complex test allows to keep value before and after the gaps */
					if (i_after<diffNumInstances&&i_before>=0)
					{
						if (!pbefore.instance(i_after).isMissing(jIndexInPBefore)
								&&!pbefore.instance(i_before).isMissing(jIndexInPBefore))
						{		
							diff.instance(i).setMissing(j);
						}
					}

				}

			}
		}
		
		/* remove attributes for which nothing has been replaced */
		final List<Attribute> toRemove=new ArrayList<Attribute>();
		for (int j=0;j<diffNumAttributes;j++)
		{
			if (diff.attributeStats(j).missingCount==diffNumInstances)
			{
				toRemove.add(diff.attribute(j));
			}
		}
		for (final Attribute tr:toRemove) diff.deleteAttributeAt(diff.attribute(tr.name()).index());
		
		for (int j=0;j<diffNumAttributes;j++)
		{
			diff=WekaDataProcessingUtil.renameAttribute(diff,j,diff.attribute(j).name()+"_diff");
		}
		
		
		
		
		return diff;
	}
	
	public static Instances removeFirstGap(final Instances ds) throws Exception
	{		
		final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(ds);
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		System.out.println("Remove first gap ...");
		final Instances newds=new Instances(ds);
		final Set<Instance> toRemove=new HashSet<Instance>();
		for (int i=0;i<newds.numInstances();i++)
		{
			if (newds.instance(i).hasMissingValue()) toRemove.add(newds.instance(i));
			else break;
		}
		newds.removeAll(toRemove);
		System.out.println("... first gap removed!");
		return newds;
	}
	
	public static Instances buildMergedDataSet2(final Instances ds1,final Instances... array) throws Exception	
	{
		System.out.println("Merge time series ...");
		Instances newds=ds1;
		for (final Instances ds:array) newds=buildMergedDataSet(newds,ds);
		System.out.println("... time series merged!");
		return newds;
	}
	
	public static Instances buildMergedDataSet(final Instances ds1,final Instances ds2) throws Exception 
	{
		final int firstDateIdx1=WekaDataStatsUtil.getFirstDateAttributeIdx(ds1);
		if (firstDateIdx1==-1) 
		{	
			throw new Exception("No date attribute in the first dataset!");			
		}
		
		final int firstDateIdx2=WekaDataStatsUtil.getFirstDateAttributeIdx(ds2);
		if (firstDateIdx2==-1) 
		{	
			throw new Exception("No date attribute in the second dataset!");			
		}
		
		final ArrayList<Attribute> newAttributes=new ArrayList<Attribute>();
		final Set<String> names1=new HashSet<String>();
		for (int i=0;i<ds1.numAttributes();i++) names1.add(ds1.attribute(i).name());			
		final Set<String> names2=new HashSet<String>();
		for (int i=0;i<ds2.numAttributes();i++) names2.add(ds2.attribute(i).name());
		for (int i=0;i<ds1.numAttributes();i++) 
		{
			final String origName=ds1.attribute(i).name();
			if (names2.contains(origName))
			{
				newAttributes.add(ds1.attribute(i).copy(ds1.relationName()+"_1_"+origName));
			}
			else
			{
				newAttributes.add(ds1.attribute(i).copy(origName));
			}
		}
		for (int i=0;i<ds2.numAttributes();i++) 
		{
			final String origName=ds2.attribute(i).name();						
			if (names1.contains(origName))
			{
				newAttributes.add(ds2.attribute(i).copy(ds2.relationName()+"_2_"+origName));
			}
			else
			{
				newAttributes.add(ds2.attribute(i).copy(origName));
			}
		}
		    
		final Instances merged=new Instances(ds1.relationName()+'_'+ds2.relationName(),newAttributes,0);
		
		final Map<Double,double[]> vals1=new HashMap<Double,double[]>(ds1.numInstances());		
		for (int i=0;i<ds1.numInstances();i++)
		{	
			vals1.put(ds1.instance(i).value(firstDateIdx1),ds1.instance(i).toDoubleArray());
		}
		final Map<Double,double[]> vals2=new HashMap<Double,double[]>(ds2.numInstances());
		for (int i=0;i<ds2.numInstances();i++)
		{	
			vals2.put(ds2.instance(i).value(firstDateIdx2),ds2.instance(i).toDoubleArray());
		}
		
		for (final Map.Entry<Double,double[]> entry:vals1.entrySet())
		{
			double[] v=entry.getValue();
			if (vals2.containsKey(entry.getKey()))
			{
				v=ArraysUtil.concat(v,vals2.get(entry.getKey()));
			}
			else
			{
				final double[] empty2=new double[ds2.numAttributes()];
				Arrays.fill(empty2,Double.NaN);
				empty2[firstDateIdx2]=entry.getKey();
				v=ArraysUtil.concat(v,empty2);
			}
			merged.add(new DenseInstance(1d,v));
		}
		for (final Map.Entry<Double,double[]> entry:vals2.entrySet())
		{
			if (!vals1.containsKey(entry.getKey()))
			{
				final double[] empty1=new double[ds1.numAttributes()];
				Arrays.fill(empty1,Double.NaN);
				empty1[firstDateIdx1]=entry.getKey();
				final double[] v=ArraysUtil.concat(empty1,entry.getValue());
				merged.add(new DenseInstance(1d,v));
			}			
		}
		
		merged.sort(WekaDataStatsUtil.getFirstDateAttributeIdx(merged));
		merged.deleteAttributeAt(WekaDataStatsUtil.getFirstDateAttributeIdx(merged));		
		return merged;
	}
	
	public static Instances buildClusteredDataSet(final Instances dataSet,final int k,final String clusterAttrName) throws Exception
	{
		final FarthestFirst clusterer=new FarthestFirst();
		clusterer.setNumClusters(k);		
		final WekaClusteringResult wcr=WekaMachineLearningUtil.computeClusters(clusterer,dataSet);			
		final Instances clusterAssignementDataSet=WekaMachineLearningUtil.buildDataSetExplainingClustersAssignment(dataSet,wcr.getAss(),clusterAttrName);
		clusterAssignementDataSet.setClassIndex(-1);		
		return clusterAssignementDataSet;
	}
	
	public static List<double[]> split(final Instances ds,final int idx) throws Exception
	{
		final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(ds);
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in the dataset!");			
		}
		
		final List<double[]> r=new ArrayList<double[]>();
		
		double lastValue=-1d;
		double lastTime=-1d;
		double[] current=new double[4]; // start,end,position,size
		current[0]=-1d;
		current[1]=-1d;
		current[2]=0d;
		current[3]=1d;
		for (int i=0;i<ds.numInstances();i++)
		{			
			final double time=ds.instance(i).value(firstDateIdx);
			if (current[0]<0d) current[0]=time;
			
			final boolean eq=Math.abs(ds.instance(i).value(idx)-lastValue)<0.001d||(Double.isNaN(lastValue)&&Double.isNaN(ds.instance(i).value(idx)));
			if (!eq&&lastTime>0d)
			{
				current[1]=lastTime;
				r.add(current);
				current=new double[]{time,-1d,i,0};				
			}
			lastValue=ds.instance(i).value(idx);
			lastTime=time;
			current[3]++;
		}
		
		if (current[1]<0d)
		{
			current[1]=ds.instance(ds.numInstances()-1).value(firstDateIdx);
			r.add(current);
		}
		
		for (final double[] dd:r)
		{
			if (dd[2]+dd[3]>=ds.numInstances()) dd[3]--;
		}
		
		return r;
	}
	

	public static List<double[]> findGaps(final Instances ds,final int idx) throws Exception
	{
		final List<double[]> l=new ArrayList<double[]>();
		final List<double[]> s=split(ds,idx);
		for (final double[] dd:s)
		{			
			if (ds.instance((int)dd[2]).isMissing(idx))				
			{
				l.add(dd);
			}
		}		
		return l;		
	}
	
	public static final Instances changeGranularity(final Instances ds,final List<String> fields) throws Exception
	{
		if (fields.size()==0) return ds;
		
		final Instances discrds=buildDataSetWithDiscretizedTime(ds);
		final Map<String,List<Integer>> map=new HashMap<String,List<Integer>>();
		for (int i=0;i<discrds.numInstances();i++)
		{
			final Instance inst=discrds.instance(i);
			final StringBuilder key=new StringBuilder();
			for (String field:fields) key.append(inst.value(discrds.attribute(field))).append('-');
			key.setLength(key.length()-1);
			if (!map.containsKey(key.toString())) 
			{
				map.put(key.toString(),new ArrayList<Integer>());
			}				
			map.get(key.toString()).add(i);			
		}		
		
		//System.out.println(map);
		
		final List<Integer> idxToConsider=new ArrayList<Integer>();
		for (int j=0;j<ds.numAttributes();j++)
		{
			if (ds.attribute(j).isNumeric()&&!ds.attribute(j).isDate())
			{				
				idxToConsider.add(j);
			}
		}
		
		final Instances newds=new Instances(ds,0);
		for (final Map.Entry<String,List<Integer>> entry:map.entrySet())
		{
			final List<Integer> idxList=entry.getValue();
			final Instance inst=new DenseInstance(1.0d,ds.instance(idxList.get(0)).toDoubleArray());			
			inst.setDataset(newds);			
			for (final Integer j:idxToConsider)
			{
				inst.setValue(j,0);
			}			
			final int[] cnt=new int[ds.numAttributes()];
			for (int k=0;k<idxList.size();k++)
			{
				final int idx=idxList.get(k);
				final Instance instToAdd=ds.instance(idx);
				//System.out.println("add "+idx);				
				for (final Integer j:idxToConsider)
				{
					if (instToAdd.isMissing(j)) continue;
					//System.out.println(idx+" "+inst.value(j)+" "+instToAdd.value(j));
					inst.setValue(j,inst.value(j)+instToAdd.value(j));
					cnt[j]++;
				}
			}
			
			for (final Integer j:idxToConsider)
			{
				inst.setValue(j,inst.value(j)/cnt[j]);
			}
			
			//System.out.println(inst);
			newds.add(inst);
		}
		
		newds.sort(WekaDataStatsUtil.getFirstDateAttributeIdx(newds));
		
		return newds;
	}


	public static boolean isDuringRising(final int start,final int end,final Instances ds) throws Exception 
	{
		final Instances markedDS=WekaDataProcessingUtil.buildDataSetWithMarkedOutliers(ds);
		final Attribute isOutlierAttribute=markedDS.attribute(WekaDataProcessingUtil.IS_EXTREME_VALUE_OR_OUTLIER_FEATURE);							
		boolean isDuringRising=false;
		for (int kk=start;kk<end;kk++)
		{
			if (markedDS.instance(kk).stringValue(isOutlierAttribute).equals("extreme")) 
			{	
				isDuringRising=true;
				break;
			}
		}
		return isDuringRising;
	}
	
//	public static final void forecast(final Instances dataset) throws Exception
//	{
//		// new forecaster
//		WekaForecaster forecaster = new WekaForecaster();
//		
//		// set the targets we want to forecast. This method calls
//		// setFieldsToLag() on the lag maker object for us
//		forecaster.setFieldsToForecast("Fortified,Dry-white");
//
//		// default underlying classifier is SMOreg (SVM) - we'll use
//		// gaussian processes for regression instead
//		forecaster.setBaseForecaster(new GaussianProcesses());
//
//		forecaster.getTSLagMaker().setTimeStampField("Date"); // date time stamp
//		forecaster.getTSLagMaker().setMinLag(1);
//		forecaster.getTSLagMaker().setMaxLag(12); // monthly data
//
//		// add a month of the year indicator field
//		forecaster.getTSLagMaker().setAddMonthOfYear(true);
//
//		// add a quarter of the year indicator field
//		forecaster.getTSLagMaker().setAddQuarterOfYear(true);
//
//		// build the model
//		forecaster.buildForecaster(dataset, System.out);
//
//		// prime the forecaster with enough recent historical data
//		// to cover up to the maximum lag. In our case, we could just supply
//		// the 12 most recent historical instances, as this covers our maximum
//		// lag period
//		forecaster.primeForecaster(dataset);
//
//		// forecast for 12 units (months) beyond the end of the
//		// training data
//		List<List<NumericPrediction>> forecast = forecaster.forecast(12,System.out);
//
//		// output the predictions. Outer list is over the steps; inner list is
//		// over
//		// the targets
//		for (int i = 0; i < 12; i++) 
//		{
//			List<NumericPrediction> predsAtStep = forecast.get(i);
//			for (int j = 0; j < 2; j++) 
//			{
//				NumericPrediction predForTarget = predsAtStep.get(j);
//				System.out.print("" + predForTarget.predicted() + " ");
//			}
//			System.out.println();
//		}
//		
//	}


}
