/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt.weka;

import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import weka.classifiers.*;
import weka.classifiers.trees.*;
import weka.core.*;


/**
 * WekaDecisionTreeFactory.
 * 
 * @author Olivier PARISOT
 */
public abstract class WekaDecisionTreeFactory extends CachedDecisionTreeFactory 
{
	//
	// Static fields
	//

	/** */
	private static final boolean MERGE_FINAL_STATE=false;
	/** */
	private static final boolean DEBUG=false;
	
	
	//
	// Abstract methods
	//
	
	/**
	 * Get the subsequent weka classifier.
	 */
	public abstract AbstractClassifier getAbstractClassifier() throws Exception;
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final DecisionTree buildDecisionTree(final Instances instances) throws Exception
	{
		if (instances.classIndex()>=0&&instances.numClasses()==1&&!instances.classAttribute().isNumeric())
		{
			return new DecisionTree(instances.classAttribute().name());
		}
		else
		{
			//System.out.println("WekaDecisionTreeFactory.buildDecisionTree() ...");
			if (DEBUG) System.out.println("WekaDecisionTreeFactory.buildDecisionTree() ...");
			final AbstractClassifier classifier=getAbstractClassifier();
			classifier.buildClassifier(instances);
			final Evaluation eval=new Evaluation(instances);
			eval.evaluateModel(classifier,instances);
			final GraphWithOperations gwo;
			// FIXME: the following lines are horrible!!!!
			/*if(classifier instanceof J48)
			{
				gwo=GraphUtil.buildGraphWithOperationsFromWekaString(((J48)classifier).graph(),MERGE_FINAL_STATE);
			}
			else*/ if(classifier instanceof REPTree)
			{
				gwo=GraphUtil.buildGraphWithOperationsFromWekaRegressionString(((REPTree)classifier).graph());
			}
			else
			{
				throw new IllegalStateException("Classifier not handled !");
			}
			//System.out.println("... end of WekaDecisionTreeFactory.buildDecisionTree()");
			if (DEBUG) System.out.println("... end of WekaDecisionTreeFactory.buildDecisionTree()");
			return new DecisionTree(gwo,eval.errorRate());
		}
	}
	
	

}
