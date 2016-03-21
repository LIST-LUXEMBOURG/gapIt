/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.cbr;

import java.io.File;
import java.util.*;

import weka.core.*;
import lu.lippmann.cdb.ext.hydviga.HydroRunner;
import lu.lippmann.cdb.weka.*;



/**
 * GapFillingKnowledgeDBAnalyzer.
 * 
 * @author the gapIt team
 */
public final class GapFillingKnowledgeDBAnalyzer 
{
	//
	// Static methods
	//
	
	private static int getCountOfFictiveGaps(final Instances newkdb)
	{
		final Set<String> set=new HashSet<String>();
		for (int i=0;i<newkdb.numInstances();i++)
		{
			final String key=newkdb.instance(i).stringValue(newkdb.attribute("serieName").index())+"-"+
							 newkdb.instance(i).value(newkdb.attribute("gapSize").index())+"-"+
							 newkdb.instance(i).value(newkdb.attribute("gapPosition").index());
			set.add(key);	
		}
		return set.size();				
	}
	
	
	//
	// Main method
	//
	
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(final String[] args)
	{
		try 
		{
			HydroRunner.init(false);
			
			Instances newkdb=new Instances(GapFillingKnowledgeDB.getKnowledgeDB());
			
			System.out.println("Considered fictive gaps -> "+getCountOfFictiveGaps(newkdb));
			
			System.out.println(newkdb.toSummaryString());
			
			newkdb=WekaDataProcessingUtil.filterDataSetOnNominalValue(newkdb,newkdb.attribute("useDownstream").index(),"false");
			newkdb=WekaDataProcessingUtil.filterDataSetOnNominalValue(newkdb,newkdb.attribute("useUpstream").index(),"false");
			//newkdb=WekaDataProcessingUtil.filterDataSetOnNominalValue(newkdb,newkdb.attribute("useNearest").index(),"false");
			//newkdb=WekaDataProcessingUtil.filterDataSetOnNominalValue(newkdb,newkdb.attribute("useMostSimilar").index(),"false");
			
			//System.out.println(newkdb.toSummaryString());
			
			Instances withGoodNashSutcliffe=new Instances(newkdb,0);
			for (int i=0;i<newkdb.numInstances();i++)
			{
				if (newkdb.instance(i).value(newkdb.attribute("NashSutcliffe").index())>0.5d) 
				{
					withGoodNashSutcliffe.add(new DenseInstance(1d,newkdb.instance(i).toDoubleArray()));
				}
			}
			
			System.out.println(withGoodNashSutcliffe.numInstances()+" / "+newkdb.numInstances());
			
			final double perc=(double)getCountOfFictiveGaps(withGoodNashSutcliffe)/getCountOfFictiveGaps(newkdb);
			System.out.println("Fictive gaps that are infilled with a good Nash-Sutcliffe -> "+getCountOfFictiveGaps(withGoodNashSutcliffe)+" ("+perc+"%)");
			
			WekaDataAccessUtil.saveInstancesIntoARFFFile(withGoodNashSutcliffe, new File("./withGoodNashSutcliffe.arff"));
		} 
		catch (final Exception e) 
		{			
			e.printStackTrace();
		}
	}
}
