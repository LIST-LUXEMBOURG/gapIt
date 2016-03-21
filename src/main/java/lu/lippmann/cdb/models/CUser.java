/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;


/**
 * CUser.
 *
 * @author Olivier PARISOT
 */
public final class CUser implements Serializable
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=1297578786L;
	
	/** */
	public static final CUser ANONYMOUS=new CUser("anonymous","N/A");
	
	
	//
	// Instance fields
	//

	/** */
	private String name;
	/** */
	private String hostName;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public CUser(final String name,final String hostName) 
	{
		this.name=name;
		this.hostName=hostName;
	}
	
	/**
	 * Default constructor (usefull for serialization.
	 */
	public CUser() 
	{
		this(null,null);		
	}

	
	//
	// Instance methods
	//

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(final String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * 
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return name+"@"+hostName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CUser other = (CUser) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	public String getHostName() 
	{
		return hostName;
	}
}
