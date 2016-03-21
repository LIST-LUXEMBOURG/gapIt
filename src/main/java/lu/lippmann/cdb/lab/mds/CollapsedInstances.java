/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import org.ejml.simple.SimpleMatrix;

import weka.core.Instances;

/**
 * 
 * @author didry
 *
 */
public final class CollapsedInstances {

	private final Instances instances;
	private final KmeansResult centroidMap;    //centroid -> elements of clusters

	private final SimpleMatrix distanceMatrix;
	
	private final boolean collapsed;
	
	
	/**
	 * 
	 * @param instances
	 * @param collapsedInstances
	 * @param mapCollapsed
	 * @param correspondanceMap 
	 * @param distances
	 * @param collapsed
	 */
	public CollapsedInstances(Instances instances,KmeansResult centroidMap, SimpleMatrix distances,boolean collapsed) {
		this.instances = instances;
		this.centroidMap = centroidMap;
		this.distanceMatrix = distances;
		this.collapsed = collapsed;
	}


	/**
	 * @return the instances
	 */
	public Instances getInstances() {
		return instances;
	}

	/**
	 * @return the centroidMap
	 */
	public KmeansResult getCentroidMap() {
		return centroidMap;
	}

	/**
	 * @return the distanceMatrix
	 */
	public SimpleMatrix getDistanceMatrix() {
		return distanceMatrix;
	}

	/**
	 * @return the collapsed
	 */
	public boolean isCollapsed() {
		return collapsed;
	}
	
}
