/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;


/**
 * Tag definition.
 *
 * @author Olivier PARISOT
 */
public final class CTag implements Serializable
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=12975734578386L;
	
	
	//
	// Instance fields
	//
	
	/** */
	private CUser user;
	/** */
	private long timestamp;
	/** */
	private CValue value;
	
	
	//
	// Instance methods
	//

	/**
	 * @return the value
	 */
	public CValue getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(final CValue value) {
		this.value = value;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(final CUser user) {
		this.user = user;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the user
	 */
	public CUser getUser() {
		return user;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return new StringBuilder("CTag: ").append(user).append(' ').append(timestamp).append(' ').append(value).toString();
	}
	
	
	//
	// Private static enum
	//

	/**
	 * Tag note definition.
	 */
	public static enum CNote 
	{
		//
		// Enum values
		//
		
		GOOD("tags/tag_green.png"),BAD("tags/tag_red.png"),WARNING("tags/tag_orange.png");
		
		
		//
		// Instance fields
		//
		
		/** */
		private final String iconPath;
		
		
		//
		// Constructors
		//

		/**
		 * Constructor.
		 */
		CNote(final String iconPath)
		{
			this.iconPath=iconPath;
		}
		
		
		//
		// Instance methods
		//
		
		/**
		 * @return the iconPath
		 */
		public String getIconPath() {
			return iconPath;
		}
		
		
		//
		// Static methods
		//
		
		public static Object[] valuesAsObjectArray() 
		{
			final CNote[] values=values();
			final Object[] l=new Object[values.length];
			for (int i=0;i<values.length;i++) l[i]=values[i].name();
			return l;
		}



	}

	
	//
	// Public static class
	//
	
	/**
	 * @author Olivier PARISOT
	 */
	public static class CValue implements Serializable
	{
		//
		// Static fields
		//
		
		/** Serial version UID. */
		private static final long serialVersionUID=129755878486L;
		
		
		//
		// Instance fields
		//
		
		/** */
		private final CNote note;
		/** */
		private final String desc;
		
		
		//
		// Constructors
		//
		
		/**
		 * Constructor.
		 */
		public CValue(final CNote note,final String desc)
		{
			this.note=note;			
			this.desc=desc;			
		}
		
		
		//
		// Instance methods
		//

		/**
		 * @return the note
		 */
		public CNote getNote() {
			return note;
		}
		
		/**	
		 * @return
		 */
		public String getDesc() {
			return desc;
		};
		
		/**	 
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return new StringBuilder("CNote: ").append(note).append(' ').append(desc).toString();
		}

	}
}
