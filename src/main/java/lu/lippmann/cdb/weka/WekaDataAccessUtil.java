/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;
import weka.core.*;
import weka.core.converters.*;


/**
 * Weka utility class.
 *
 * @author the WP1/ACORA team
 */
public final class WekaDataAccessUtil 
{
	//
	// Static fields
	//

	/** */
	public static final String DEFAULT_SAMPLE_DIR="J:\\Parisot/Public/CadralDecisionBuild/samples/";
	/** */
	public static final String DEFAULT_SAMPLE_FILE=DEFAULT_SAMPLE_DIR+"csv/bank.csv";
	/** */
	public static final String DEFAULT_SAMPLE_FILE_CLASS="pep";
	
	/** Logger. */
	private static final Logger LOGGER=Logger.getLogger(WekaDataAccessUtil.class.toString());

	/** */
	private static final ConcurrentHashMap<String,Instances> INSTANCES_CACHE = new ConcurrentHashMap<String,Instances>();	

	
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private WekaDataAccessUtil() {}


	//
	// Static methods
	//

	protected static Instances loadInstancesFromCSVFile(final File file,final boolean setIndex) throws Exception
	{
		Instances toReturn=null;
		final CSVLoader loader=new CSVLoader();
		try 
		{		
			loader.setSource(file);
			toReturn=loader.getDataSet();
			if (setIndex) toReturn.setClassIndex(toReturn.numAttributes()-1);		
		} 
		catch (IOException e) 
		{
			throw new Exception("Error when loading CSV file!",e);
		}
		return toReturn;
	}

	public static Instances loadInstancesFromCSVStream(final InputStream is,final boolean setIndex) throws Exception
	{
		LOGGER.log(Level.INFO,"Loading dataset from stream ...");		
		final CSVLoader loader=new CSVLoader();
		Instances instances=null;
		try 
		{		
			loader.setSource(is);
			instances=loader.getDataSet();
			if (setIndex) instances.setClassIndex(instances.numAttributes()-1);		
		} 
		catch (IOException e) 
		{
			throw new Exception("Error when loading CSV stream!",e);
		}
		WekaDataStatsUtil.checkAttributesNames(instances);
		LOGGER.log(Level.INFO,"... Dataset loaded from stream");
		return instances;
	}
	
	public static Instances loadInstancesFromARFFOrCSVFile(final File file) throws Exception
	{
		if (file==null)  throw new IllegalStateException("buildInstancesFromARFFOrCSVFile(): null file? ");		
		
		LOGGER.log(Level.INFO,"Loading dataset from file: "+file.getName()+" ...");		
				
		Instances toReturn=null;

		if(INSTANCES_CACHE.containsKey(file.getName()+file.length()))
		{
			toReturn=INSTANCES_CACHE.get(file.getName()+file.length());
		}
		else
		{								
			if (file.getName().endsWith(".csv")) 
			{	
				toReturn=loadInstancesFromCSVFile(file,true);
			}
			else 
			{	
				toReturn=new Instances(new FileReader(file));								
				Attribute clsAttribute=toReturn.attribute("class");
				if (clsAttribute==null) clsAttribute=toReturn.attribute(toReturn.numAttributes()-1);
				toReturn.setClass(clsAttribute);

			}
			if (toReturn==null)  throw new IllegalStateException("buildInstancesFromARFFOrCSVFile(): file not found? "+file.getName());
			if (toReturn.classIndex()<0) throw new IllegalStateException("buildInstancesFromARFFOrCSVFile(): no class? "+file.getName());
			INSTANCES_CACHE.put(file.getName(),toReturn);
		}
		
		WekaDataStatsUtil.checkAttributesNames(toReturn);
		LOGGER.log(Level.INFO,"... Dataset loaded from file: "+file.getName());
		return toReturn;
	}

	public static Instances loadInstancesFromCSVString(final String s,final boolean setIndex) throws Exception
	{
		final CSVLoader loader=new CSVLoader();
		Instances instances=null;
		try 
		{			
			loader.setSource(new ByteArrayInputStream(s.getBytes()));
			instances=loader.getDataSet();
			if (setIndex) instances.setClassIndex(instances.numAttributes()-1);			
		} 
		catch (IOException e) 
		{
			throw new Exception("Error when loading CSV string!",e);
		}
		WekaDataStatsUtil.checkAttributesNames(instances);
		return instances;
	}
	
	public static Instances loadInstancesFromARFFString(final String s,final boolean setIndex,final boolean... check) throws Exception
	{
		final Instances instances=new Instances(new StringReader(s));
		if (setIndex) instances.setClassIndex(instances.numAttributes()-1);
		if (check.length==0||check[0]) WekaDataStatsUtil.checkAttributesNames(instances);
		return instances;
	}

	public static void saveInstancesIntoCSVFile(final Instances dataset,final File file) throws IOException
	{
		final CSVSaver saver=new CSVSaver();
		saver.setInstances(dataset);
		saver.setFile(file);
		saver.writeBatch();
	}

	public static String saveInstancesIntoCSVString(final Instances dataset) throws IOException
	{
		final CSVSaver saver=new CSVSaver();
		saver.setInstances(dataset);
		final ByteArrayOutputStream baos=new ByteArrayOutputStream();
		saver.setDestination(baos);
		saver.writeBatch();
		return baos.toString();
	}
	
	public static void saveInstancesIntoARFFFile(final Instances dataset,final File file) throws IOException
	{
		final ArffSaver saver=new ArffSaver();
		saver.setInstances(dataset);
		saver.setFile(file);
		saver.writeBatch();		
	}

}
