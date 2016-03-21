/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.util.Map;
import javax.swing.Icon;

import lu.lippmann.cdb.common.gui.ColorHelper;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jfree.chart.*;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import weka.core.Instances;


/**
 * PieChartTabView.
 *
 * @author the WP1 team
 */
public final class PieChartTabView extends AbstractTabView
{
	//
	// Instance fields
	//
	
	/** */
	private final JFreeChart pieChart;
	/** */
	private final DefaultPieDataset pieDataset;
	/** */
	private final ChartPanel chartPanel;
	

	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public PieChartTabView()
	{
		super();
		
		this.pieDataset=new DefaultPieDataset();
		this.pieChart=ChartFactory.createPieChart("",this.pieDataset,true,true,true);
		final PiePlot plot=(PiePlot)this.pieChart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		this.chartPanel=new ChartPanel(this.pieChart,true);
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Pie chart";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return chartPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 		
		for (final Object k:this.pieDataset.getKeys())
		{
			this.pieDataset.remove((Comparable<?>)k);
		}
		
		final int numInstances=dataSet.numInstances();
		for (final Map.Entry<Object,Integer> entry:WekaDataStatsUtil.getClassRepartition(dataSet).entrySet())
		{			
			if (entry.getValue().doubleValue()>0)
			{
				this.pieDataset.setValue(entry.getKey().toString(),entry.getValue().doubleValue()/numInstances);
				final Color color=ColorHelper.getRandomBrightColor();			
				((PiePlot)this.pieChart.getPlot()).setSectionPaint(entry.getKey().toString(),color);
			}
		}
		
		chartPanel.repaint();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/piechart.png");
	}
}
