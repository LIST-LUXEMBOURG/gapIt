/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.io.File;
import java.util.*;
import java.util.regex.*;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.dt.DecisionTree;
import lu.lippmann.cdb.dt.weka.J48DecisionTreeFactory;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.*;
import weka.attributeSelection.SymmetricalUncertAttributeEval;
import weka.core.*;
import edu.uci.ics.jung.graph.Graph;


/**
 * Weka utility class.
 *
 * @author the ACORA team
 */
public final class WekaDataStatsUtil 
{
	//
	// Static fields
	//

	/** */
	private static final double EPSILON = 0.01d;


	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private WekaDataStatsUtil() {}


	//
	// Static methods
	//

	public static int getFirstAttributeWithMissingValue(final Instances newds)
	{
		int attrWithMissingIdx=-1;		
		for (int i=0;i<newds.numInstances();i++)
		{			
			for (int j=0;j<newds.numAttributes();j++) 
			{
				if (newds.instance(i).isMissing(j)) 
				{	
					attrWithMissingIdx=j;
					break;
				}
			}
			if (attrWithMissingIdx!=-1) break;
		}
		return attrWithMissingIdx;
	}
	
	public static boolean isInteger(final Instances dataSet,final int idx)
	{
		return dataSet.attribute(idx).isNumeric()&&dataSet.attributeStats(idx).realCount==0;
	}

	public static List<Integer> getIntegerAttributesIndexes(final Instances ds)
	{
		final List<Integer> l=new ArrayList<Integer>();		
		for (int k=0;k<ds.numAttributes();k++)
		{
			if (isInteger(ds,k)) l.add(k);
		}		
		return l;		
	}

	public static Set<String> getPresentValuesForNominalAttribute(final Instances dataSet,final int idx)
	{
		if (!dataSet.attribute(idx).isNominal()) throw new IllegalArgumentException();
		final Set<String> l=new HashSet<String>();
		final int numInstances=dataSet.numInstances();		
		for (int k=0;k<numInstances;k++)
		{
			try
			{
				l.add(dataSet.instance(k).stringValue(idx));
			}
			catch(IndexOutOfBoundsException e) { /* case of missing values */ }
		}
		return l;
	}

	public static List<String> getDateAttributeNames(final Instances dataset) 
	{
		final List<String> l=new ArrayList<String>();
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			if (dataset.attribute(k).isDate()) l.add(dataset.attribute(k).name());			
		}
		return l;
	}

	public static List<Integer> getDateAttributeIndexes(final Instances dataset) 
	{
		final List<Integer> l=new ArrayList<Integer>();
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			if (dataset.attribute(k).isDate()) l.add(k);			
		}
		return l;
	}

	public static List<String> getAttributeNames(final Instances dataset) 
	{
		final int numAttributes=dataset.numAttributes();
		final List<String> l=new ArrayList<String>(numAttributes);		
		for (int k=0;k<numAttributes;k++)
		{
			l.add(dataset.attribute(k).name());			
		}
		return l;
	}

	public static int getFirstDateAttributeIdx(final Instances dataset) 
	{
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			if (dataset.attribute(k).isDate()) return k;			
		}
		return -1;
	}

	public static long[] getMinMaxForAttribute(final Instances dataSet,final int idx)
	{
		double min=Double.POSITIVE_INFINITY;
		double max=Double.NEGATIVE_INFINITY;
		final int numInstances=dataSet.numInstances();
		for (int k=0;k<numInstances;k++)
		{
			final double val=dataSet.instance(k).value(idx);
			if (val<min) min=val;
			if (val>max) max=val;
		}
		if (Double.isInfinite(min)||Double.isInfinite(max)) return new long[]{0l,0l};

		return new long[]{(long)min,(long)max};
	}
	
	public static double[] getMinMaxForAttributeAsArrayOfDoubles(final Instances dataSet,final int idx)
	{
		double min=Double.POSITIVE_INFINITY;
		double max=Double.NEGATIVE_INFINITY;
		final int numInstances=dataSet.numInstances();
		for (int k=0;k<numInstances;k++)
		{
			final double val=dataSet.instance(k).value(idx);
			if (val<min) min=val;
			if (val>max) max=val;
		}
		if (Double.isInfinite(min)||Double.isInfinite(max)) return new double[]{0d,0d};

		return new double[]{min,max};
	}

	public static boolean areAllAttributesNominal(final Instances dataset) 
	{
		boolean r=true;
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			r&=(dataset.attribute(k).isNominal());
		}
		return r;
	}

	public static List<Attribute> getNominalAttributesList(final Instances dataset)
	{
		final List<Attribute> l=new ArrayList<Attribute>();	
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			if (dataset.attribute(k).isNominal()) l.add(dataset.attribute(k));
		}
		return l;
	}

	public static boolean areAllNonClassAttributesNumeric(final Instances dataset) 
	{
		boolean r=true;
		final int numAttributes=dataset.numAttributes();
		for (int k=0;k<numAttributes;k++)
		{
			if (k!=dataset.classIndex()) r&=(dataset.attribute(k).isNumeric());
		}
		return r;
	}

	public static int[] getNominalAttributesIndexes(final Instances ds,final int[] indexes) throws Exception
	{
		final List<Integer> l=new ArrayList<Integer>();		
		for (int k=0;k<indexes.length;k++)
		{
			if (ds.attribute(indexes[k]).isNominal()) l.add(indexes[k]);
		}

		final int ls=l.size();
		final int[] r=new int[ls];
		for (int i=0;i<ls;i++) r[i]=l.get(i).intValue();
		return r;		
	}

	public static int[] getNominalAttributesIndexes(final Instances dss) throws Exception
	{
		final List<Integer> l=new ArrayList<Integer>();		
		for (int k=0;k<dss.numAttributes();k++)
		{
			if (dss.attribute(k).isNominal()) l.add(k);
		}

		final int ls=l.size();
		final int[] r=new int[ls];
		for (int i=0;i<ls;i++) r[i]=l.get(i).intValue();
		return r;		
	}

	public static int[] getNumericAttributesIndexesAsArray(final Instances dss) throws Exception
	{
		final List<Integer> l=getNumericAttributesIndexes(dss);		
		final int ls=l.size();
		final int[] r=new int[ls];
		for (int i=0;i<ls;i++) r[i]=l.get(i).intValue();
		return r;		
	}

	public static List<Integer> getNumericAttributesIndexes(final Instances ds)
	{
		final List<Integer> l=new ArrayList<Integer>();		
		for (int k=0;k<ds.numAttributes();k++)
		{
			if (ds.attribute(k).isNumeric()&&!ds.attribute(k).isDate()) l.add(k);
		}		
		return l;		
	}

	public static List<String> getNumericAttributesNames(final Instances ds)
	{
		final List<String> l=new ArrayList<String>();		
		for (int k=0;k<ds.numAttributes();k++)
		{
			if (ds.attribute(k).isNumeric()&&!ds.attribute(k).isDate()) l.add(ds.attribute(k).name());
		}		
		return l;		
	}

	public static List<String> getNominalAttributesNames(final Instances ds)
	{
		final List<String> l=new ArrayList<String>();		
		for (int k=0;k<ds.numAttributes();k++)
		{
			if (ds.attribute(k).isNominal()||ds.attribute(k).isString()) l.add(ds.attribute(k).name());
		}		
		return l;		
	}

	public static Map<Object,Integer> getClassRepartition(final Instances dataSet)
	{
		if (dataSet.classIndex()<0) throw new IllegalArgumentException();
		return getNominalRepartition(dataSet,dataSet.classIndex());
	}

	public static Map<Object,Integer> getNominalRepartition(final Instances dataSet,final int idx)
	{
		/* known values*/
		final Map<Object,Integer> m=new LinkedHashMap<Object,Integer>();
		final Attribute attribute=dataSet.attribute(idx);
		final AttributeStats attributeStats=dataSet.attributeStats(idx);
		int sum=0;
		for (int i=0;i<attributeStats.nominalCounts.length;i++)
		{			
			m.put(attribute.value(i),attributeStats.nominalCounts[i]);
			sum+=attributeStats.nominalCounts[i];
		}
		/* unk values */
		int c=dataSet.numInstances()-sum;
		if (c>0) m.put("?",c);
		return m;
	}

	public static double[] getClassRepartitionForComputing(final Instances dataSet)
	{
		final Map<Object,Integer> initRep=getClassRepartition(dataSet);
		final double[] r=new double[initRep.size()];
		final Iterator<Integer> iter=initRep.values().iterator();
		int k=0;
		while (iter.hasNext())
		{
			r[k]=(double)iter.next().intValue()/(double)dataSet.numInstances();
			k++;
		}
		/* TODO: add unk values? */
		return r;
	}

	public static Map<Object,String> getNominalRepartitionForDescription(final Instances dataSet,final int idx)
	{
		final Map<Object,Integer> initRep=getNominalRepartition(dataSet,idx);
		final int numInstances=dataSet.numInstances();

		final Map<Object,String> m=new HashMap<Object,String>();
		for (Map.Entry<Object,Integer> entry:initRep.entrySet())
		{
			m.put(entry.getKey(),entry.getValue()+" ("+FormatterUtil.DECIMAL_FORMAT.format(100d*entry.getValue().intValue()/numInstances)+"%)");
		}
		/* TODO: add unk values? */
		return m;
	}


	public static int getCountOfMissingValues(final Instances dataset) 
	{
		final int numInstances=dataset.numInstances();
		final int numAttributes=dataset.numAttributes();

		int c=0;
		for (int i=0;i<numInstances;i++)
		{
			for (int j=0;j<numAttributes;j++)
			{
				if (dataset.instance(i).isMissing(j)) c++;
			}				
		}
		return c;
	}	

	/**
	 * TODO : Check that dataset is consistent with graph.
	 */
	public static final Object[] buildNodeAndEdgeRepartitionMap(final Graph<CNode,CEdge> graph,final Instances dataSet) throws IllegalStateException
	{
		final List<CNode> roots = GraphUtil.getRoots(graph);
		if(roots.size() > 1) throw new IllegalStateException("The graph must have only one root!");
		final CNode root = roots.get(0);
		final Map<CNode,Map<Object,Integer>> mapNode = new HashMap<CNode, Map<Object,Integer>>();
		final Map<CEdge,Float> 			    mapEdge = new HashMap<CEdge,Float>();
		buildNodeAndEdgeRepartitionMap(graph, root, dataSet,mapNode,mapEdge,0);

		//Normalize mapEdge
		int max=0;
		for(CEdge edge : mapEdge.keySet()){
			int count = mapEdge.get(edge).intValue();
			if(count > max) { max = count; }
		}
		for(CEdge edge : mapEdge.keySet()){
			mapEdge.put(edge, (mapEdge.get(edge)+0f)/max);
		}
		return new Object[]{mapNode,mapEdge};
	}

	/**
	 * 
	 * @param graph
	 * @param dataset
	 * @return
	 * @throws IllegalStateException 
	 */
	private static final void buildNodeAndEdgeRepartitionMap(final Graph<CNode,CEdge> graph,final CNode currentNode,final Instances dataSet,Map<CNode,Map<Object,Integer>> mapNode,Map<CEdge,Float> mapEdge,int depth) throws IllegalStateException{

		final String cmp = "(>=|<=|>|<|==|!=)";
		final Pattern nomP  = Pattern.compile("(.+)\\s*"+cmp+"\\s*'(.+)'");
		final Pattern numP  = Pattern.compile("(.+)\\s*"+cmp+"\\s*(.+)");
		Matcher m1,m2;

		//System.out.println("Current node : (" + depth +")" + currentNode + ",Size="+dataSet.size());

		mapNode.put(currentNode,WekaDataStatsUtil.getClassRepartition(dataSet));

		if(graph==null || graph.getVertices().isEmpty())
		{
			throw new IllegalStateException("Graph cannot be null");
		}

		for(CEdge e : graph.getOutEdges(currentNode)){
			//E.g. age >= 13  or pays = 'Luxembourg'
			final String expression = e.getExpression();
			boolean foundM2=false;
			m1 = nomP.matcher(expression);
			m2 = numP.matcher(expression);
			if(m1.find() || (foundM2=m2.find()))
			{
				final String attributeName = foundM2?m2.group(1):m1.group(1);
				final Attribute attribute = dataSet.attribute(attributeName); 
				if(attribute == null) throw new IllegalStateException("Attribute not found:"+attributeName);
				final int idx = attribute.index();
				final String comp		  = foundM2?m2.group(2):m1.group(2);
				final String value        = foundM2?m2.group(3):m1.group(3);
				Instances filteredInstances = null;
				if(attribute.isNominal())
				{
					if(comp.equals("=="))
					{	//Remove quotes in nominal value
						filteredInstances = WekaDataProcessingUtil.filterDataSetOnNominalValue(dataSet, idx, value.replaceAll("'",""));	
					}else // (in particular !=) 
					{
						throw new IllegalStateException("Not implemented !");
					}

				}else if(attribute.isNumeric()){
					final AttributeStats stats = dataSet.attributeStats(idx);
					double min  = stats.numericStats.min;
					double max  = stats.numericStats.max;
					if(comp.equals("=="))
					{
						min = max = Double.valueOf(value);
					}
					else if(comp.equals(">"))
					{
						min = Double.valueOf(value) + EPSILON;
					}
					else if(comp.equals("<"))
					{
						max = Double.valueOf(value) - EPSILON;
					}
					else if(comp.equals(">="))
					{
						min = Double.valueOf(value);
					}
					else if(comp.equals("<="))
					{
						max = Double.valueOf(value);
					}
					else if(comp.equals("!="))
					{
						throw new IllegalStateException("Not implemented !");
					}
					filteredInstances = WekaDataProcessingUtil.filterDataSetOnNumericValue(dataSet, idx, min, max);
				}
				mapEdge.put(e, Float.valueOf(filteredInstances.size()));
				buildNodeAndEdgeRepartitionMap(graph,graph.getDest(e),filteredInstances,mapNode,mapEdge,depth+1);
			}
		}
	}

	public static void checkAttributesNames(final Instances ds) throws Exception
	{
		final String[] NOT_ALLOWED_STRING=new String[]{"-"," "};
		final int numAttributes=ds.numAttributes();
		for (int i=0;i<numAttributes;i++)
		{
			final String attrName=ds.attribute(i).name();
			if (Character.isDigit(attrName.charAt(0))) 
			{	
				throw new Exception("An attribute name should not begin with a digit! ("+attrName+")");
			}
			for (final String s:NOT_ALLOWED_STRING)
			{
				if (attrName.contains(s))
				{	
					throw new Exception("An attribute name should not contain '"+s+"' ! ("+attrName+")");
				}
			}
		}
	}

	/**
	 * Doesn't work if class is unary (numeric is discretized)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public static Map<Attribute,Double> computeSymmetricUncertaintyCorrelation(final Instances ds) throws Exception
	{
		return WekaDataStatsUtil.computeSymmetricUncertaintyCorrelation(ds,null);
	}

	/**
	 * Doesn't work if class is unary (numeric is discretized)
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public static Map<Attribute,Double> computeSymmetricUncertaintyCorrelation(Instances ds,final List<Attribute> coreFeatures) throws Exception
	{
		//Disretize numeric class if needed
		if(ds.classIndex()!=-1&&ds.attribute(ds.classIndex()).isNumeric()){
			final int classIndex = ds.classIndex();
			ds.setClassIndex(-1);
			ds = WekaDataProcessingUtil.buildDiscretizedDataSetUnsupervised(ds,classIndex,5);
			ds.setClassIndex(classIndex);
		}
		//Compute coefficients
		final Map<Attribute,Double> res = new LinkedHashMap<Attribute,Double>();
		final SymmetricalUncertAttributeEval smuae = new SymmetricalUncertAttributeEval();
		smuae.buildEvaluator(ds);
		final int M = ds.numAttributes();
		for(int j = 0 ; j < M ; j++){
			if((coreFeatures==null||!coreFeatures.contains(ds.attribute(j))) && j!=ds.classIndex())
			{
				res.put(ds.attribute(j),smuae.evaluateAttribute(j));	
			}
		}
				
		return MapsUtil.sortByValue(res);
	}

	public static double getMaxValue(final Instances ds,final List<String> attrNames)
	{
		double max=Double.NEGATIVE_INFINITY;
		for (int i=0;i<ds.numAttributes();i++)
		{
			if (ds.attribute(i).isDate()||ds.attribute(i).isNominal()||ds.attribute(i).isString()) continue;
			
			if (attrNames!=null&&attrNames.contains(ds.attribute(i).name())) continue;
			for (int j=0;j<ds.numInstances();j++)
			{
				 final double val=ds.instance(j).value(i);
				 if (val>max) max=val;
			}
		}
		return max;
	}
	
	public static double getMinValue(final Instances ds,final List<String> attrNames)
	{
		double min=Double.POSITIVE_INFINITY;
		for (int i=0;i<ds.numAttributes();i++)
		{
			if (ds.attribute(i).isDate()||ds.attribute(i).isNominal()||ds.attribute(i).isString()) continue;
			
			if (attrNames!=null&&attrNames.contains(ds.attribute(i).name())) continue;
			for (int j=0;j<ds.numInstances();j++)
			{
				 final double val=ds.instance(j).value(i);
				 if (val<min) min=val;
			}
		}
		return min;
	}
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		final Instances ds = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File("./samples/csv/bank.csv"));
		final J48DecisionTreeFactory dtf = new J48DecisionTreeFactory(0.5d,false);
		final DecisionTree dtree = dtf.buildDecisionTree(ds);
		final Graph<CNode,CEdge> graph = dtree.getGraphWithOperations();
		Object[] res = buildNodeAndEdgeRepartitionMap(graph, ds);
		Map<CNode,Map<Object,Integer>> mapNode = (Map<CNode,Map<Object,Integer>>)res[0];
		Map<CEdge,Integer> mapEdge 			   = (Map<CEdge,Integer>)res[1];
		for(final CNode node : mapNode.keySet()){
			System.out.println("Node : " + node + "->" + mapNode.get(node));
		}

		for(final CEdge edge : mapEdge.keySet()){
			System.out.println("Edge : " + edge + "->" + mapEdge.get(edge));
		}

	}






}

