/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import java.util.List;

import weka.core.*;

/**
 * 
 * @author didry
 *
 */
public class KmeansResult {

	private Instances centroids;
	private List<Instances> clusters;
	
	/**
	 * 
	 * @param centroids
	 * @param clusters
	 */
	public KmeansResult(Instances centroids, List<Instances> clusters) {
		this.centroids = centroids;
		this.clusters = clusters;
	}

	/**
	 * @return the centroids
	 */
	public Instances getCentroids() {
		return centroids;
	}

	/**
	 * @param centroids the centroids to set
	 */
	public void setCentroids(Instances centroids) {
		this.centroids = centroids;
	}

	/**
	 * @return the clusters
	 */
	public List<Instances> getClusters() {
		return clusters;
	}

	/**
	 * @param clusters the clusters to set
	 */
	public void setClusters(List<Instances> clusters) {
		this.clusters = clusters;
	}
	
	
	
	
}
