/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt.weka;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;


/**
 * REPTreeDecisionTreeFactory.
 * 
 * @author Olivier PARISOT
 */
public final class REPTreeDecisionTreeFactory extends WekaDecisionTreeFactory 
{
	//
	// Instance methods
	//
	
	/** */
	private final boolean withPruning;
	
	
	//
	// Contructors
	//
	
	/**
	 * Contructor.
	 */
	public REPTreeDecisionTreeFactory(final boolean withPruning)
	{
		super();
		this.withPruning=withPruning;
	}
	
	
	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDecisionTreeKey(final Instances instances) 
	{
		return "reptree-"+instances.hashCode()+"-"+instances.classIndex()+"-"+instances.numInstances()+"-"+withPruning;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractClassifier getAbstractClassifier() throws Exception 
	{
		final REPTree repTree=new REPTree();
		repTree.setNoPruning(!this.withPruning);
		return repTree;
	}

}
