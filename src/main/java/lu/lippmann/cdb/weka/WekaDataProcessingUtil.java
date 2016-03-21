/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.weka.filters.AllToNumeric;
import weka.attributeSelection.*;
import weka.core.*;
import weka.filters.Filter;



/**
 * Weka utility class.
 *
 * @author the ACORA team
 */
public final class WekaDataProcessingUtil 
{
	//
	// Static fields
	//

	/** */
	public static final String IS_EXTREME_VALUE_OR_OUTLIER_FEATURE="isExtremeValueOrOutlier";
	/** */
	private static final String OUTLIER_FEATURE="Outlier";
	/** */
	private static final String EXTREME_VALUE_FEATURE="ExtremeValue";

	
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private WekaDataProcessingUtil() {}


	//
	// Static methods
	//

	public static Instances buildDataSetModifiedByMathExpression(final Instances ds,final int index,final String mathExpr) throws Exception
	{
		final weka.filters.unsupervised.attribute.MathExpression filter=new weka.filters.unsupervised.attribute.MathExpression();				
		filter.setExpression(mathExpr);
		filter.setIgnoreRange(""+(index+1)+"");		
		filter.setInvertSelection(true);
		filter.setInputFormat(ds);		
		return Filter.useFilter(ds,filter);
	}

	public static Instances buildFilteredByAttributesDataSet(final Instances ds,final int[] attrIndexes) throws Exception
	{
		final weka.filters.unsupervised.attribute.Reorder filter=new weka.filters.unsupervised.attribute.Reorder();
		filter.setAttributeIndicesArray(attrIndexes);
		filter.setInputFormat(ds);
		return Filter.useFilter(ds,filter);
	}

	public static Instances buildFilteredByRowsDataSet(final Instances ds,final int[] rowsIndexes) throws Exception
	{
		final Instances newds=new Instances(ds,0);
		for (final int i:rowsIndexes) newds.add(ds.instance(i));
		return newds;
	}
	
	public static Instances buildFilteredByRowsDataSet(final Instances ds,final int min,final int max) throws Exception
	{
		final Instances newds=new Instances(ds,0);
		for (int i=min;i<max;i++) newds.add(ds.instance(i));
		return newds;
	}
	
	public static Instances buildFilteredDataSet(final Instances ds,final int minattr,final int maxattr,final int mininst,final int maxinst)
	{
		if (maxattr>ds.numAttributes()) throw new IllegalArgumentException();
		if (maxinst>ds.numInstances()) throw new IllegalArgumentException();
		
		final ArrayList<Integer> listAttrIdx=new ArrayList<Integer>();
		for (int i=minattr;i<=maxattr;i++) listAttrIdx.add(i);
		final int[] array=new int[listAttrIdx.size()];
		for (int k=0;k<array.length;k++) array[k]=listAttrIdx.get(k);
		
		try 
		{
			final Instances newds=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(ds,array);
			final Instances newds2=new Instances(newds,0);
			for (int j=mininst;j<=maxinst;j++) newds2.add(newds.instance(j));
			return newds2;
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
		return null;
	}
	
	public static Instances buildDataSetSortedByAttribute(final Instances ds,final int idx) throws Exception
	{
		final int numAttributes=ds.numAttributes();
		final List<AttributeDesc> attrValuesList=new ArrayList<AttributeDesc>(numAttributes);
		for (int i=0;i<numAttributes;i++)
		{
			attrValuesList.add(new AttributeDesc(i,ds.attributeToDoubleArray(i)));
		}
		final AttributeDesc base=attrValuesList.get(idx);
		Collections.sort(attrValuesList,new Comparator<AttributeDesc>()
		{
			@Override
			public int compare(final AttributeDesc o1,final AttributeDesc o2) 
			{
				final double distance1=MathsUtil.distance(base.getArray(),o1.getArray());
				final double distance2=MathsUtil.distance(base.getArray(),o2.getArray());				
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
	
	public static Instances buildDataSetWithMissingValuesReplaced(final Instances ds) throws Exception
	{
		final weka.filters.unsupervised.attribute.ReplaceMissingValues filter=new weka.filters.unsupervised.attribute.ReplaceMissingValues();
		filter.setInputFormat(ds);
		return Filter.useFilter(ds,filter);
	}	
	
	public static Instances buildDataSetWithMissingValuesReplacedUsingEM(final Instances ds) throws Exception
	{
		if (!WekaDataStatsUtil.areAllNonClassAttributesNumeric(ds))
		{
			throw new Exception("All the attributes of the dataset should be numeric.");
		}
		
		final weka.filters.unsupervised.attribute.EMImputation filter=new weka.filters.unsupervised.attribute.EMImputation();
		filter.setInputFormat(ds);		
		return Filter.useFilter(ds,filter);
	}	
	
	public static final Instances buildDataSetWithoutExtremeValues(final Instances ds) throws Exception
	{
		return buildDataSetWithout(ds,EXTREME_VALUE_FEATURE);
	}

	public static final Instances buildDataSetWithoutOutliers(final Instances ds) throws Exception
	{
		return buildDataSetWithout(ds,OUTLIER_FEATURE);
	}
	
	private static final Instances buildDataSetWithout(final Instances ds,final String attrName) throws Exception
	{
		Instances dsToUse=new Instances(ds);
		if (WekaDataStatsUtil.getCountOfMissingValues(ds)>0)
		{
			System.out.println("Replace missing values before removing '"+attrName+"'");
			dsToUse=buildDataSetWithMissingValuesReplaced(ds);
		}
		
		final weka.filters.unsupervised.attribute.InterquartileRange filter=new weka.filters.unsupervised.attribute.InterquartileRange();
		filter.setInputFormat(dsToUse);
		
		final Instances markedDs=Filter.useFilter(dsToUse,filter);

		final Attribute attr=markedDs.attribute(attrName);
		final List<Instance> l=new ArrayList<Instance>();
		final int numInstances=markedDs.numInstances();
		for (int i=0;i<numInstances;i++)
		{	    	
			if (markedDs.instance(i).stringValue(attr).equals("no"))
			{
				l.add(ds.instance(i));
			}
		}

		final Instances newds=new Instances(ds,0);	    
		for (final Instance ins:l) newds.add(ins);
		return newds;	    
	}	

	public static final Instances buildDataSetWithMarkedOutliers(final Instances ds) throws Exception
	{
		final weka.filters.unsupervised.attribute.InterquartileRange filter=new weka.filters.unsupervised.attribute.InterquartileRange();
		filter.setInputFormat(ds);	    
		final Instances markedDs=Filter.useFilter(ds,filter);
		String attrName=IS_EXTREME_VALUE_OR_OUTLIER_FEATURE;
		while (markedDs.attribute(attrName)!=null) attrName+="_";
		markedDs.insertAttributeAt(new Attribute(attrName,Arrays.asList("extreme","outlier","normal")),markedDs.numAttributes());

		final int numInstances=markedDs.numInstances();
		for (int i=0;i<numInstances;i++)
		{	    	
			boolean isExtrValue=(markedDs.instance(i).stringValue(markedDs.attribute(EXTREME_VALUE_FEATURE)).equals("yes"));
			boolean isOutlier=(markedDs.instance(i).stringValue(markedDs.attribute(OUTLIER_FEATURE)).equals("yes"));
			if (isExtrValue) markedDs.instance(i).setValue(markedDs.numAttributes()-1,"extreme");
			else if (isOutlier) markedDs.instance(i).setValue(markedDs.numAttributes()-1,"outlier");
			else markedDs.instance(i).setValue(markedDs.numAttributes()-1,"normal");
		}
		
		markedDs.deleteAttributeAt(markedDs.attribute(EXTREME_VALUE_FEATURE).index());
		markedDs.deleteAttributeAt(markedDs.attribute(OUTLIER_FEATURE).index());
		return markedDs;	    
	}
	
	public static final Instances buildDataSetWithMarkedMissing(final Instances ds) throws Exception
	{
		final Instances markedDs=new Instances(ds);
		String attrName="HAS_MISSING";
		while (markedDs.attribute(attrName)!=null) attrName+="_";
		markedDs.insertAttributeAt(new Attribute(attrName,Arrays.asList("no","yes")),markedDs.numAttributes());

		final int numInstances=markedDs.numInstances();
		final int numAttributes=markedDs.numAttributes();
		for (int i=0;i<numInstances;i++)
		{	    	
			boolean isComplete=true;
			for (int j=0;j<numAttributes-1;j++) // avoid to check last attribute that is always missing here :-)
			{
				isComplete&=!(markedDs.instance(i).isMissing(j));
				if (!isComplete) break;
			}
			
			markedDs.instance(i).setValue(markedDs.numAttributes()-1,isComplete?"no":"yes");
		}
		return markedDs;	    
	}
	
	public static final Instances buildDataSetWithBestAttributes(final Instances ds) throws Exception
	{
		final weka.filters.supervised.attribute.AttributeSelection filter=new weka.filters.supervised.attribute.AttributeSelection();
		final CfsSubsetEval eval=new CfsSubsetEval();
		final GreedyStepwise search=new GreedyStepwise();
		search.setGenerateRanking(true);
		search.setSearchBackwards(true);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		filter.setInputFormat(ds);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildDiscretizedDataSetUnsupervised(final Instances ds) throws Exception 
	{
		final lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize filter=new lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize();
		//filter.setIgnoreClass(true);
		filter.setInputFormat(ds);
		filter.setFindNumBins(true);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildDiscretizedDataSetUnsupervisedForAll(final Instances ds,final int cnt) throws Exception 
	{
		final lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize filter=new lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize();
		//filter.setIgnoreClass(true);
		filter.setInputFormat(ds);
		filter.setBins(cnt);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildDiscretizedDataSetUnsupervisedForOne(final Instances ds,final int idx) throws Exception 
	{
		final lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize filter=new lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize();
		//filter.setIgnoreClass(true);
		filter.setAttributeIndices(""+(idx+1));
		filter.setInputFormat(ds);
		filter.setFindNumBins(true);		
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildDiscretizedDataSetUnsupervised(final Instances ds,final int idx,final int cnt) throws Exception 
	{
		final lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize filter=new lu.lippmann.cdb.weka.filters.UnsupervisedReadableDiscretize();
		//filter.setIgnoreClass(true);
		filter.setAttributeIndices(""+(idx+1));
		filter.setInputFormat(ds);
		filter.setBins(cnt);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildDiscretizedDataSetSupervised(final Instances ds) throws Exception 
	{
		final lu.lippmann.cdb.weka.filters.SupervisedReadableDiscretize filter=new lu.lippmann.cdb.weka.filters.SupervisedReadableDiscretize();
		filter.setInputFormat(ds);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildNumerizedDataSet(final Instances ds) throws Exception 
	{
		final AllToNumeric filter=new AllToNumeric();
		filter.setInputFormat(ds);
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildNormalizedDataSet(final Instances ds) throws Exception 
	{
		final weka.filters.unsupervised.attribute.Normalize filter=new weka.filters.unsupervised.attribute.Normalize();
		filter.setInputFormat(ds);						
		return Filter.useFilter(ds,filter);
	}
	
	public static Instances buildTransposedDataSet(final Instances ds) throws Exception 
	{
		final int numInstances=ds.numInstances();
		
		final ArrayList<Attribute> arrayList=new ArrayList<Attribute>(numInstances);			
		for (int i=0;i<numInstances;i++) arrayList.add(new Attribute("instance" + i));		
		
		final Instances instances=new Instances(ds.relationName()+"Transposed",arrayList, 0);
		
		final int numAttributes=ds.numAttributes();		
		for (int j=0;j<numAttributes;j++)
		{			
			instances.add(new DenseInstance(1,ds.attributeToDoubleArray(j)));
		}

		return instances;
	}
	
	public static final String buildDataSetInStringFormatForCalluna(final Instances ds) throws Exception
	{
		if (WekaDataStatsUtil.areAllAttributesNominal(ds)) 
		{	
			throw new IllegalArgumentException("Impossible to generate a dataset for Calluna when all values are nominal!");
		}

		final int[] rankedAttributes=WekaMachineLearningUtil.computeRankedAttributes(ds);
		final int[] selectedNominalAttributesIndexes=WekaDataStatsUtil.getNominalAttributesIndexes(ds,rankedAttributes);

		final StringBuilder sb=new StringBuilder();

		final int numAttributes=ds.numAttributes();
		for (int i=0;i<numAttributes;i++) 
		{				
			if (ArraysUtil.contains(selectedNominalAttributesIndexes,i)) 
			{
				sb.append(',');				
			}
		}
		sb.append(",");
		for (int i=0;i<numAttributes;i++) 
		{	
			if (!ArraysUtil.contains(selectedNominalAttributesIndexes,i)&&!ds.attribute(i).isDate()) 
			{	
				sb.append(ds.attribute(i).name()).append(',');
			}
		}
		if (sb.charAt(sb.length()-1)==',') sb.setLength(sb.length()-1);
		sb.append('\n');

		int n=0;
		final int numInstances=ds.numInstances();
		for (int k=0;k<numInstances;k++)
		{
			for (int j=0;j<selectedNominalAttributesIndexes.length;j++)
			{
				sb.append(ds.attribute(selectedNominalAttributesIndexes[j]).name())
				.append('-')
				.append(ds.instance(k).stringValue(selectedNominalAttributesIndexes[j]))
				.append(',');				
			}
			sb.append("item"+(n++));

			for (int i=0;i<numAttributes;i++)
			{
				if (!ArraysUtil.contains(selectedNominalAttributesIndexes,i)&&!ds.attribute(i).isDate()) 
				{	
					sb.append(',').append(ds.instance(k).value(i));
				}
			}
			sb.append('\n');										
		}

		return sb.toString();
	}

	public static Instances buildHierarchizedDataSet(final Instances ds) throws Exception
	{
		return buildFilteredByAttributesDataSet(ds,WekaMachineLearningUtil.computeRankedAttributes(ds));
	}

	public static Instances filterDataSetOnNumericValue(final Instances dataSet,final int idx,final double min,final double max) 
	{
		if (idx<0) throw new IllegalStateException();
		if (!dataSet.attribute(idx).isNumeric()) throw new IllegalStateException();

		final Instances newds=new Instances(dataSet,0);		
		final int numInstances=dataSet.numInstances();
		for (int k=0;k<numInstances;k++)
		{
			final double val=dataSet.instance(k).value(idx);
			if (val<=max&&val>=min) newds.add(dataSet.instance(k));
		}
		return newds;
	}

	public static Instances filterDataSetOnNominalValue(final Instances dataSet,final int idx,final String filtervalue) 
	{
		if (idx<0) throw new IllegalStateException();
		if (!dataSet.attribute(idx).isNominal()) throw new IllegalStateException();

		final Instances newds=new Instances(dataSet,0);		
		final int numInstances=dataSet.numInstances();
		for (int k=0;k<numInstances;k++)
		{
			final String val=dataSet.instance(k).stringValue(idx);
			if (val.equals(filtervalue)) newds.add(dataSet.instance(k));
		}
		return newds;
	}

	public static Instances buildNominalizedDataSet(final Instances dataSet) throws Exception 
	{
		final List<Integer> l=WekaDataStatsUtil.getIntegerAttributesIndexes(dataSet);		
		return buildNominalizedDataSet(dataSet,ArraysUtil.transform(l));
	}

	public static Instances buildNormalizedDataSetByAll(final Instances dataSet) throws Exception 
	{
		final double max=WekaDataStatsUtil.getMaxValue(dataSet,null);
		final double min=WekaDataStatsUtil.getMinValue(dataSet,null);
		
		final Instances newds=new Instances(dataSet);
		final int numInstances=newds.numInstances();
		final int numAttributes=newds.numAttributes();
		for (int k=0;k<numInstances;k++)
		{			
			final Instance instanceK=newds.instance(k);
			instanceK.setDataset(newds);
			for (int l=0;l<numAttributes;l++)
			{				
				instanceK.setValue(l,(instanceK.value(l)-min)/(max-min));
			}
		}
		return newds;
	}
	
	public static Instances buildNominalizedDataSet(final Instances dataSet,final int[] attributes) throws Exception 
	{
		final weka.filters.unsupervised.attribute.NumericToNominal filter=new weka.filters.unsupervised.attribute.NumericToNominal();
		filter.setInputFormat(dataSet);
		if (attributes!=null) filter.setAttributeIndicesArray(attributes);
		return Filter.useFilter(dataSet,filter);
	}

	public static Instances buildDataSetWithNumerizedStringAttribute(final Instances dataSet,final int attribute) throws Exception 
	{
		/*if (!dataSet.attribute(attribute).isNominal()||!dataSet.attribute(attribute).isString())
		{
			throw new Exception(dataSet.attribute(attribute).name()+" is not a nominal or a string! -> ");
		}*/
		
		final Instances newds=new Instances(dataSet);		
		newds.insertAttributeAt(new Attribute(dataSet.attribute(attribute).name()+"_num"),newds.numAttributes());		
		final int numInstances=newds.numInstances();
		final int newnumAttributes=newds.numAttributes();
		for (int i=0;i<numInstances;i++)
		{
			newds.instance(i).setValue(newnumAttributes-1,Double.parseDouble(newds.instance(i).stringValue(attribute)));
		}
		return newds;
	}
	
	public static Instances buildDataSetWithoutRowsWithMissingValues(final Instances dataSet) 
	{
		final Instances newds=new Instances(dataSet);		
		final int numInstances=newds.numInstances();
		final int numAttributes=newds.numAttributes();
		final List<Instance> toRemove=new ArrayList<Instance>();
		for (int i=0;i<numInstances;i++)
		{
			for (int j=0;j<numAttributes;j++)
			{
				if (newds.instance(i).isMissing(j))
				{
					toRemove.add(newds.instance(i));					
					break;
				}
			}				
		}				
		for (final Instance ii:toRemove) newds.remove(ii);
		return newds;
	}
	
	public static Instances buildDataSetWithoutRowsWithMissingValues(final Instances dataSet,final List<String> attrNames) 
	{
		final Instances newds=new Instances(dataSet);		
		final int numInstances=newds.numInstances();
		final int numAttributes=newds.numAttributes();
		final List<Instance> toRemove=new ArrayList<Instance>();
		for (int i=0;i<numInstances;i++)
		{
			for (int j=0;j<numAttributes;j++)
			{
				if (attrNames.contains(newds.attribute(j).name())&&newds.instance(i).isMissing(j))
				{
					toRemove.add(newds.instance(i));					
					break;
				}
			}				
		}				
		for (final Instance ii:toRemove) newds.remove(ii);
		return newds;
	}
	
	public static Instances buildDataSetWithoutAttributesWithMissingValues(final Instances dataSet) throws Exception 
	{
		final int numInstances=dataSet.numInstances();
		final int numAttributes=dataSet.numAttributes();
		
		final Set<Integer> toRemove=new HashSet<Integer>();		
//		for (int i=0;i<numInstances;i++)
//		{
//			for (int j=0;j<numAttributes;j++)
//			{
//				if (toRemove.contains(j)) continue;
//				if (dataSet.instance(i).isMissing(j))
//				{
//					toRemove.add(j);
//					break;
//				}
//			}				
//		}
		
		for (int j=0;j<numAttributes;j++)
		{
			for (int i=0;i<numInstances;i++)
			{
				if (dataSet.instance(i).isMissing(j))
				{
					toRemove.add(j);
					break;
				}
			}
		}
		
		final List<Integer> current=new ArrayList<Integer>(numAttributes);
		for (int j=0;j<numAttributes;j++) current.add(j);
		current.removeAll(toRemove);
		final StringBuilder sb=new StringBuilder();
		for (final Integer ii:current) sb.append(ii+1).append(',');
		if (sb.length()>0) sb.setLength(sb.length()-1);
		System.out.println(sb);
		
		final weka.filters.unsupervised.attribute.Reorder filter=new weka.filters.unsupervised.attribute.Reorder();
		filter.setAttributeIndices(sb.toString());		
		filter.setInputFormat(dataSet);
		return Filter.useFilter(dataSet,filter);
	}
	
	public static Instances buildDataSetWithoutDuplicates(final Instances dataSet) 
	{
		final int numInstances=dataSet.numInstances();
		final Set<Instance> l=new TreeSet<Instance>(new InstanceComparator());
		for (int i=0;i<numInstances;i++)
		{
			l.add(dataSet.instance(i));
		}				
		final Instances newds=new Instances(dataSet,0);		
		for (final Instance ii:l) newds.add(ii);
		return newds;
	}
	
	public static Instances buildDataSetWithoutConstantAttributes(final Instances dataSet) throws Exception 
	{
		final weka.filters.unsupervised.attribute.RemoveUseless filter=new weka.filters.unsupervised.attribute.RemoveUseless();
		filter.setInputFormat(dataSet);		
		return Filter.useFilter(dataSet,filter);
	}
		
	public static Instances buildDataSetWithDuplicateAttribute(final Instances dataSet,final int index) 
	{
		final Instances newds=new Instances(dataSet);
		final Attribute newa=newds.attribute(index).copy(newds.attribute(index).name()+"_dupl");		
		newds.insertAttributeAt(newa,newds.numAttributes());		
		int i=0;
		for (double d:newds.attributeToDoubleArray(index))
		{
			newds.instance(i).setValue(newds.numAttributes()-1,d);
			i++;
		}		
		return newds;
	}
	
	public static Instances buildDataSetWithNominalAsBinary(final Instances dataSet,final int idx) throws Exception
	{
		final weka.filters.unsupervised.attribute.NominalToBinary filter=new weka.filters.unsupervised.attribute.NominalToBinary();
		filter.setAttributeIndices(""+(idx+1));
		filter.setInputFormat(dataSet);		
		return Filter.useFilter(dataSet,filter);
	}

	public static Instances buildDataSetWithUnsupervisedMergeNominalValues(final Instances dataSet,final int idx) throws Exception
	{
		final weka.filters.unsupervised.attribute.MergeInfrequentNominalValues filter=new weka.filters.unsupervised.attribute.MergeInfrequentNominalValues();
		filter.setAttributeIndices(""+(idx+1));
		filter.setInputFormat(dataSet);
		//filter.setDebug(true);
		return Filter.useFilter(dataSet,filter);
	}
	
	public static Instances buildDataSetWithSupervisedMergeNominalValues(final Instances dataSet,final int idx) throws Exception
	{
		final weka.filters.supervised.attribute.MergeNominalValues filter=new weka.filters.supervised.attribute.MergeNominalValues();
		filter.setAttributeIndices(""+(idx+1));
		filter.setInputFormat(dataSet);
		//filter.setDebug(true);
		return Filter.useFilter(dataSet,filter);
	}
	
	public static Instances renameAttribute(final Instances dataSet,final int idx,final String newname) throws Exception
	{
		weka.filters.unsupervised.attribute.RenameAttribute filter=new weka.filters.unsupervised.attribute.RenameAttribute();
		filter.setAttributeIndices(""+(idx+1));
		filter.setReplace(newname);
		filter.setInputFormat(dataSet);		
		return Filter.useFilter(dataSet,filter);
	}
	
	public static Instances afterDate(final Instances dataSet,final int year,final int month,final int day,final int hour,final int minute,final int second) throws Exception 
	{
		final int firstDateIdx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
		if (firstDateIdx==-1) 
		{	
			throw new Exception("No date attribute in this dataset!");			
		}
		
		final Calendar cal=Calendar.getInstance();
		cal.set(Calendar.YEAR,year);
		cal.set(Calendar.MONTH,month-1);
		cal.set(Calendar.DAY_OF_MONTH,day);
		cal.set(Calendar.HOUR_OF_DAY,hour);
		cal.set(Calendar.MINUTE,minute);
		cal.set(Calendar.SECOND,second);
		
		return WekaDataProcessingUtil.filterDataSetOnNumericValue(dataSet,firstDateIdx,cal.getTimeInMillis(),Double.POSITIVE_INFINITY);				
	}	
	
	
	//
	// Inner classes
	//
	
	static final class AttributeDesc
	{
		private final int idx;
		private final double[] array;
		
		AttributeDesc(final int idx,final double[] array)
		{
			this.idx=idx;
			MathsUtil.normalize(array);
			this.array=array;			
		}

		public int getIdx() 
		{
			return idx;
		}

		public double[] getArray() 
		{
			return array;
		}
	}

	
}
