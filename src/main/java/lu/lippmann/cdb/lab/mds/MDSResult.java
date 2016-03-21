/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import weka.core.*;

/**
 * 
 * @author didry
 *
 */
public class MDSResult {

	/** Distance matrix + correspondance maps etc **/
	private CollapsedInstances cInstances;

	/** MDS matrix result **/
	private SimpleMatrix coordinates;

	private boolean normalized;

	/**
	 * 
	 * @param distanceMDS
	 * @param matrix
	 */
	public MDSResult(final CollapsedInstances cInstances,final SimpleMatrix matrix,final boolean normalized) {
		this.cInstances = cInstances;
		this.coordinates = matrix;
		this.normalized  = normalized;
	}

	/**
	 * @return the distanceMDS
	 */
	public CollapsedInstances getCInstances() {
		return cInstances;
	}

	/**
	 * @return the matrix
	 */
	public SimpleMatrix getCoordinates() {
		return coordinates;
	}

	/**
	 * 
	 * @return
	 */
	public Instances buildInstancesFromMatrix(){
		final int nbInstances = coordinates.numRows();
		final ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("X",0));
		attrs.add(new Attribute("Y",1));
		final Instances ds =new Instances("Projection dataset",attrs,nbInstances);
		for(int i = 0 ; i < nbInstances ; i++ ){
			final Instance inst = new DenseInstance(1.0d,new double[]{coordinates.get(i,0),coordinates.get(i,1)});
			ds.add(inst);	
		}
		return ds;
	}

	/**
	 * 
	 * @return
	 */
	public Instances getCollapsedInstances(){
		if(cInstances.isCollapsed()){
			final List<Instance> centroids =  cInstances.getCentroidMap().getCentroids();
			final int nbCentroids = centroids.size();
			Instances collapsedInstances = new Instances(cInstances.getInstances(),0);
			for(int i = 0 ; i < nbCentroids ; i++){
				collapsedInstances.add(centroids.get(i));
			}
			return collapsedInstances;
		}else{
			return cInstances.getInstances();
		}
	}

	/**
	 * @return the normalized
	 */
	public boolean isNormalized() {
		return normalized;
	}
	

}
