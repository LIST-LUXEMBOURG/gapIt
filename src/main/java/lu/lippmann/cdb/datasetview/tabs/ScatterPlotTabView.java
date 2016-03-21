/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.ColorHelper;
import lu.lippmann.cdb.common.gui.dataset.InstanceFormatter;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.JXPanel;
import org.jfree.chart.*;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

import weka.core.*;


/**
 * ScatterPlotTabView.
 *
 * @author the WP1 team
 */
public final class ScatterPlotTabView extends AbstractTabView
{
	//
	// Instance fields
	//

	/** */
	private final JXPanel panel;


	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public ScatterPlotTabView()
	{
		super();

		this.panel=new JXPanel();
		this.panel.setLayout(new BorderLayout());
	}


	//
	// Instance methods
	//

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
	public String getName() 
	{
		return "Scatter plot";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return this.panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{	
		if (WekaDataStatsUtil.getNumericAttributesIndexes(dataSet).size()<2) 
		{	
			throw new Exception("Too few numeric attributes");
		}

		update0(dataSet,-1,-1,-1,false);
	}

	private void update0(final Instances dataSet,int xidx,int yidx,int coloridx,final boolean asSerie)
	{
		System.out.println(xidx+" "+yidx);

		this.panel.removeAll();

		if (xidx==-1) xidx=0;
		if (yidx==-1) yidx=1;
		if (coloridx==-1) coloridx=0;

		final Object[] numericAttrNames=WekaDataStatsUtil.getNumericAttributesNames(dataSet).toArray();

		final JComboBox xCombo=new JComboBox(numericAttrNames);
		xCombo.setBorder(new TitledBorder("x"));
		xCombo.setSelectedIndex(xidx);
		final JComboBox yCombo=new JComboBox(numericAttrNames);
		yCombo.setBorder(new TitledBorder("y"));
		yCombo.setSelectedIndex(yidx);
		final JCheckBox jcb=new JCheckBox("Draw lines");
		jcb.setSelected(asSerie);		
		final JComboBox colorCombo=new JComboBox(numericAttrNames);
		colorCombo.setBorder(new TitledBorder("color"));
		colorCombo.setSelectedIndex(coloridx);
		colorCombo.setVisible(dataSet.classIndex()<0);
		
		xCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update0(dataSet,xCombo.getSelectedIndex(),yCombo.getSelectedIndex(),colorCombo.getSelectedIndex(),jcb.isSelected());
			}
		});

		yCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update0(dataSet,xCombo.getSelectedIndex(),yCombo.getSelectedIndex(),colorCombo.getSelectedIndex(),jcb.isSelected());
			}
		});

		colorCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update0(dataSet,xCombo.getSelectedIndex(),yCombo.getSelectedIndex(),colorCombo.getSelectedIndex(),jcb.isSelected());
			}
		});
		
		
		jcb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update0(dataSet,xCombo.getSelectedIndex(),yCombo.getSelectedIndex(),colorCombo.getSelectedIndex(),jcb.isSelected());
			}
		});
		
		final JXPanel comboPanel=new JXPanel();
		comboPanel.setLayout(new GridLayout(1,0));
		comboPanel.add(xCombo);
		comboPanel.add(yCombo);
		comboPanel.add(colorCombo);
		comboPanel.add(jcb);
		this.panel.add(comboPanel,BorderLayout.NORTH);

		final java.util.List<Integer> numericAttrIdx=WekaDataStatsUtil.getNumericAttributesIndexes(dataSet);
		final ChartPanel scatterplotChartPanel=buildChartPanel(dataSet,
																numericAttrIdx.get(xidx),
																numericAttrIdx.get(yidx),
																numericAttrIdx.get(coloridx),
																asSerie);

		this.panel.add(scatterplotChartPanel,BorderLayout.CENTER);

		this.panel.repaint();
		this.panel.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/piechart.png"); // TODO: change
	}
	
	
	//
	// Static methods
	//
	
	private static ChartPanel buildChartPanel(final Instances dataSet,final int xidx,final int yidx,final int coloridx,final boolean asSerie)
	{		
		final XYSeriesCollection data = new XYSeriesCollection();
		final Map<Integer,List<Instance>> filteredInstances = new HashMap<Integer,List<Instance>>();
		final int classIndex=dataSet.classIndex();
		if (classIndex<0)
		{
			final XYSeries series = new XYSeries("Serie",false);
			for (int i=0;i<dataSet.numInstances();i++) 
			{
				series.add(dataSet.instance(i).value(xidx),dataSet.instance(i).value(yidx));
				
			}
			data.addSeries(series);
		}
		else
		{
			final Set<String> pvs=WekaDataStatsUtil.getPresentValuesForNominalAttribute(dataSet,classIndex);
			int p=0;
			for (final String pv:pvs)
			{
				final XYSeries series = new XYSeries(pv,false);
				for (int i=0;i<dataSet.numInstances();i++) 
				{
					if (dataSet.instance(i).stringValue(classIndex).equals(pv))
					{
						if(!filteredInstances.containsKey(p)){
							filteredInstances.put(p,new ArrayList<Instance>());
						}
						filteredInstances.get(p).add(dataSet.instance(i));

						series.add(dataSet.instance(i).value(xidx),dataSet.instance(i).value(yidx));
					}
				}
				data.addSeries(series);

				p++;
			}

		}

		final JFreeChart chart = ChartFactory.createScatterPlot(
				"Scatter Plot", // chart title
				dataSet.attribute(xidx).name(), // x axis label
				dataSet.attribute(yidx).name(), // y axis label
				data, // data
				PlotOrientation.VERTICAL,
				true, // include legend
				true, // tooltips
				false // urls
				);

		final XYPlot xyPlot = (XYPlot) chart.getPlot();
		final XYToolTipGenerator gen = new XYToolTipGenerator() 
		{
			@Override
			public String generateToolTip(final XYDataset dataset,final int series,final int item) 
			{
				if(classIndex < 0)
				{
					return InstanceFormatter.htmlFormat(dataSet.instance(item),true);
				}
				else
				{
					return InstanceFormatter.htmlFormat(filteredInstances.get(series).get(item),true);
				}
			}
		};

		int nbSeries;
		if(classIndex < 0)
		{
			nbSeries=1;
		}else
		{
			nbSeries = filteredInstances.keySet().size();
		}
		
		final XYItemRenderer renderer=new XYLineAndShapeRenderer(asSerie,true) 
		{
	        /** */
			private static final long serialVersionUID=1L;

			@Override
	        public Paint getItemPaint(final int row,final int col) 
	        {
	        	//System.out.println(row+" "+col);
	        	if (classIndex < 0)
	        	{
	        		final double v=dataSet.instance(col).value(coloridx);
	        		final double[] minmax=WekaDataStatsUtil.getMinMaxForAttributeAsArrayOfDoubles(dataSet,coloridx);
	        	
	        		final double rated=(v-minmax[0])/(minmax[1]-minmax[0]);
	        		System.out.println("rated -> "+rated+" min="+minmax[0]+"max="+minmax[1]);
	        		final int colorIdx=(int)Math.round((ColorHelper.YlGnBu_9_COLORS.length-1)*rated);
	        	
	        		//System.out.println(minmax[0]+" "+minmax[1]+" "+v+" "+rated+" "+colorIdx);
	        		return ColorHelper.YlGnBu_9_COLORS[colorIdx];
	        	}
	        	else return super.getItemPaint(row,col);
	        }
		};
		xyPlot.setRenderer(renderer);

		        
		for(int i = 0 ; i < nbSeries ; i++)
		{
			renderer.setSeriesToolTipGenerator(i,gen);
		}

		return new ChartPanel(chart);
	}

    
}
