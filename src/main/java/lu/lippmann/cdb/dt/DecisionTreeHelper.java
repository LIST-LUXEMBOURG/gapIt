/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;


/**
 * DecisionTree helper.
 *
 * @author Olivier PARISOT
 */
public final class DecisionTreeHelper 
{	
	//
	// Static fields
	//

	/** Default confidence factor for computing. */
	public static final double HIGH_CONFIDENCE_FACTOR=0.5d;
	/** Default confidence factor for showing. */
	public static final double MEDIUM_CONFIDENCE_FACTOR=0.25d;
	/** Default confidence factor for showing. */
	public static final double LOW_CONFIDENCE_FACTOR=0.01d;


	//
	// Constructors
	//

	/**
	 * Private constructor. 
	 */
	private DecisionTreeHelper() {}

}
