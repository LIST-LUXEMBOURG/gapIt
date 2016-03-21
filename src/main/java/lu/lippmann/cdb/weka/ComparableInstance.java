/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import weka.core.*;

/**
 * 
 * @author didry
 *
 */
public class ComparableInstance implements Comparable<ComparableInstance>{

	private Instance instance;
	private InstanceComparator ic;
	/**
	 * 
	 * @param instance
	 */
	public ComparableInstance(Instance instance){
		this.ic       =  new InstanceComparator(false);
		this.instance = instance;
	}
	
	@Override
	public int compareTo(ComparableInstance o) {
		return ic.compare(instance,o.instance);
	}

	/**
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}
	
	@Override
	public String toString() {
		return "["+instance.toString()+"]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for(int i =  instance.numAttributes()-1 ; i >= 0; i--){
			result+= prime*result+String.valueOf(instance.value(i)).hashCode();
		}
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ComparableInstance)) return false;
		ComparableInstance o = (ComparableInstance)obj;
		return (this.compareTo(o)==0);
	}
	
	
	

	
}
