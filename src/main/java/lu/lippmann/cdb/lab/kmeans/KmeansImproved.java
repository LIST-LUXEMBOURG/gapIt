/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.kmeans;

import weka.clusterers.*;
import weka.core.*;


/**
 * 
 * 
 * @author
 */
public class KmeansImproved {

	public static final int DEFAULT_NUM_CLUSTERS_MAX = 10;
	private int maxClusters;

	private Instances instances;
	private NormalizableDistance distance;
	private SimpleKMeans usedKmeans;


	/**
	 * 
	 * @param instances
	 */
	public KmeansImproved(final Instances pinstances,final int maxClusterSize){
		try {
			this.instances   = new Instances(pinstances);
			this.distance    = new EuclideanDistance(this.instances);
			this.maxClusters = maxClusterSize;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param instances
	 */
	public KmeansImproved(Instances instances){
		this(instances,DEFAULT_NUM_CLUSTERS_MAX);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public double[] getClusteredInstances() throws Exception {

		//Removing potential class index 
		instances.setClassIndex(-1);

		//Clustering using Kmeans
		int k ; 
		double max=0, r2 =0, pseudoF = 0;

		//Testing from 2 to 10 clusters, should be set as entry of this function
		SimpleKMeans bestKMeans = new SimpleKMeans();
		for (k=2; k <= maxClusters; k++){
			final SimpleKMeans kMeans = new SimpleKMeans();
			kMeans.setNumClusters(k);
			kMeans.buildClusterer(instances);
			//Choosing the "optimal" number of clusters
			r2 = R2(kMeans);
			pseudoF = pseudoF(r2, k);
			//System.out.println(pseudo_f);
			if(pseudoF > max){
				max = pseudoF;
				bestKMeans = kMeans;
			}
		}

		//Real clustering using the chosen number
		final ClusterEvaluation eval = new ClusterEvaluation();
		eval.setClusterer(bestKMeans);
		eval.evaluateClusterer(instances);
		double[] clusterAssignments = eval.getClusterAssignments();

		this.usedKmeans = bestKMeans;
		
		return clusterAssignments;

	}

	/**
	 * @return the usedKmeans
	 */
	public SimpleKMeans getUsedKmeans() {
		return usedKmeans;
	}

	/**
	 * 
	 * @param instances
	 * @param k
	 * @param clusters_sizes
	 * @param clusters_centroids
	 * @return
	 */
	private double R2(SimpleKMeans kMeans){
		//int k, int[] clusters_sizes, Instances clusters_centroids){
		final int k = kMeans.getNumClusters();
		final int [] clusters_sizes = kMeans.getClusterSizes();
		final Instances clusters_centroids = kMeans.getClusterCentroids();
		double inter, total;
		double[] weights = new double[k];
		double[] centroid = new double[instances.numAttributes()];
		final int N = instances.numInstances(); 
		final double instance_weight = 1.0;
		inter = total =0;

		//Computing the centroid of the entire set
		for(int i=0; i<N; i++){
			final Instance instance = instances.get(i);
			double[] temp = instance.toDoubleArray();
			for(int j=0; j<temp.length; j++)
				centroid[j] += temp[j];
		}
		for(int j=0; j<centroid.length; j++){
			centroid[j] = centroid[j]/N;
		}

		for(int i=0; i<k; i++){
			weights[i] = (0.0+clusters_sizes[i])/N;
		}


		final Instance centroid_G = new DenseInstance(instance_weight, centroid);
		for(int i=0; i<N; i++){
			total += Math.pow(distance.distance(instances.instance(i), centroid_G), 2);
		}
		total = total/N;

		for(int i=0; i<k; i++){
			inter += weights[i]*Math.pow(distance.distance(clusters_centroids.get(i),centroid_G),2);
		}

		return(inter/total);
	}


	/**
	 * 
	 * @param criteria
	 * @param k
	 * @param n
	 * @return
	 */
	private double pseudoF(double criteria, int k){
		final int n = instances.numInstances();
		return((criteria/(k-1))*((n-k)/(1-criteria)));
	}

}	




