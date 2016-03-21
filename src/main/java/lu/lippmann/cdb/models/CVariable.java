/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;
import java.util.*;


/**
 * Cadral variable definition: item used to build rules or expressions.
 *
 * @author Olivier PARISOT
 */
public final class CVariable implements Serializable, Comparable<CVariable>
{
	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=19016446759L;

	
	//
	// Instance fields
	//
	
	/** */
	private String key;
	/** */
	private String description;
	/** */
	private CadralType type;
	
	/** for CadralType.ENUMERATION only */
	private List<String> values;
	
	public CVariable(){
		
	}
	
	/**
	 * 
	 * @param c
	 */
	public CVariable(CVariable c){
		this.key = c.getKey();
		this.description = c.getDescription();
		this.type = c.getType();
		if(c.getValues()!=null){
			this.values = new ArrayList<String>(c.getValues());
		}else{
			this.values = null;
		}
	}
		
	//
	// Instance fields
	//
	
	/**
	 * @return the key
	 */
	public String getKey() 
	{
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(final String key) 
	{
		this.key = key;
	}

	/**
	 * @return the description
	 */
	public String getDescription() 
	{
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(final String description) 
	{
		this.description = description;
	}

	/**
	 * @return the type
	 */
	public CadralType getType() 
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final CadralType type) 
	{
		this.type = type;
	}

	
	/**
	 * @return the values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasDescription() 
	{		
		return description!=null&&description.length()>0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "CVariable [key=" + key + ", description=" + description
				+ ", type=" + type + ", values=" + values + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override	
	public int hashCode() 
	{
		return key.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) 
	{
		if(obj instanceof CVariable){
			return key.equals(((CVariable)obj).key);
		}else{
			return false;
		}
	}

	public boolean allEquals(final CVariable c){
		if( (values==null && c.getValues()!=null) || (values!=null && c.getValues()==null) ){
			return false;
		}else{
			boolean res = key.equals(c.getKey()) && description.equals(c.getDescription()) && type.equals(c.getType());
			if(values!=null && c.getValues()!=null){
				return res && values.equals(c.getValues());
			}else{
				return res;
			}
			
		}
	}

	//
	// Inner enums
	//

	/**
	 * All possible type : unknow, ...
	 */
	public enum CadralType
	{
		UNKNOWN,NUMERIC,BOOLEAN,ENUMERATION;
	}



	@Override
	public int compareTo(CVariable o) {
		return key.compareTo(o.getKey());
	}




}
