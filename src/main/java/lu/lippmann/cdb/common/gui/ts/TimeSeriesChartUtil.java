/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui.ts;

import java.awt.*;
import java.util.*;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.ColorHelper;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.gantt.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

import weka.core.*;


/**
 * TimeSeriesChartUtil.
 * 
 * @author the WP1 team
 */
public final class TimeSeriesChartUtil 
{
	//
	// Static fields
	//

	/** */
	private static final String DATE_TIME_LABEL="Date/Time";
	
	
	//
	// Constructors
	//
	
	/**
	 * Private constructor.
	 */
	private TimeSeriesChartUtil() {}
	
	
	//
	// Static methods
	//
	
	public static ChartPanel buildChartPanelForNominalAttribute(final Instances ds,final Attribute attr,final int dateIdx) 
	{
	    final TaskSeriesCollection localTaskSeriesCollection=new TaskSeriesCollection();
	    final java.util.List<String> names=new ArrayList<String>();
	    
	    final Set<String> present=WekaDataStatsUtil.getPresentValuesForNominalAttribute(ds,attr.index());
		for (final String pr:present)
	    {
			names.add(pr);
			localTaskSeriesCollection.add(new TaskSeries(pr));			
	    }
		
		final Calendar cal=Calendar.getInstance();
		try
		{
			for (final double[] dd:WekaTimeSeriesUtil.split(ds,attr.index()))
			{
				cal.setTimeInMillis((long)dd[0]);
				final Date start=cal.getTime();
				cal.setTimeInMillis((long)dd[1]);
				final Date end=cal.getTime();
				final String sd=ds.instance((int)dd[2]).stringValue(attr);
				localTaskSeriesCollection.getSeries(sd).add(new Task("T",start,end));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	    final XYTaskDataset localXYTaskDataset=new XYTaskDataset(localTaskSeriesCollection);
	    localXYTaskDataset.setTransposed(true);
	    localXYTaskDataset.setSeriesWidth(0.6D);	

	    final DateAxis localDateAxis=new DateAxis(DATE_TIME_LABEL);
	    final SymbolAxis localSymbolAxis=new SymbolAxis("",names.toArray(new String[names.size()]));
	    localSymbolAxis.setGridBandsVisible(false);
	    final XYBarRenderer localXYBarRenderer=new XYBarRenderer();
	    localXYBarRenderer.setUseYInterval(true);
	    localXYBarRenderer.setShadowVisible(false);
	    final XYPlot localXYPlot=new XYPlot(localXYTaskDataset,localDateAxis,localSymbolAxis,localXYBarRenderer);	    
	    
	    final CombinedDomainXYPlot localCombinedDomainXYPlot=new CombinedDomainXYPlot(new DateAxis(DATE_TIME_LABEL));
	    localCombinedDomainXYPlot.add(localXYPlot);
	    final JFreeChart localJFreeChart=new JFreeChart("", localCombinedDomainXYPlot);
	    localJFreeChart.setBackgroundPaint(Color.white);

	    final ChartPanel cp=new ChartPanel(localJFreeChart,true);
	    cp.setBorder(new TitledBorder(attr.name()));
	    return cp;
	}
	
	private static void fillWithSingleAxis(final Instances dataSet,final int dateIdx,final TimeSeriesCollection tsDataset)
	{
		final int numInstances=dataSet.numInstances();
		
		final Calendar cal=Calendar.getInstance();
		for (final Integer i:WekaDataStatsUtil.getNumericAttributesIndexes(dataSet))
		{	
			if (dataSet.attributeStats(i).missingCount==dataSet.numInstances()) 
			{	
				System.out.println("TimeSeriesChartUtil: Only missing values for '"+dataSet.attribute(i).name()+"', so skip it!");
				continue;
			}
			final TimeSeries ts=new TimeSeries(dataSet.attribute(i).name());							
			for (int k=0;k<numInstances;k++)
			{									
				final Instance instancek=dataSet.instance(k);
				final long timeInMilliSec=(long)instancek.value(dateIdx);
				cal.setTimeInMillis(timeInMilliSec);
				
				if (instancek.isMissing(i))
				{	
					ts.addOrUpdate(new Millisecond(cal.getTime()),null);					
				}
				else
				{
					ts.addOrUpdate(new Millisecond(cal.getTime()),instancek.value(i));
				}
			}
			if (!ts.isEmpty()) tsDataset.addSeries(ts);
		}
	}
	
	private static void fillWithSingleAxisInterval(final Instances dataSet,final int dateIdx,final YIntervalSeriesCollection tsDataset,final double deviation,final int deviatedAttrIdx) 
	{
		final int numInstances=dataSet.numInstances();
		
		for (final Integer i:WekaDataStatsUtil.getNumericAttributesIndexes(dataSet))
		{	
			if (dataSet.attributeStats(i).missingCount==dataSet.numInstances()) 
			{	
				System.out.println("TimeSeriesChartUtil: Only missing values for '"+dataSet.attribute(i).name()+"', so skip it!");
				continue;
			}
			final YIntervalSeries ts=new YIntervalSeries(dataSet.attribute(i).name());							
			for (int k=0;k<numInstances;k++)
			{									
				final Instance instancek=dataSet.instance(k);
				final long timeInMilliSec=(long)instancek.value(dateIdx);
				
				if (instancek.isMissing(i))
				{	
					//ts.add(timeInMilliSec,null,0d,0d);					
				}
				else
				{
					if (i==deviatedAttrIdx&&k>0&&k<(numInstances-1))
					{
						System.out.println(numInstances+" "+k+" "+instancek.value(i)+" "+(instancek.value(i)-deviation)+" "+(instancek.value(i)+deviation));
						ts.add(timeInMilliSec,instancek.value(i),instancek.value(i)-deviation,instancek.value(i)+deviation);
					}
					else
					{
						ts.add(timeInMilliSec,instancek.value(i),instancek.value(i),instancek.value(i));
					}
					//System.out.println(instancek.value(i)+" "+(instancek.value(i)-deviation)+" "+(instancek.value(i)+deviation));
				}
			}
			if (!ts.isEmpty()) tsDataset.addSeries(ts);
		}	
	}
	
	private static void fillWithMultipleAxis(final Instances dataSet,final int dateIdx,final TimeSeriesCollection tsDataset,final JFreeChart tsChart)
	{	
		final int numInstances=dataSet.numInstances();
		
		int axisNumber=0;
		final Calendar cal=Calendar.getInstance();
		for (final Integer i:WekaDataStatsUtil.getNumericAttributesIndexes(dataSet))
		{	
			final TimeSeries ts=new TimeSeries(dataSet.attribute(i).name());				
			for (int k=0;k<numInstances;k++)
			{
				final long timeInMilliSec=(long)dataSet.instance(k).value(dateIdx);
				cal.setTimeInMillis(timeInMilliSec);									
				if (dataSet.instance(k).isMissing(i)) 
				{
					ts.addOrUpdate(new Millisecond(cal.getTime()),null);					
				}
				else
				{
					ts.addOrUpdate(new Millisecond(cal.getTime()),dataSet.instance(k).value(i));
				}
			}
			if (!ts.isEmpty()) 
			{	
				if (axisNumber==0)
				{
					tsDataset.addSeries(ts);						
				}
				else
				{
					final XYPlot plot=tsChart.getXYPlot();
			        final NumberAxis axisToAdd=new NumberAxis(dataSet.attribute(i).name());				        
			        axisToAdd.setAutoRangeIncludesZero(false);
			        plot.setRangeAxis(axisNumber,axisToAdd);
			        final TimeSeriesCollection t=new TimeSeriesCollection();
			        t.addSeries(ts);
			        plot.setDataset(axisNumber,t);
			        plot.mapDatasetToRangeAxis(axisNumber,axisNumber);				        
			        final StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
			        renderer2.setSeriesPaint(0,ColorHelper.getColorForAString(dataSet.attribute(i).name()));
			        plot.setRenderer(axisNumber,renderer2);			        
				}
				axisNumber++;
			}
		}
	}
	
	public static ChartPanel buildChartPanelForAllAttributesInterval(final Instances dataSet,final int dateIdx,final double deviation,final int deviatedAttrIdx) 
	{
		final YIntervalSeriesCollection tsDataset=new YIntervalSeriesCollection();
		final JFreeChart tsChart=ChartFactory.createTimeSeriesChart("","Time","Value",tsDataset,true,true,false);
		tsChart.getXYPlot().setBackgroundPaint(Color.WHITE);		
		
		double startgap0=-1d;
		double endgap0=-1d;
		try 
		{
			java.util.List<double[]> gaps=WekaTimeSeriesUtil.findGaps(dataSet, deviatedAttrIdx);
			startgap0= gaps.get(0)[2]+gaps.get(0)[3];
			//System.out.println("start -> "+startgap0);
			endgap0= gaps.get(1)[2]-1;
			//System.out.println("end -> "+endgap0);
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}
		final double startgap=startgap0;
		final double endgap=endgap0;
		System.out.println("gap --> "+startgap+" "+endgap);
		
		
		
		tsChart.getXYPlot().setRenderer(/*deviatedAttrIdx,*/new DeviationRenderer(true,false)
		{
			/** */
			private static final long serialVersionUID=1234L;

			private boolean inRange(final int item)
			{
				return (item>=startgap&&item<=endgap);				
			}
			
			@Override
	        public boolean getItemShapeVisible(int series, int item)
			{
	               return false;
	        }
			 
	        @Override
	        public boolean getItemLineVisible(int series, int item)
	        {	           
	              return inRange(item);
	        }

		});
		for (int i=0;i<dataSet.numAttributes()-1;i++)
		{
			//final Color cc=ColorHelper.COLORBREWER_ALL_QUALITATIVE[i];
			final Color cc=ColorHelper.getColorForAString(dataSet.attribute(i).name());
			tsChart.getXYPlot().getRenderer().setSeriesPaint(i,cc);
			((AbstractRenderer) tsChart.getXYPlot().getRenderer()).setSeriesFillPaint(i,cc.brighter());
		}
		
		fillWithSingleAxisInterval(dataSet,dateIdx,tsDataset,deviation,deviatedAttrIdx);
			
		final ChartPanel cp=new ChartPanel(tsChart,true);
		if (dataSet.numAttributes()<=2) cp.setBorder(new TitledBorder(dataSet.attribute(0).name()));
		return cp;
	}	

	public static ChartPanel buildChartPanelForAllAttributes(final Instances dataSet,final boolean multAxis,final int dateIdx,final Color color)
	{
		return buildChartPanelForAllAttributes(dataSet,multAxis,dateIdx,color,null);
	}
	
	
	public static ChartPanel buildChartPanelForAllAttributes(final Instances dataSet,final boolean multAxis,final int dateIdx,final Color color,final Collection<XYAnnotation> annotations)
	{
		final TimeSeriesCollection tsDataset=new TimeSeriesCollection();
		final JFreeChart tsChart=ChartFactory.createTimeSeriesChart("","Time","Value",tsDataset,true,true,false);
		tsChart.getXYPlot().setBackgroundPaint(Color.WHITE);
		
		// optimized renderer to avoid to print all points
		tsChart.getXYPlot().setRenderer(new SamplingXYLineRenderer());
		//tsChart.getXYPlot().setRenderer(new XYStepRenderer());
		
		if (color==null)
		{
			for (int i=0;i<dataSet.numAttributes()-1;i++)
			{
				final String attrName=dataSet.attribute(i).name();				
				tsChart.getXYPlot().getRenderer().setSeriesPaint(i,ColorHelper.getColorForAString(attrName));
			}
		}
		else
		{
			tsChart.getXYPlot().getRenderer().setSeriesPaint(0,color);
		}
		
		if (annotations!=null)
		{
			for (final XYAnnotation aa:annotations) tsChart.getXYPlot().addAnnotation(aa);
		}
		
		if (multAxis) fillWithMultipleAxis(dataSet,dateIdx,tsDataset,tsChart);
		else fillWithSingleAxis(dataSet,dateIdx,tsDataset);
		
	    final ChartPanel cp=new ChartPanel(tsChart,true);
	    if (dataSet.numAttributes()<=2) cp.setBorder(new TitledBorder(dataSet.attribute(0).name()));
		return cp;
	}
	
	public static JScrollPane buildPanelWithChartForEachAttribute(final Instances dataSet,final int dateIdx)
	{
		final JXPanel jxpm=new JXPanel();		
		jxpm.setLayout(new GridBagLayout());	
		jxpm.setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
		final GridBagConstraints gbc=new GridBagConstraints();		 
		gbc.gridx=0;
		gbc.gridy=0;		 
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill=GridBagConstraints.BOTH; 
		gbc.insets=new Insets(10,10,10,10);		
		final int numAttributes=dataSet.numAttributes();
		for (int i=0;i<numAttributes;i++)
		{
			final Attribute attr=dataSet.attribute(i);
			System.out.println("Build chart panel for '"+attr.name()+"' time serie ...");
			if (attr.isNominal())
			{
				jxpm.add(buildChartPanelForNominalAttribute(dataSet,attr,dateIdx),gbc);
				gbc.gridy++;
			}
			else if (attr.isNumeric()&&!attr.isDate())
			{				
				try 
				{
					final Instances filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,new int[]{attr.index(),dateIdx});
					jxpm.add(buildChartPanelForAllAttributes(filteredDs,false,1,null),gbc);
					gbc.gridy++;
				} 
				catch (Exception e) 
				{					
					e.printStackTrace();
				}
			}
		}
		return new JScrollPane(jxpm,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
}
