/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt.weka;

import lu.lippmann.cdb.weka.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;


/**
 * J48DecisionTreeFactory.
 * 
 * @author Olivier PARISOT
 */
public class J48DecisionTreeFactory extends WekaDecisionTreeFactory 
{
	//
	// Instance fields
	//
	
	/** */
	private double confidenceFactor;
	/** */
	private final boolean isUnpruned;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public J48DecisionTreeFactory(final double confidenceFactor,final boolean isUnpruned)
	{
		this.confidenceFactor=confidenceFactor;
		this.isUnpruned=isUnpruned;
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
		return "j48-"+instances.hashCode()+"-"+instances.classIndex()+"-"+instances.numInstances()+"-"+confidenceFactor+"-"+isUnpruned;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractClassifier getAbstractClassifier() throws Exception 
	{
		return WekaMachineLearningUtil.buildJ48Classifier(isUnpruned,2,-1,confidenceFactor);
	}

	public double getConfidenceFactor() 
	{
		return confidenceFactor;		
	}

	public void setConfidenceFactor(final double d) 
	{
		this.confidenceFactor=d;		
	}
}
