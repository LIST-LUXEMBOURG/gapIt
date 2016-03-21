/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;

import lu.lippmann.cdb.dt.weka.*;


/**
 * DecisionTreeFactoryEnum.
 * 
 * @author Olivier PARISOT
 */
public enum DecisionTreeFactoryEnum 
{
	//
	// Enum values
	//
	
	J48_WITH_LOW_CONFIDENCE_FACTOR(new J48DecisionTreeFactory(DecisionTreeHelper.LOW_CONFIDENCE_FACTOR,false)),
	J48_WITH_HIGH_CONFIDENCE_FACTOR(new J48DecisionTreeFactory(DecisionTreeHelper.HIGH_CONFIDENCE_FACTOR,false))
;
	
	
	//
	// Instance fields
	//
	
	/** */
	private final DecisionTreeFactory dtf;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	DecisionTreeFactoryEnum(final DecisionTreeFactory dtf)
	{
		this.dtf=dtf;
	}

	
	//
	// Instance methods
	//
	
	public DecisionTreeFactory getDecisionTreeFactory() 
	{
		return dtf;
	}	
	
}
