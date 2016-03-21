/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;
import java.util.logging.Logger;

import lu.lippmann.cdb.dsl.GraphDsl;
import lu.lippmann.cdb.dt.DecisionTree;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import weka.attributeSelection.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.clusterers.*;
import weka.core.*;
import weka.filters.Filter;


/**
 * Weka utility class.
 *
 * @author the ACORA team
 */
public final class WekaMachineLearningUtil 
{
	//
	// Static fields
	//
	
	/** Logger. */
	private static final Logger LOGGER=Logger.getLogger(WekaMachineLearningUtil.class.toString());

	
	//
	// Constructorslol
	//

	/**
	 * Private constructor.
	 */
	private WekaMachineLearningUtil() {}
	
	
	//
	// Static methods
	//
	
	public static List<String> computeJRipRules(final Instances dataset) throws Exception
	{
		LOGGER.info("Weka rules discovering (JRip) ...");

		final JRip rip=new JRip(); 
		rip.buildClassifier(dataset);		
		final String s=rip.toString();									
		
		final List<String> rules=new ArrayList<String>();
		if (s!=null)
		{
			final Scanner sc=new Scanner(s).useDelimiter("\n");
			while (sc.hasNext())
			{				
				final String line=sc.next();
				if (line.startsWith("(")||line.startsWith(" =>")) rules.add(line);				
			}
			sc.close();
		}
		
		LOGGER.info("... Weka rules discovering (JRip) finished");
		
		return rules;
	}
	
	/**
	 * Generate the centroid coordinates based 
	 * on it's  members (objects assigned to the cluster of the centroid) and the distance 
	 * function being used.
	 * @return the centroid
	 */
	public static Instance computeCentroid(final boolean preserveOrder,final NormalizableDistance distanceFunction,final Instances members) 
	{
		if (members.numInstances()==1) return members.firstInstance();

		final int numAttributes=members.numAttributes();
		final int numInstances=members.numInstances();

		final double[] vals = new double[numAttributes];

		//used only for Manhattan Distance
		Instances sortedMembers = null;
		int middle = 0;
		boolean dataIsEven = false;

		final boolean isManhattanDist=(distanceFunction instanceof ManhattanDistance);
		final boolean isEuclideanDist=(distanceFunction instanceof EuclideanDistance);

		if (isManhattanDist) 
		{
			middle = (numInstances - 1) / 2;
			dataIsEven = ((numInstances % 2) == 0);
			if (preserveOrder) 
			{
				sortedMembers = members;
			} 
			else 
			{
				sortedMembers = new Instances(members);
			}
		}

		for (int j = 0; j < numAttributes; j++) 
		{
			//in case of Euclidian distance the centroid is the mean point
			//in case of Manhattan distance the centroid is the median point
			//in both cases, if the attribute is nominal, the centroid is the mode            
			if (isEuclideanDist || members.attribute(j).isNominal()) 
			{
				vals[j] = members.meanOrMode(j);
			} 
			else if (isManhattanDist) 
			{
				//singleton special case
				if (numInstances == 1) 
				{
					vals[j] = members.instance(0).value(j);
				} 
				else 
				{
					sortedMembers.kthSmallestValue(j, middle + 1);
					vals[j] = sortedMembers.instance(middle).value(j);
					if (dataIsEven) 
					{
						sortedMembers.kthSmallestValue(j, middle + 2);
						vals[j] = (vals[j] + sortedMembers.instance(middle + 1).value(j)) / 2;
					}
				}
			}
		}

		return new DenseInstance(1d,vals);
	}
	
	/**
	 * 
	 */
	public static WekaClusteringResult computeClusters(final Clusterer wekaClusterer,final Instances instances) throws Exception 
	{	
		final Instances ii=new Instances(instances);
		ii.setClassIndex(-1); // fixme: hmm hmm		

		LOGGER.info("Weka clustering ("+wekaClusterer.getClass().getName()+") ...");
		wekaClusterer.buildClusterer(ii);
		final ClusterEvaluation eval=new ClusterEvaluation();
		eval.setClusterer(wekaClusterer);
		eval.evaluateClusterer(ii);		
		LOGGER.info("... weka clustering finished (count="+eval.getNumClusters()+")");

		return new WekaClusteringResult(eval.getClusterAssignments(),computeClusters(eval.getClusterAssignments(),eval.getNumClusters(),instances));
	}
	

	
	/**
	 * 
	 */
	public static List<Instances> computeClusters(final double[] ass,final int clustersCount,final Instances instances) throws Exception 
	{	
		final List<Instances> clustersList=new ArrayList<Instances>(clustersCount);
		for (int k=0;k<clustersCount;k++) clustersList.add(new Instances(instances,0));		

		if (ass.length!=instances.numInstances()) throw new IllegalStateException();
		for (int i=0;i<ass.length;i++) 
		{	
			clustersList.get((int)ass[i]).add(new DenseInstance(1.0d,instances.instance(i).toDoubleArray()));		
		}

		return clustersList;
	}
	
	public static Instances computeSortedByClustersDataSet(final double[] ass,final int clustersCount,final Instances instances) throws Exception
	{
		final List<Instances> l=computeClusters(ass,clustersCount,instances);		
		final Instances newds=l.get(0);
		for (int i=1;i<clustersCount;i++)
		{
			final Instances currentInstances=l.get(i);
			for (int j=0;j<currentInstances.numInstances();j++)
			{
				newds.add(currentInstances.instance(j));
			}
		}
		return newds;
	}
	
	/**
	 * 
	 */
	public static int[] computeBestAttributes(final Instances sampleDataSet) throws Exception
	{
	    final AttributeSelection attsel=new AttributeSelection();
	    final CfsSubsetEval eval=new CfsSubsetEval();
	    final GreedyStepwise search=new GreedyStepwise();
	    search.setGenerateRanking(true);
	    search.setSearchBackwards(true);
	    attsel.setEvaluator(eval);
	    attsel.setSearch(search);
	    attsel.SelectAttributes(sampleDataSet);
	    return attsel.selectedAttributes();
	}	
	
	/**
	 * 
	 */
	public static int[] computeRankedAttributes(final Instances sampleDataSet) throws Exception
	{
	    final AttributeSelection attsel=new AttributeSelection();
	    final InfoGainAttributeEval eval=new InfoGainAttributeEval();
	    final Ranker search=new Ranker();
	    search.setGenerateRanking(true);
	    attsel.setEvaluator(eval);
	    attsel.setSearch(search);
	    attsel.SelectAttributes(sampleDataSet);
	    return attsel.selectedAttributes();
	}	
	
	/**
	 * 
	 */
	public static double[] computeInfoGainEvaluation(final Instances sampleDataSet) throws Exception
	{
		if (sampleDataSet.classIndex()==-1) throw new IllegalStateException();
		
	    final AttributeSelection attsel=new AttributeSelection();
	    final InfoGainAttributeEval eval=new InfoGainAttributeEval();
	    final Ranker search=new Ranker();
	    search.setGenerateRanking(true);
	    attsel.setEvaluator(eval);
	    attsel.setSearch(search);
	    attsel.SelectAttributes(sampleDataSet);
	    
	    final double[] scores=new double[sampleDataSet.numAttributes()];
	    for (int i=0;i<scores.length;i++) 
	    {	
	    	scores[i]=eval.evaluateAttribute(i);
	    }	    
	    
	    return scores;
	}	
	
	public static Instances computeNearestNeighbours(final Instances ds,final Instance inst,final int n) throws Exception
	{
		return computeNearestNeighbours(ds,inst,n,null);
	}
	
	
	public static Instances computeNearestNeighbours(final Instances ds,final Instance inst,final int n,final String attrs) throws Exception
	{
		if (ds.numAttributes()!=inst.numAttributes()) 
		{	
			throw new IllegalStateException("Not the same count of attributes? ds->"+ds.numAttributes()+" i->"+inst.numAttributes());
		}
		
		if (n>ds.numInstances())
		{
			throw new IllegalStateException("n>size! (n="+n+",size="+ds.numInstances()+")");
		}
		
		final weka.core.neighboursearch.LinearNNSearch knn=new weka.core.neighboursearch.LinearNNSearch(ds);
		final EuclideanDistance distFunction=new EuclideanDistance(ds);
		if (attrs!=null) distFunction.setAttributeIndices(attrs);
		knn.setDistanceFunction(distFunction);		
		final Instances res=knn.kNearestNeighbours(inst,n);
		return res;
	}
	
	public static Instances computeManualNearestNeighbours(final Instances ds,final Instance inst,final int n) throws Exception
	{
		if (ds.numAttributes()!=inst.numAttributes()) 
		{	
			throw new IllegalStateException("Not the same count of attributes? ds->"+ds.numAttributes()+" i->"+inst.numAttributes());
		}
		
		if (n>ds.numInstances())
		{
			throw new IllegalStateException("n>size! (n="+n+",size="+ds.numInstances()+")");
		}
		
		final Instances res=new Instances(ds,0);		
		
		final NormalizableDistance ed=new EuclideanDistance(ds);		
		final Comparator<Instance> comp=new Comparator<Instance>()
		{
			@Override
			public int compare(final Instance i1,final Instance i2) 
			{				
				final double d1=ed.distance(inst,i1);
				final double d2=ed.distance(inst,i2);
				System.out.println(d1+" "+d2);
				if (d1>d2) return 1;
				else if (d2>d1) return -1;
				return 0;
			}
		};
		
		final int numInstances=ds.numInstances();
		final List<Instance> list=new ArrayList<Instance>(numInstances);
		for (int i=0;i<numInstances;i++) list.add(ds.instance(i));				
		Collections.sort(list,comp);		
		for (int i=0;i<n;i++) res.add(list.get(i));		
		return res;
	}
	
	public static AbstractClassifier buildJ48Classifier(final boolean isUnpruned,final int minObjInLeaf,final int numFolds,final double confidenceFactor) throws Exception
	{
		final J48 j48Classifier=new J48();
		if (isUnpruned) 
		{
			j48Classifier.setOptions(Utils.splitOptions("-U -M "+Math.max(3,minObjInLeaf)));
		} 
		else 
		{
			j48Classifier.setOptions(Utils.splitOptions("-C "+confidenceFactor+" -M "+Math.max(3,minObjInLeaf)));
		}		
		return j48Classifier;
	}

	public static EM buildEMClusterer() throws Exception
	{
		final EM clusterer=new EM();
		clusterer.setOptions(Utils.splitOptions("-I 100 -V"));
		return clusterer;
	}

	public static SimpleKMeans buildSimpleKMeansClusterer() throws Exception
	{
		final SimpleKMeans clusterer=new SimpleKMeans();		
		clusterer.setOptions(Utils.splitOptions("-I 100"));
		return clusterer;
	}
	
	public static SimpleKMeans buildSimpleKMeansClustererWithK(final int K) throws Exception
	{
		final SimpleKMeans clusterer=new SimpleKMeans();
		clusterer.setOptions(Utils.splitOptions("-I 100 -N " + K));
		return clusterer;
	}
	
	public static SimpleKMeans buildSimpleKMeansClustererWithK(final int K,final DistanceFunction df) throws Exception
	{
		final SimpleKMeans clusterer=new SimpleKMeans();
		clusterer.setOptions(Utils.splitOptions("-I 100 -N " + K));
		clusterer.setDistanceFunction(df);
		return clusterer;
	}

	public static SimpleKMeans buildSimpleKMeansClustererWithManhattanDistance(final int nb) throws Exception
	{
		final SimpleKMeans clusterer=new SimpleKMeans();
		clusterer.setOptions(Utils.splitOptions("-I 100 -A weka.core.ManhattanDistance -N "+nb));
		return clusterer;
	}

	public static Instances buildDataSetExplainingClustersAssignment(final Instances ds,final double[] ass,final String clusterAttrName) throws Exception
	{
		Instances newds=new Instances(ds);
		newds.insertAttributeAt(new Attribute(clusterAttrName),newds.numAttributes());
		final int numInstances=newds.numInstances();
		final int numAttributes=newds.numAttributes();
		for (int i=0;i<numInstances;i++) newds.instance(i).setValue(numAttributes-1,ass[i]);
		newds=WekaDataProcessingUtil.buildNominalizedDataSet(newds,new int[]{numAttributes-1});
		newds.setClassIndex(numAttributes-1);
		return newds;
	}
	
	@Deprecated
	public static Instances buildDataSetExplainingClustersAssignment(final List<Instances> cl,final String clusterAttrName,final boolean deleteOldClass) throws Exception
	{
		return buildDataSetExplainingClustersAssignment(cl,clusterAttrName,deleteOldClass,true);
	}
	
	@Deprecated
	public static Instances buildDataSetExplainingClustersAssignment(final List<Instances> cl,final String clusterAttrName,final boolean deleteOldClass,final boolean prefixValueWithName) throws Exception
	{			
		/* build an ARFF stream with a new attribute for cluster assignment */
	    final StringBuilder sb=new StringBuilder("@relation newClusteredDataset\n");
	    final Instances sampleDataSet=cl.get(0);
		final int numAttributes=sampleDataSet.numAttributes();
		for (int i=0;i<numAttributes;i++) 
	    {	
	    	sb.append(sampleDataSet.attribute(i)).append('\n');
	    }
	    sb.append("@attribute ").append(clusterAttrName).append("{");
	    for (int i=0;i<cl.size();i++) 
	    {
	    	if (prefixValueWithName) sb.append("cluster");
	    	sb.append(i).append(',');
	    }
	    sb.setLength(sb.length()-1);
	    sb.append("}\n");
	    
	    sb.append("@data\n");
	    
		int n=0;
		for (final Instances cluster:cl)
		{
			final int clusterSize=cluster.numInstances();
			for (int k=0;k<clusterSize;k++)
			{
				sb.append(cluster.instance(k)).append(',');
				if (prefixValueWithName) sb.append("cluster");
				sb.append(n).append('\n');										
			}
			n++;
		}			
		
		/* load the built ARFF stream */
		final Instances newInstances=WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),true);
		if (deleteOldClass) newInstances.deleteAttributeAt(newInstances.numAttributes()-2);
		
		return newInstances;
	}

	public static final List<Integer> computeUnsupervisedFeaturesSelection(final Instances dataSet,final int k) throws Exception
	{
		final List<Integer> r=new ArrayList<Integer>(k);
		
		final Instances trdataSet=WekaDataProcessingUtil.buildTransposedDataSet(dataSet);		
		
		final EuclideanDistance distanceFunction=new EuclideanDistance(trdataSet);
		
		final SimpleKMeans skm=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(k,distanceFunction);			
		skm.buildClusterer(trdataSet);
		final ClusterEvaluation eval=new ClusterEvaluation();
		eval.setClusterer(skm);
		eval.evaluateClusterer(trdataSet);	
		
		final int numAttributes=dataSet.numAttributes();
		final int numClusters=eval.getNumClusters();
		final List<Instances> clusters=WekaMachineLearningUtil.computeClusters(eval.getClusterAssignments(),numClusters,trdataSet);
		for (int i=0;i<numClusters;i++)
		{
			final Instances cluster=clusters.get(i);
			
			final Instance centroid=WekaMachineLearningUtil.computeCentroid(true,distanceFunction,cluster);
			final Instances nearestNeighbours=WekaMachineLearningUtil.computeManualNearestNeighbours(cluster,centroid,1);
			final Instance realCentroid=nearestNeighbours.instance(0);				
				
			int j;			
			for (j=0;j<numAttributes;j++)
			{
				if (trdataSet.instance(j).toString().equals(realCentroid.toString())) break;
			}
			if (j==numAttributes) throw new IllegalStateException("real centroid ("+realCentroid+") of cluster_"+i+" not found? size="+cluster.numInstances());
			//System.out.println("Cluster_"+i+" size="+cluster.numInstances()+"("+dataSet.attribute(j).name()+") -> "+realCentroid);
			r.add(j);
		}

		return r;
	}
	
	public static WekaClusteringResult computeClusterFromAssignmentTree(final Instances instances,final GraphDsl dsl,final String dtInStringFormat) throws Exception 
	{
		if (dtInStringFormat==null||dtInStringFormat.isEmpty())
		{
			throw new Exception("Empty textual definition of clusters assignment!");
		}
		final GraphWithOperations gwo=(GraphWithOperations)dsl.getGraphDslParsingResult(dtInStringFormat).getGraph();		
		
		final double[] ass=new double[instances.numInstances()];
		
		final int MAX_CLUSTERS=20; // FIXME: hmm hmm
		final List<Instances> res=new ArrayList<Instances>(MAX_CLUSTERS);
		/*for (int i=0;i<MAX_CLUSTERS;i++)
		{
			res.add(new Instances(instances,0));
		}
		final int n=instances.numInstances();
		for (int i=0;i<n;i++)
		{
			final Instance instance=instances.get(i);
			final String val=GraphEvaluator.computeEvaluation(gwo,instance).getResult();
			final int idx=Integer.valueOf(val.substring("cluster".length()));
			res.get(idx).add(instance);
			ass[i]=idx;
		}
		
		final Iterator<Instances> iter=res.iterator();
		while (iter.hasNext())
		{
			if (iter.next().numInstances()==0) iter.remove();
		}*/
				
		return new WekaClusteringResult(ass,res);
	}
	
	public static WekaClusteringResult computeClustersFromFixedCluster(final Instances dataset,final GraphDsl dsl,final String dtInStringFormat) throws Exception 
	{
		if (dtInStringFormat==null||dtInStringFormat.isEmpty())
		{
			throw new Exception("Empty textual definition of cluster!");
		}
		
		final GraphWithOperations gwo=(GraphWithOperations)dsl.getGraphDslParsingResult(dtInStringFormat).getGraph();
		
		final double[] ass=new double[dataset.numInstances()];
		
		final List<Instances> l=new ArrayList<Instances>();		
		l.add(new Instances(dataset,0));
		l.add(new Instances(dataset,0));
		
		final int cnt=dataset.numInstances();
		/*for (int i=0;i<cnt;i++)
		{
			final Instance instance=dataset.instance(i);
			try 
			{				
				final String known=instance.stringValue(instance.classIndex());				
				final String predicted=GraphEvaluator.computeEvaluation(gwo,instance).getResult();
				final int idx=known.equals(predicted)?0:1;
				//System.out.println(instance.value(dataset.attribute("age"))+" *"+known+"* *"+predicted+"* "+idx);				
				l.get(idx).add(instance);
				ass[i]=idx;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}	*/
		return new WekaClusteringResult(ass,l);
	}

	public static WekaClusteringResult computeClustersResultFromDT(final DecisionTree dt,final Instances ds,final int idx) throws Exception
	{
		final int numInstances=ds.numInstances();
		
		final double[] ass=new double[numInstances];
		final List<Instances> cl=new ArrayList<Instances>();
		final List<String> possValues=new ArrayList<String>(WekaDataStatsUtil.getPresentValuesForNominalAttribute(ds,idx));
		for (int i=0;i<possValues.size();i++) cl.add(new Instances(ds,0));
		
		/*for (int k=0;k<numInstances;k++)
		{
			final String predicted=GraphEvaluator.computeEvaluation(dt.getGraphWithOperations(),ds.instance(k)).getResult();
			final int clusterIdx=possValues.indexOf(predicted);
			ass[k]=clusterIdx;
			cl.get(clusterIdx).add(ds.instance(k));
		}*/
		
		return new WekaClusteringResult(ass,cl);
	}
	
	public static WekaClusteringResult computeClustersResultFromDT(final DecisionTree dt,final Instances ds) throws Exception
	{
		return computeClustersResultFromDT(dt,ds,ds.classIndex());
	}

	public static WekaClusteringResult computeClustersResultFromOutliersDetection(final Instances ds) throws Exception
	{
		final Instances markedDS=WekaDataProcessingUtil.buildDataSetWithMarkedOutliers(ds);

		final List<Instances> clustersList=new ArrayList<Instances>(2);
		clustersList.add(new Instances(markedDS,0));
		clustersList.add(new Instances(markedDS,0));
		final double[] assignment=new double[markedDS.numInstances()];
		final Attribute isOutlierAttribute=markedDS.attribute(WekaDataProcessingUtil.IS_EXTREME_VALUE_OR_OUTLIER_FEATURE);
		for (int i=0;i<markedDS.numInstances();i++)
		{
			final int idx=(int)markedDS.instance(i).value(isOutlierAttribute);
			clustersList.get(idx).add(markedDS.instance(i));
			assignment[i]=idx;
		}
		for (final Instances clu:clustersList) clu.deleteAttributeAt(isOutlierAttribute.index());
		
		return new WekaClusteringResult(assignment,clustersList);
	}
	
	public static WekaClusteringResult computeClustersResultFromNominalAttributeValues(final Instances ds,final int attridx) throws Exception
	{
		final List<String> l=new ArrayList<String>(WekaDataStatsUtil.getPresentValuesForNominalAttribute(ds,attridx));		
		final List<Instances> clustersList=new ArrayList<Instances>(l.size());
		for (int lc=0;lc<l.size();lc++)
		{
			clustersList.add(new Instances(ds,0));
		}
		final double[] assignment=new double[ds.numInstances()];
		for (int i=0;i<ds.numInstances();i++)
		{
			final int idx=l.indexOf(ds.instance(i).stringValue(attridx));
			clustersList.get(idx).add(ds.instance(i));
			assignment[i]=idx;
		}
		
		return new WekaClusteringResult(assignment,clustersList);
	}
	
	public static Instances buildDataSetWithCentroidDistanceAsNewFeature(final Instances ds,final NormalizableDistance df)
	{
		final Instances newds=new Instances(ds);
		String dtcName="__distToCentroid__";
		while (ds.attribute(dtcName)!=null) dtcName+="_"; 
		newds.insertAttributeAt(new Attribute(dtcName),newds.numAttributes());
		
		final Instance centroid=computeCentroid(true,df,ds);
		
		final int numInstances=newds.numInstances();
		for (int i=0;i<numInstances;i++)
		{						
			final double dist=df.distance(ds.instance(i),centroid);
			//System.out.println(dist);
			newds.instance(i).setValue(newds.attribute(dtcName),dist);
		}
		
		return newds;
	}
}
