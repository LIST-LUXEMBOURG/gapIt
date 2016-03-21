/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.beta.util;

import java.util.*;

import lu.lippmann.cdb.lab.beta.*;
import lu.lippmann.cdb.lab.beta.shih.TupleSI;
import weka.clusterers.*;
import weka.core.*;


/**
 * 
 * 
 * @author 
 */
public final class WekaUtil2 {


	private WekaUtil2() { }

	/**
	 * 
	 * @param newInstances
	 * @param K
	 * @return
	 * @throws Exception
	 */
	public static double[] doKMeans(final Instances newInstances, final int K) throws Exception {
		final SimpleKMeans clusterer=new SimpleKMeans();
		clusterer.setOptions(Utils.splitOptions("-N " + K +" -R first-last -I 500 -S 10 -A weka.core.EuclideanDistance"));

		clusterer.buildClusterer(newInstances);

		final ClusterEvaluation eval=new ClusterEvaluation();
		eval.setClusterer(clusterer);
		eval.evaluateClusterer(newInstances);

		double[] ass = eval.getClusterAssignments();
		return ass;
	}

	/**
	 * 
	 * @param newInstances
	 * @param K
	 * @return
	 * @throws Exception
	 */
	public static List<IndexedInstance> doHAC(final Instances instances, final int K) throws Exception {
		final HierarchicalClusterer clusterer=new HierarchicalClusterer();
		clusterer.setOptions(Utils.splitOptions("-N " + K +" -L MEAN -P -A weka.core.EuclideanDistance"));
		return computeClusters(clusterer,instances);
	}	
	
	/**
	 * Generate the centroid coordinates based 
	 * on it's  members (objects assigned to the cluster of the centroid) and the distance 
	 * function being used.
	 * @return the centroid
	 */
	public static MixedCentroid computeMixedCentroid(final boolean preserveOrder,final NormalizableDistance distanceFunction,final Instances numericInstances,final Instances originalInstances,final int clusterIndex) 
	{
		final int numInstances=numericInstances.numInstances();
		final int numAttributes=numericInstances.numAttributes();

		final Map<TupleSI,Integer> addedAttr = new HashMap<TupleSI, Integer>();

		if (numInstances==1){
			Instance uniqueNumInstance = numericInstances.firstInstance();
			Instance uniqueMixInstance = originalInstances.firstInstance();
			double[] centroid = uniqueNumInstance.toDoubleArray();
			for(int i = 0 ; i < uniqueMixInstance.numAttributes() ; i++){
				if(!uniqueMixInstance.attribute(i).isNumeric()){
					final String catVal = uniqueMixInstance.attribute(i).value((int)uniqueMixInstance.value(i));
					addedAttr.put(new TupleSI(catVal,i),1);
				}
			}
			return new MixedCentroid(clusterIndex,centroid, addedAttr);
		}


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
			if (preserveOrder) {sortedMembers = numericInstances;} else{sortedMembers = new Instances(numericInstances);}
		}

		for (int j = 0; j < numAttributes; j++) 
		{
			//in case of Euclidian distance the centroid is the mean point
			//in case of Manhattan distance the centroid is the median point
			//in both cases, if the attribute is nominal, the centroid is the mode            
			if (isEuclideanDist){
				vals[j] = numericInstances.meanOrMode(j);

				for(int i = 0 ; i < numInstances ; i++){
					if(!originalInstances.attribute(j).isNumeric()){
						final Instance instance = originalInstances.instance(i);
						final String catVal = instance.attribute(j).value((int)instance.value(j));
						//Initialize map
						final TupleSI key = new TupleSI(catVal, j);
						if(!addedAttr.containsKey(key))	addedAttr.put(key, 0);
						addedAttr.put(key, addedAttr.get(key)+1);
					}
				}
			} 
			else if (isManhattanDist) 
			{
				sortedMembers.kthSmallestValue(j, middle + 1);
				vals[j] = sortedMembers.instance(middle).value(j);
				if (dataIsEven) 
				{
					sortedMembers.kthSmallestValue(j, middle + 2);
					vals[j] = (vals[j] + sortedMembers.instance(middle + 1).value(j)) / 2;
				}
			}else{
				throw new IllegalStateException("Not handled distance ...");
			}
		}

		return new MixedCentroid(clusterIndex,vals,addedAttr);
	}

	/**
	 * 
	 * @param instances
	 * @param instance
	 */
	public static void removeFromInstances(Instances instances,Instance instance){
		InstanceComparator cp = new InstanceComparator();
		for(int i = 0 ; i < instances.numInstances() ; i++){
			Instance cinstance = instances.instance(i);
			if(cp.compare(cinstance, instance)==0){
				instances.remove(cinstance);
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param wekaClusterer
	 * @param instances
	 * @return
	 * @throws Exception
	 */
	public static List<IndexedInstance> computeClusters(final Clusterer wekaClusterer,final Instances instances) throws Exception 
	{	
		final Instances ii=new Instances(instances);
		ii.setClassIndex(-1); 	

		wekaClusterer.buildClusterer(ii);

		final ClusterEvaluation eval=new ClusterEvaluation();
		eval.setClusterer(wekaClusterer);
		eval.evaluateClusterer(ii);		

		final int clustersCount=eval.getNumClusters();		
		final List<IndexedInstance> clustersList=new ArrayList<IndexedInstance>(clustersCount);
		
		//Initialize instances
		for (int k=0;k<clustersCount;k++){
			clustersList.add(new IndexedInstance(new Instances(instances,0),new HashMap<Integer, Integer>()));		
		}

		final double[] ass=eval.getClusterAssignments();
		if (ass.length!=ii.numInstances()) throw new IllegalStateException();
		for (int i=0;i<ass.length;i++) 
		{	
			IndexedInstance idxi = clustersList.get((int)ass[i]);
			idxi.getInstances().add(instances.instance(i));
			int pos = idxi.getInstances().size()-1;
			idxi.getMapOrigIndex().put(pos,i);
		}

		return clustersList;
	}

}
