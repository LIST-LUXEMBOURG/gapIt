/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.services;


/**
 * Login exception.
 *
 * @author Olivier PARISOT
 */
public final class AuthentificationException extends Exception 
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID = -2614927067432829112L;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public AuthentificationException() 
	{
		super();
	}

	/**
	 * Constructor.
	 */
	public AuthentificationException(final String message,final Throwable cause) 
	{
		super(message, cause);
	}

	/**
	 * Constructor.
	 */
	public AuthentificationException(final String message) 
	{
		super(message);
	}
}
