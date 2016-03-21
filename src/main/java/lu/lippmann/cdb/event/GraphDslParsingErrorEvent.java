/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import java.util.*;


/**
 * 
 *
 *
 * @author Olivier PARISOT
 *
 */
public class GraphDslParsingErrorEvent implements AbstractEvent
{
	//
	// Instance fields
	//
	
	/** */
	private final Map<Integer,String> linesWithError;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public GraphDslParsingErrorEvent(final Map<Integer,String> linesWithError) 
	{
		this.linesWithError=linesWithError;
	}

	
	//
	// Instance methods
	//

	/**
	 * @return the linesWithError
	 */
	public Map<Integer,String> getLinesWithError() 
	{
		return linesWithError;
	}

}
