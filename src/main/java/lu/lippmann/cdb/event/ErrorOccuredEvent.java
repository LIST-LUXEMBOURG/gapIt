/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;


/**
 * Error event.
 *
 * @author Olivier PARISOT
 */
public final class ErrorOccuredEvent implements AbstractEvent
{
	//
	// Instance fields
	//
	
	/** */
	private final String msg;
	/** */
	private final Throwable exception;


	
	//
	// Constructors.
	//
	
	/**
	 * Constructor.
	 */
	public ErrorOccuredEvent(final String msg,final Throwable exception)
	{
		this.msg=msg;
		this.exception=exception;
	}

	
	//
	// Instance methods
	//
	
	/**
	 * 
	 * @return the msg
	 */
	public String getMsg() 
	{
		return msg;
	}


	/**
	 * 
	 * @return the exception
	 */
	public Throwable getException() 
	{
		return exception;
	}

}
