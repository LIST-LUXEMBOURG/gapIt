/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;

import java.io.File;

import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.dsl.ASCIIGraphDsl;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;


/**
 * RegressionTreeFactory.
 * 
 * @author
 */
public class RegressionTreeFactory 
{
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(final String[] args)
	{
		try
		{
			final String f="./samples/csv/uci/winequality-red.csv";
			//final String f="./samples/arff/UCI/crimepredict.arff";
			final Instances dataSet=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(f));
			System.out.println(dataSet.classAttribute().isNumeric());
			
			final REPTree rt=new REPTree();
			rt.setMaxDepth(3);
			rt.buildClassifier(dataSet);
			
			System.out.println(rt);
			
			//System.out.println(rt.graph());
			
			final GraphWithOperations gwo=GraphUtil.buildGraphWithOperationsFromWekaRegressionString(rt.graph());			
			System.out.println(gwo);
			System.out.println(new ASCIIGraphDsl().getDslString(gwo));
			
			final Evaluation eval=new Evaluation(dataSet);			
			
			/*Field privateStringField = Evaluation.class.getDeclaredField("m_CoverageStatisticsAvailable");
			privateStringField.setAccessible(true);
			//privateStringField.get
			boolean fieldValue = privateStringField.getBoolean(eval);
			System.out.println("fieldValue = " + fieldValue);*/
			
			double[] d=eval.evaluateModel(rt,dataSet);			
			System.out.println("PREDICTED -> "+FormatterUtil.buildStringFromArrayOfDoubles(d));
			
			System.out.println(eval.errorRate());
			System.out.println(eval.sizeOfPredictedRegions());
			
			System.out.println(eval.toSummaryString("",true));
			
			/*final String f2="./samples/csv/salary.csv";
			final Instances dataSet2=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(f2));
			
			final J48 j48=new J48();
			j48.buildClassifier(dataSet2);
			System.out.println(j48.graph());
			final GraphWithOperations gwo2=GraphUtil.buildGraphWithOperationsFromWekaString(j48.graph(),false);
			System.out.println(gwo2);*/
			
			System.out.println(new DecisionTree(gwo,eval.errorRate()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
