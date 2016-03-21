/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import java.io.Serializable;

import lu.lippmann.cdb.models.CUser;


/**
 * ChatMessageReceivedEvent.
 * 
 * @author Yoann DIDRY, Olivier PARISOT
 */
public final class ChatMessageReceivedEvent implements AbstractEvent,Serializable 
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=127967L;


	//
	// Instance fields
	//	
	
	/** */
	private final String message;
	/** */
	private final CUser user;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public ChatMessageReceivedEvent(final CUser user,final String message)
	{
		this.user=user;
		this.message=message;
	}

	
	//
	// Instance methods
	//

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @return the user
	 */
	public CUser getUser() {
		return user;
	}
	
}
