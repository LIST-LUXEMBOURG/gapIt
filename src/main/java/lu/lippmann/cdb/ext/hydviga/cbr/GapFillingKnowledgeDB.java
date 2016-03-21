/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.cbr;

import java.io.File;
import java.util.List;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * GapFillingKnowledgeDB.
 * 
 * @author the HYDVIGA team
 */
public final class GapFillingKnowledgeDB 
{
	//
	// Static fields
	//
	
	/** */
	private static final String HEADER=
										"serieName,"+
										"serieX,"+
										"serieY,"+
										
										"gapSize,"+
										"gapPosition,"+										

										"season,"+
										"year,"+
										
										"isDuringRising,"+
										"flow,"+
										
										"hasDownstream,"+
										"hasUpstream,"+		
										
										"algo,"+
										"useDiscretizedTime,"+
										"useMostSimilar,"+
										"useNearest,"+
										"useDownstream,"+
										"useUpstream,"+
										
										"MAE,"+
										"RMSE,"+
										"RSR,"+
										"PBIAS,"+
										"NashSutcliffe,"+
										"indexOfAgreement,"+
										
										"wasTheBestSolution";
	
	public static final String[] INPUT_FIELDS=new String[]{"serieName","gapSize","gapPosition","serieX","serieY","hasDownstream","hasUpstream","season","year","isDuringRising","flow"};
	
	/** FIXME: POTENTIALLY BAD FOR MEMORY LEAK! */
	private static final StringBuilder DATABASE_AS_STRINGBUILDER=new StringBuilder(HEADER).append("\n");
	/** FIXME: POTENTIALLY BAD FOR MEMORY LEAK! */
	private static Instances DATABASE;
	
	
	//
	// Static  methods
	//
	
	public static void clear()
	{
		DATABASE_AS_STRINGBUILDER.setLength(0);
		DATABASE_AS_STRINGBUILDER.append(HEADER).append("\n");
		DATABASE=null;
	}
	
	public static void storeCasesIntoKnowledgeDB(final List<GapFillingCase> l)
	{
		for (final GapFillingCase tsgfs:l)
		{
			storeCaseIntoKnowledgeDB(tsgfs);
		}
		
		try 
		{
			DATABASE=repackKnowledgeDB();
		} 
		catch (Exception e) 
		{			
			System.err.println(DATABASE_AS_STRINGBUILDER);
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void storeCaseIntoKnowledgeDB(final GapFillingCase tsgfs)
	{	
		final String algoName=tsgfs.algo.toString();
		final int useDiscretizedTimeIdx=algoName.indexOf('_');
		final boolean useDiscretizedTime=useDiscretizedTimeIdx>0;
		DATABASE_AS_STRINGBUILDER
		
		  .append(tsgfs.attr.name())
		  .append(",")
		  .append(tsgfs.x)
		  .append(",")
		  .append(tsgfs.y)
		  .append(",")

		  .append(tsgfs.gapSize)
		  .append(",")
		  .append(tsgfs.gapPosition)
		  .append(",")		  

		  .append(tsgfs.season)
		  .append(",")
		  .append(tsgfs.year)
		  .append(",")		  
		  
		  .append(tsgfs.isDuringRising)
		  .append(",")		  		  
		  .append(tsgfs.flow)
		  .append(",")		 		  
		  
		  .append(tsgfs.hasDownstream)
		  .append(",")
		  .append(tsgfs.hasUpstream)
		  .append(",")			  
		  		  
		  .append(useDiscretizedTime?algoName.substring(0,useDiscretizedTimeIdx):algoName)
		  .append(",")
		  .append(useDiscretizedTime)
		  .append(",")		  
		  .append(tsgfs.useMostSimilar)
		  .append(",")
		  .append(tsgfs.useNearest)
		  .append(",")
		  .append(tsgfs.useDownstream)
		  .append(",")
		  .append(tsgfs.useUpstream)
		  .append(",")
		  
		  .append(tsgfs.mae)
		  .append(",")
		  .append(tsgfs.rmse)
		  .append(",")
		  .append(tsgfs.rsr)
		  .append(",")
		  .append(tsgfs.pbias)
		  .append(",")		  
		  .append(tsgfs.nashSutcliffe)
		  .append(",")		  
		  .append(tsgfs.indexOfAgreement)
		  .append(",")	
		  
		  .append(tsgfs.wasTheBestSolution)		  
		  .append("\n");
	}
	
	public static Instances getKnowledgeDB() throws Exception
	{
		return DATABASE;
	}
	
	public static Instances getKnowledgeDBWithBestCasesOnly() throws Exception
	{
		final Instances newds=new Instances(DATABASE,0);
		final int numInstances=DATABASE.numInstances();
		final int numAttributes=DATABASE.numAttributes();
		for (int i=0;i<numInstances;i++)
		{			
			if (DATABASE.instance(i).stringValue(numAttributes-1).equals("true")) newds.add(DATABASE.instance(i));
		}
		
		return newds;
	}
	
	private static Instances repackKnowledgeDB() throws Exception
	{
		return WekaDataAccessUtil.loadInstancesFromCSVString(DATABASE_AS_STRINGBUILDER.toString(),false);		
	}
	
	public static void loadKnowledgeDBFromFile(final String path) throws Exception
	{
		DATABASE=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(path));
		DATABASE_AS_STRINGBUILDER.setLength(0);
		DATABASE_AS_STRINGBUILDER.append(WekaDataAccessUtil.saveInstancesIntoCSVString(DATABASE));
	}

	public static Instances findSimilarCases(final String attrname,final double x,final double y,final int year,final String season,final int gapSize,final int gapPosition,final boolean isDuringRising,final boolean hasDownstream,final boolean hasUpstream,final String flow) throws Exception 
	{
		/* build the current case */
		final StringBuilder newsb=new StringBuilder(DATABASE_AS_STRINGBUILDER);
		newsb
		  .append(attrname)
		  .append(",")
		  .append(x)
		  .append(",")
		  .append(y)
		  .append(",")

		  .append(gapSize)
		  .append(",")
		  .append(gapPosition)
		  .append(",")
		  
		  .append(season)
		  .append(",")
		  .append(year)
		  .append(",")			  
		  
		  .append(isDuringRising)
		  .append(",")		  		  
		  .append(flow)
		  .append(",")
		  
		  .append(hasDownstream)
		  .append(",")
		  .append(hasUpstream)
		  .append(",")		  
		  
		  .append("?")
		  .append(",")
		  .append("?")
		  .append(",")		  
		  .append("?") 
		  .append(",")
		  .append("?") 
		  .append(",")
		  .append("?")
		  .append(",")
		  .append("?")
		  .append(",")
		  .append(0) // MAE
		  .append(",")
		  .append(0) // RMSE
		  .append(",")
		  .append(0) // RSR
		  .append(",")
		  .append(0) // PBIAS
		  .append(",")		  
		  .append(1) // NS
		  .append(",")		 
		  .append(1) // IOA
		  .append(",")			  
		  .append(true) // BEST SOLUTION  		  
		  .append("\n");		
		final Instances tmpDB=WekaDataAccessUtil.loadInstancesFromCSVString(newsb.toString(),false);					
		
		final Instance newcase=tmpDB.instance(tmpDB.numInstances()-1);
		
		/* compute NN for the current case */
		final Instances knn=WekaMachineLearningUtil.computeNearestNeighbours(tmpDB,newcase,10,"2,3,4,6,7,8,9,10,23");		
		knn.add(0,newcase);
		
		System.out.println(knn.toSummaryString());
		
		return knn;
		
	}
}
