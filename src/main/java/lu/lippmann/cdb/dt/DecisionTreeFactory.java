/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;

import weka.core.Instances;


/**
 * DecisionTreeFactory.
 * 
 * @author the ACORA team.
 */
public interface DecisionTreeFactory 
{
	DecisionTree buildDecisionTree(final Instances instances) throws Exception;
	
	DecisionTree buildCachedDecisionTree(final Instances instances) throws Exception;
	
	String getDecisionTreeKey(final Instances instances);
}
