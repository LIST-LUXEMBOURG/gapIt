/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
//import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.common.gui.ts.TimeSeriesChartUtil;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import org.jfree.chart.ChartPanel;
import weka.core.*;


/**
 * TimeSeriesTabView.
 *
 * @author the WP1 team
 */
public class TimeSeriesTabView extends AbstractTabView
{
	//
	// Static fields
	//
	
	/** */
	private static final int MAX_SIZE=1000000;
	
	
	//
	// Instance fields
	//

	//private final ExecutorService EXECUTOR=Executors.newCachedThreadPool();
	
	/** */
	private final JXPanel jxp;
	
	/** */
	private final JTabbedPane tabPannel;
	
	/** */
	private final JXPanel oneGraphOneAxisPanel;
	///** */
	//private final JXPanel oneGraphMultipleAxisPanel;
	/** */
	private final JXPanel multipleGraphsPanel;
	/** */
	private final TimeSeriesClusteringPanel clusteringPanel;
	/** */
	private final TimeSeriesSimilarityPanel similarityPanel;
	/** */
	private final TimeSeriesCalendarPanel calendarPanel;
	
	/** */
	private JXComboBox dateAttributeField;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public TimeSeriesTabView()
	{	
		this(true,true,true,true,true);
	}
	
	/**
	 * Constructor.
	 */
	public TimeSeriesTabView(final boolean withOneGraphOneAxis,final boolean withMultipleGraphs,final boolean withClustering,final boolean withSimilarity,final boolean withCalendar)
	{	
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new BorderLayout());		
				
		this.tabPannel=new JTabbedPane();
		
		if (withOneGraphOneAxis)
		{
			this.oneGraphOneAxisPanel=new JXPanel();		
			this.oneGraphOneAxisPanel.setLayout(new BorderLayout());		
			this.tabPannel.addTab("One graph & one axis",this.oneGraphOneAxisPanel);
		}
		else this.oneGraphOneAxisPanel=null;
		
		//this.tabPannel.addTab("One graph & multiple axis",this.oneGraphMultipleAxisPanel);
		
		if (withMultipleGraphs)
		{
			this.multipleGraphsPanel=new JXPanel();		
			this.multipleGraphsPanel.setLayout(new BorderLayout());	
			this.tabPannel.addTab("Multiple graphs",this.multipleGraphsPanel);
		}
		else this.multipleGraphsPanel=null;
		
		if (withClustering)
		{
			this.clusteringPanel=new TimeSeriesClusteringPanel(this);
			this.tabPannel.addTab("Clustering",clusteringPanel.getComponent());
		}
		else this.clusteringPanel=null;
		
		if (withSimilarity)
		{
			this.similarityPanel=new TimeSeriesSimilarityPanel(TimeSeriesSimilarityPanel.Mode.MDS,false);
			this.tabPannel.addTab("Similarity",similarityPanel.getComponent());
		}
		else this.similarityPanel=null;
		
		if (withCalendar)
		{
			this.calendarPanel=new TimeSeriesCalendarPanel();
			this.tabPannel.addTab("Calendar",calendarPanel.getComponent());
		}
		else this.calendarPanel=null;
		
		
		this.jxp.add(this.tabPannel,BorderLayout.CENTER);
	}

	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSlow()
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Time series";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsDateAttribute()
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{					
		return this.jxp;
	}
	
	private void fillTabs(final Instances dataSet)
	{			
		if (dataSet.numInstances()>MAX_SIZE)
		{
			throw new IllegalStateException("Time series are too long ("+dataSet.numInstances()+"), and records count should be > "+MAX_SIZE+": please filter data before using it.");
		}
		
		final int dateIdx=dataSet.attribute(dateAttributeField.getSelectedItem().toString()).index();		
		
		if (this.oneGraphOneAxisPanel!=null)
		{
			//EXECUTOR.execute(new Runnable()
			//SwingUtilities.invokeLater(new Runnable()
			//{
				//@Override
				//public void run() 
				{
					System.out.println("TimeSeriesTabView: building 'one graph one axis' subpanel ...");		
					oneGraphOneAxisPanel.removeAll();
					final ChartPanel oneGraphOneAxisChartPanel=TimeSeriesChartUtil.buildChartPanelForAllAttributes(dataSet,false,dateIdx,null);
					oneGraphOneAxisPanel.add(oneGraphOneAxisChartPanel,BorderLayout.CENTER);
				}
			//});
		}
		
		/*System.out.println("TimeSeriesTabView: building 'one graph multiple axis' subpanel ...");		
		this.oneGraphMultipleAxisPanel.removeAll();
		final ChartPanel oneGraphMultipleAxisChartPanel=TimeSeriesChartUtil.buildChartPanelForAllAttributes(dataSet,true,dateIdx);
		this.oneGraphMultipleAxisPanel.add(oneGraphMultipleAxisChartPanel,BorderLayout.CENTER);*/
		
		if (this.multipleGraphsPanel!=null)
		{
			//EXECUTOR.execute(new Runnable()
			//SwingUtilities.invokeLater(new Runnable()
			//{
				//@Override
				//public void run() 
				{
					System.out.println("TimeSeriesTabView: building 'multiple graphs' subpanel ...");		
					multipleGraphsPanel.removeAll();
					multipleGraphsPanel.add(TimeSeriesChartUtil.buildPanelWithChartForEachAttribute(dataSet,dateIdx),BorderLayout.CENTER);
				}
			//});			
		}
		
		if (this.clusteringPanel!=null)
		{
			//EXECUTOR.execute(new Runnable()
			//SwingUtilities.invokeLater(new Runnable()
			//{
				//@Override
				//public void run() 
				{
					System.out.println("TimeSeriesTabView: building 'clustering' subpanel ...");
					clusteringPanel.refresh(dataSet,dateIdx);
				}
			//});	
		}		
		
		if (this.similarityPanel!=null)
		{
			//EXECUTOR.execute(new Runnable()
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run() 
				{
					System.out.println("TimeSeriesTabView: building 'similarity' subpanel ...");
					similarityPanel.refresh(dataSet);
				}
			});	
		}
		
		if (this.calendarPanel!=null)
		{
			//EXECUTOR.execute(new Runnable()
			//SwingUtilities.invokeLater(new Runnable()
			//{
				//@Override
				//public void run() 
				{
					System.out.println("TimeSeriesTabView: building 'calendar' subpanel ...");
					calendarPanel.refresh(dataSet,dateIdx);
				}
			//});				
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 					
		if (this.dateAttributeField!=null) 
		{	
			this.jxp.remove(this.dateAttributeField);
			this.dateAttributeField=null;
			this.jxp.updateUI();
		}
		
		final java.util.List<String> dateAttributeNames=WekaDataStatsUtil.getDateAttributeNames(dataSet);
		final boolean hasDateAttributes=(!dateAttributeNames.isEmpty())
				/*&&(WekaDataStatsUtil.getNumericAttributesIndexes(dataSet).size()>0)*/;		
		
		if (hasDateAttributes) 
		{						
			this.dateAttributeField=new JXComboBox(dateAttributeNames.toArray());	
			this.dateAttributeField.setBorder(new TitledBorder("Date attribute"));
			this.jxp.add(this.dateAttributeField,BorderLayout.SOUTH);
			this.dateAttributeField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					fillTabs(dataSet);				
				}
			});

			new AbstractSimpleAsync<Void>(true)
			{
				@Override
				public Void execute() throws Exception 
				{
					fillTabs(dataSet);
					return null;
				}

				@Override
				public void onSuccess(Void result) 
				{
					
				}

				@Override
				public void onFailure(Throwable caught) 
				{
					caught.printStackTrace();					
				}
			}.start();
				
		}
		else 
		{	
			throw new Exception("No date attributes in the dataset.");
		}				
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/time-series.png");
	}
}
