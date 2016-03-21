/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;
import java.util.*;


/**
 * CGraph element basic implementation.
 *
 * @author Olivier PARISOT
 */
public class CGraphElementImpl implements Serializable, Comparable<CGraphElementImpl>, CGraphElement {

	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=142883327311598L;
	
	
	//
	// Instance fields
	//
	
	/** */
	protected Long id;
	/** */
	protected String name;
	/** */
	private List<CTag> tags;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public CGraphElementImpl(final Long id,final String name)
	{
		this.id=id;
		this.name=name;
		this.tags=new ArrayList<CTag>();
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * 
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(final Long id) {
		this.id = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof CGraphElementImpl){
			return id.compareTo(((CGraphElement)obj).getId())==0;
		}else{
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final CGraphElementImpl cge) {
		return id.compareTo(cge.getId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTag(final CTag tag) 
	{
		tags.add(tag);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeTag(final CTag tag) 
	{
		tags.remove(tag);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<CTag> getTags() 
	{		
		return tags;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAndAddTags(final List<CTag> tags) 
	{
		this.tags=new ArrayList<CTag>(tags);
	}


}
