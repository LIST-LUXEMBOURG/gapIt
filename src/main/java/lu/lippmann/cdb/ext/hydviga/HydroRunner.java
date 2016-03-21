/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga;

import java.awt.Frame;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

import org.jdesktop.swingx.JXFrame;
import com.google.inject.*;

import lu.lippmann.cdb.DefaultModule;
import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.event.EventPublisher;
import lu.lippmann.cdb.ext.hydviga.cbr.GapFillingKnowledgeDB;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.ui.HydroDatasetView;
import lu.lippmann.cdb.ext.hydviga.ui.HydroDatasetView.Granularity;
import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.lab.timeseries.DynamicTimeWarping;
import lu.lippmann.cdb.main.ErrorPresenter;
import lu.lippmann.cdb.models.CGraph;
import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * HydroRunner.
 * 
 * @author the gapIt team
 */
public final class HydroRunner 
{	
	//
	// Static fields
	//

	/** */
	private static final String PROPERTIES_PATH="."+File.separatorChar+"cdb-hydro.properties";
	/** */
	private static final Properties PROPERTIES=new Properties();

	
	/** Tricky: to avoid gc. */
	@SuppressWarnings("unused")
	private static BusLogger buslogger;
	/** Tricky: to avoid gc. */
	private static ErrorPresenter errorPresenter;
	
	
	//
	// Instance methods
	//
	
	/**
	 * Private constructor.
	 */
	private HydroRunner() {}
	
	
	//
	// Static method
	//
	
	public static void init(final boolean withUI) throws Exception
	{
		/* load properties*/
		System.out.println("Loading properties from '"+PROPERTIES_PATH+"' ...");
		try 
		{	 
			PROPERTIES.load(new FileInputStream(PROPERTIES_PATH));	
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
			System.err.println("The properties file is not accessible!");
			System.exit(0);
		} 
		System.out.println("... properties loaded! -> "+PROPERTIES);
		
		/* check L&F */
		LookAndFeelUtil.init();			
		System.out.println("Available look and feel:");
		for (final UIManager.LookAndFeelInfo lafInfo:UIManager.getInstalledLookAndFeels())
		{
			System.out.println("\t"+lafInfo);
		}			
		System.out.println("Current look and feel:\n\t"+UIManager.getLookAndFeel());
		
		/* load dataset */
		Instances dataSet;
		if (Boolean.valueOf(PROPERTIES.getProperty("withOriginalFile")))
		{
			/* get list of files */
			final Object[] ALL_H_FILES = getHFilesList();
		
			/* get user selection */
			final int[] indexes=getUserSelection(ALL_H_FILES);	    	
		
			/* merge */
			dataSet=buildDatasetH_(ALL_H_FILES[indexes[0]].toString());
			for (int i=1;i<indexes.length;i++)
			{
				dataSet=WekaTimeSeriesUtil.buildMergedDataSet2(dataSet,buildDatasetH_(ALL_H_FILES[indexes[i]].toString()));
			}
			
			final int fdidx=WekaDataStatsUtil.getFirstDateAttributeIdx(dataSet);
			dataSet=WekaDataProcessingUtil.renameAttribute(dataSet,fdidx,"timestamp");
			
		}
		else
		{
			dataSet=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File(PROPERTIES.getProperty("modifiedDataPath")));
		}
								
		/* filter dataset with year */			
		dataSet=WekaDataProcessingUtil.afterDate(dataSet,Integer.valueOf(PROPERTIES.getProperty("minYear")),1,1,0,0,0);
		
		/* load geo coordinates manager */
		final StationsDataProvider gcp=loadGeoCoordinatesProvider(PROPERTIES.getProperty("coordinatesPath"),
																	PROPERTIES.getProperty("relationshipsPath").split(","),
																	PROPERTIES.getProperty("shapeImagePath"));
		
		/* load KnowledgeDB */
		GapFillingKnowledgeDB.loadKnowledgeDBFromFile(PROPERTIES.getProperty("knowledgeDBPath"));
		
		if (withUI)
		{
			/* launch UI */
			final Injector injector=Guice.createInjector(new DefaultModule());			
			buslogger=injector.getInstance(BusLogger.class);
			errorPresenter=injector.getInstance(ErrorPresenter.class);
			errorPresenter.init();			
			final EventPublisher eventPublisher=injector.getInstance(EventPublisher.class);
			final String title="gapIT";
			final HydroDatasetView view=new HydroDatasetView(title,eventPublisher,injector.getInstance(CommandDispatcher.class),injector.getInstance(ApplicationContext.class),PROPERTIES.getProperty("picturePath"),gcp);
			view.setDataSet(dataSet).setAsVisible(true);
			view.setGranularity(Granularity.BY_DAY);
			view.setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
			view.setExtendedState(Frame.MAXIMIZED_BOTH);
		}
	}
	
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(final String[] args)
	{
		try 
		{	
			init(true);			
		} 
		catch (Exception e) 
		{						
			e.printStackTrace();			
		}
	}

	private static int[] getUserSelection(final Object[] ALL_H_FILES) 
	{
		final JList<Object> list=new JList<Object>(ALL_H_FILES);
		JOptionPane.showMessageDialog(null, list, "'"+PROPERTIES.getProperty("prefix")+"' files to select:", JOptionPane.PLAIN_MESSAGE);
		
		final int[] indexes=list.getSelectedIndices();
		if (indexes.length==0) 
		{	
			JOptionPane.showMessageDialog(null,"No file selected, so exit!");
			System.exit(0);
		}
		return indexes;
	}

	private static Object[] getHFilesList() 
	{
		final File f = new File(PROPERTIES.getProperty("initialDataPath"));
		
		final List<String> available=new ArrayList<String>();
		final List<String> allFiles=Arrays.asList(f.list());
		for (String fn:allFiles)
		{
			if (fn.contains(PROPERTIES.getProperty("prefix"))) available.add(fn.substring(2,fn.indexOf('.')));
		}			
		final Object[] ALL_H_FILES=available.toArray();
		return ALL_H_FILES;
	}

	private static void showDistances(final Instances dataSet,final int tsidx)
	{	
		for (int maxValues:new int[]{1000,5000,10000})
		{
			System.out.println("");
			System.out.println("DTW from "+dataSet.attribute(tsidx).name()+" (first "+maxValues+" values)");
			final double[] startArray1=ArraysUtil.firstValues(dataSet.attributeToDoubleArray(tsidx),maxValues);
			for (int i=0;i<dataSet.numAttributes();i++)
			{	
				if (dataSet.attribute(i).isDate()) continue;
				if (i==tsidx) continue;
				final double[] startArray2=ArraysUtil.lastValues(dataSet.attributeToDoubleArray(i),maxValues);
				final double distance=new DynamicTimeWarping(startArray1,startArray2).getDistance();
				System.out.println("\t to "+dataSet.attribute(i).name()+": "+distance);
			}
			System.out.println("");
			
			System.out.println("");
			System.out.println("DTW from "+dataSet.attribute(tsidx).name()+" (last "+maxValues+" values)");
			final double[] endArray1=ArraysUtil.lastValues(dataSet.attributeToDoubleArray(tsidx),maxValues);		
			for (int i=0;i<dataSet.numAttributes();i++)
			{	
				if (dataSet.attribute(i).isDate()) continue;
				if (i==tsidx) continue;
				final double[] endArray2=ArraysUtil.lastValues(dataSet.attributeToDoubleArray(i),maxValues);
				final double distance=new DynamicTimeWarping(endArray1,endArray2).getDistance();
				System.out.println("\t to "+dataSet.attribute(i).name()+": "+distance);
			}
			System.out.println("");

		}
	}
	
	private static Instances buildDatasetH_(final String name) throws Exception
	{
		final String usedName=name.replaceAll("-","_");
		
		final String SEP="-";
		
		final String dateFormat="dd"+SEP+"MM"+SEP+"yyyy"+SEP+"HH:mm:ss";
		
		final StringBuilder sb=new StringBuilder();
		sb.append("@relation "+PROPERTIES.getProperty("prefix")+usedName+" \n");
		sb.append("@attribute ").append(usedName).append("_d date ").append(dateFormat).append(" \n");
		sb.append("@attribute ").append(usedName).append("_val numeric \n");
		
		sb.append("@data \n");
		
		final Instances emptyds=WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),false);
		
		final String fn=PROPERTIES.getProperty("initialDataPath")+PROPERTIES.getProperty("prefix")+name+".txt";			
		final File file=new File(fn);
		final Scanner input=new Scanner(file);
		while (input.hasNext()) 
		{
		    final String nextLine=input.nextLine();
		    
		    final String[] array=nextLine.split("\t");
		    
		    if (array.length==5)
		    {
		    	final String DDMMYYYY=array[0];
		    	
		    	/* first 'column' */
		    	String HH_MM_SS=array[2];		    	
		    	if (HH_MM_SS.startsWith("24")) HH_MM_SS="00"+HH_MM_SS.substring(2); 		  		 		    		    		    
		    	final String formattedDate=DDMMYYYY.replaceAll("/",SEP)+SEP+HH_MM_SS;		    
		    	try
		    	{
		    		System.out.println("'"+HH_MM_SS+"'");
		    		if (!(HH_MM_SS.endsWith("00")||HH_MM_SS.endsWith("15")||HH_MM_SS.endsWith("30")||HH_MM_SS.endsWith("45")))
		    		{
		    			throw new Exception("hour not managed -> "+HH_MM_SS);
		    		}
		    		
		    		emptyds.attribute(0).parseDate(formattedDate);
		    		
			    	String val=array[1];
			    	if (val.contains("---")) val="?";
			    		    
			    	sb.append(formattedDate).append(",").append(val).append("\n");
		    	}
		    	catch(ParseException pe)
		    	{
		    		System.out.println(name+" -> "+pe.getMessage());
		    		//continue;
		    	}		    
		    
		    	/* second 'column' */
		    	String HH_MM_SS2=array[4];
		    	if (HH_MM_SS2.startsWith("24")) HH_MM_SS2="00"+HH_MM_SS2.substring(2); 		  		 		    		    		    
		    	final String formattedDate2=DDMMYYYY.replaceAll("/",SEP)+SEP+HH_MM_SS2;		    
		    	try
		    	{
		    		if (!(HH_MM_SS2.endsWith("00")||HH_MM_SS2.endsWith("15")||HH_MM_SS2.endsWith("30")||HH_MM_SS2.endsWith("45")))
		    		{
		    			throw new Exception("hour not managed -> "+HH_MM_SS2);
		    		}
		    		
		    		emptyds.attribute(0).parseDate(formattedDate2);
		    		
			    	String val2=array[3];
			    	if (val2.contains("---")) val2="?";
			    		    
			    	sb.append(formattedDate2).append(",").append(val2).append("\n");
		    	}
		    	catch(ParseException pe)
		    	{
		    		System.out.println(name+" -> "+pe.getMessage());
		    		//continue;
		    	}		    
		    
		    }
		    else
		    {
		    	final String DDMMYYYY=array[0];
		    	String HH_MM_SS=array[1];
		    	if (HH_MM_SS.startsWith("24")) HH_MM_SS="00"+HH_MM_SS.substring(2); 		  		 		    		    
		    
		    	final String formattedDate=DDMMYYYY.replaceAll("/",SEP)+SEP+HH_MM_SS;
		    
		    	try
		    	{
		    		emptyds.attribute(0).parseDate(formattedDate);
		    	}
		    	catch(ParseException pe)
		    	{
		    		System.out.println(name+" -> "+pe.getMessage());
		    		continue;
		    	}		    
		    
		    	String val=array[2];
		    	if (val.contains("---")) val="?";
		    		    
		    	sb.append(formattedDate).append(",").append(val).append("\n");
		    }
		}
		input.close();
		
		final Instances ds=WekaDataAccessUtil.loadInstancesFromARFFString(sb.toString(),false);
		return ds;
	}
	
	private static StationsDataProvider loadGeoCoordinatesProvider(final String coordinatesPath,final String[] relationshipsPaths,final String shapeImagePath) throws IOException
	{
		/* load coordinates from path */
		final List<String> lines=Files.readAllLines(Paths.get(coordinatesPath),Charset.defaultCharset());		
		final Map<String,double[]> coordinatesMap=new HashMap<String,double[]>();
		for (int i=1;i<lines.size();i++)
		{
			final String[] s=lines.get(i).split("\t");
			coordinatesMap.put(s[0]+"_val",new double[]{Double.parseDouble(s[1]),Double.parseDouble(s[2])});
		}				
		
		/* load relationships graphs from path */
		final List<CGraph> relationshipsGraphs=new ArrayList<CGraph>();
		for (final String relationshipsPath:relationshipsPaths)
		{
			final List<String> lines2=Files.readAllLines(Paths.get(relationshipsPath),Charset.defaultCharset());				
			final String xmlRepr=FormatterUtil.buildStringFromArrayOfStrings(lines2.toArray(),'\n');
			final CGraph relationshipsGraph=GraphUtil.getCGraphFrom(xmlRepr);
			relationshipsGraphs.add(relationshipsGraph);
		}
		
		/* laod map picture from path */	
		System.err.println("Try "+shapeImagePath);
		final java.awt.Image mapImage=new ImageIcon(shapeImagePath).getImage();
		System.err.println(shapeImagePath);
				
		final StationsDataProvider gcp=new StationsDataProvider(coordinatesMap,relationshipsGraphs,mapImage);
		return gcp;
	}
}
