/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.beta.util;

import java.util.Map;

import weka.core.Instances;

/**
 * 
 * @author didry
 *
 */
public class IndexedInstance {

	private Instances instances;
	private Map<Integer,Integer> mapOrigIndex;
	

	/**
	 * 
	 * @param instances
	 * @param mapOrigIndex
	 */
	public IndexedInstance(Instances instances,Map<Integer, Integer> mapOrigIndex) {
		this.instances = instances;
		this.mapOrigIndex = mapOrigIndex;
	}
	
	/**
	 * @return the instances
	 */
	public Instances getInstances() {
		return instances;
	}
	
	/**
	 * @param instances the instances to set
	 */
	public void setInstances(Instances instances) {
		this.instances = instances;
	}
	
	/**
	 * @return the mapOrigIndex
	 */
	public Map<Integer, Integer> getMapOrigIndex() {
		return mapOrigIndex;
	}
	
	/**
	 * @param mapOrigIndex the mapOrigIndex to set
	 */
	public void setMapOrigIndex(Map<Integer, Integer> mapOrigIndex) {
		this.mapOrigIndex = mapOrigIndex;
	}
	
	@Override
	public String toString() {
		return "IndexedInstance [instances=" + instances + ", mapOrigIndex="
				+ mapOrigIndex + "]";
	}
	

}
