/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.beta.util;

import java.util.*;

import lu.lippmann.cdb.lab.beta.shih.TupleSI;

/**
 * 
 * @author didry
 *
 */
public class MixedCentroid {

	private int clusterIndex;
	private double[] centroid;				   //centroid of a mixted cluster
	private Map<TupleSI,Integer> addedAttribute; //count of each categorical attribute in this mixed cluster

	/**
	 * 
	 * @param centroid
	 * @param addedAttribute
	 */
	public MixedCentroid(int clusterIndex,double[] centroid, Map<TupleSI, Integer> addedAttribute) {
		this.clusterIndex = clusterIndex;
		this.centroid = centroid;
		this.addedAttribute = addedAttribute;
	}

	/**
	 * @return the centroid
	 */
	public double[] getCentroid() {
		return centroid;
	}

	/**
	 * @param centroid the centroid to set
	 */
	public void setCentroid(double[] centroid) {
		this.centroid = centroid;
	}

	/**
	 * @return the addedAttribute
	 */
	public Map<TupleSI, Integer> getAddedAttribute() {
		return addedAttribute;
	}

	/**
	 * @param addedAttribute the addedAttribute to set
	 */
	public void setAddedAttribute(Map<TupleSI, Integer> addedAttribute) {
		this.addedAttribute = addedAttribute;
	}
	
	

	/**
	 * @return the clusterIndex
	 */
	public int getClusterIndex() {
		return clusterIndex;
	}

	/**
	 * @param clusterIndex the clusterIndex to set
	 */
	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
	}

	/**
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static double distance(final double[] t1,final double[] t2)
	{
		if (t1.length!=t2.length) throw new IllegalStateException("distance()");
		double sum=0d;
		for (int i=0;i<t1.length;i+=1)
		{
			sum+=(t1[i]-t2[i])*(t1[i]-t2[i]);
		}
		return Math.sqrt(sum);
	}

	//the TupleSI must have the same order !!!
	public double[] getMixedCentroid(SortedSet<TupleSI> domain){
		double[] x1 = getCentroid();
		double[] res = new double[x1.length+domain.size()];
		int i = 0;
		for(i = 0 ; i < x1.length ; i++){
			res[i] = x1[i];
		}
		i=x1.length;
		for(TupleSI t : domain){
			if(addedAttribute.containsKey(t)){
				res[i]=addedAttribute.get(t);
			}
			i++;
		}
		return res; 
	}


	@Override
	public String toString() {
		String res = "Cluster = " + clusterIndex+"\nCenter = ";
		for(int j = 0 ; j < centroid.length ; j++){res+=centroid[j]+"\t";}
		res+="\nAdded Attribute =";
		res+=addedAttribute;
		return res;
	}

}
