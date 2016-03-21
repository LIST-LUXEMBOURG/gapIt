/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;

import weka.core.Instances;

/**
 * 
 * @author didry
 *
 */
public class ComparableInstances {

	private Instances instances;
	
	private List<ComparableInstance> comparableInstances;
	
	/**
	 * 
	 * @param instances
	 */
	public ComparableInstances(Instances instances){
		this.instances = instances;
		this.comparableInstances = new ArrayList<ComparableInstance>();
		final int size = instances.numInstances();
		for(int i = 0 ; i < size ; i++){
			this.comparableInstances.add(new ComparableInstance(instances.instance(i)));
		}
	}

	/**
	 * @return the comparableInstances
	 */
	public List<ComparableInstance> getComparableInstances() {
		return comparableInstances;
	}

	/**
	 * @return the instances
	 */
	public Instances getInstances() {
		return instances;
	}
	
	
	

}
