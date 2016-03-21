/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.common.gui.ts.TimeSeriesChartUtil;
import lu.lippmann.cdb.datasetview.tabs.TabView.*;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * TimeSeriesClusteringPanel.
 *
 * @author the WP1 team
 */
public final class TimeSeriesClusteringPanel
{
	//
	// Static fields
	//

	/** */
	private static final String CLUSTER_ATTRIBUTE_NAME="ts_cluster";
	/** */
	private static int CLUTERING_IDX=0;
	
	
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	/** */
	private Component currentChartPanel;
	
	/** */
	private final JSlider slider;
	/** */
	private ChangeListener cl;

	/** */
	private final JXButton addClustButton;
	/** */
	private ActionListener al;

	/** */
	private final AbstractTabView atv;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public TimeSeriesClusteringPanel(final AbstractTabView atv)
	{	
		this.atv=atv;
		
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new BorderLayout());		
		
		this.slider=new JSlider();
		this.slider.setBorder(new TitledBorder("Clusters count"));
		this.slider.setOpaque(false);
		this.slider.setMaximum(10);
		this.slider.setValue(2);
		this.slider.setMinimum(1);
		this.slider.setMinorTickSpacing(1);
		this.slider.setMajorTickSpacing(3);
		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);
		this.slider.setSnapToTicks(true);
		
		this.jxp.add(this.slider,BorderLayout.SOUTH);
		
		this.addClustButton=new JXButton("Add clusters info into the dataset");
		this.jxp.add(this.addClustButton,BorderLayout.NORTH);
	}

	
	//
	// Instance methods
	//
	
	public Component getComponent() 
	{					
		return jxp;
	}
	
	public void refresh(final Instances dataSet,final int dateIdx)
	{
		this.jxp.add(new JXLabel("Computing in progress..."),BorderLayout.CENTER);
		final Thread t=new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				refresh0(dataSet,dateIdx);				
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	public void refresh0(final Instances dataSet,final int dateIdx)
	{		 	
		if (this.currentChartPanel!=null) 
		{	
			this.jxp.remove(this.currentChartPanel);
			this.currentChartPanel=null;
			this.jxp.updateUI();
		}
		
		if (this.cl!=null) this.slider.removeChangeListener(cl);
		
		this.cl=new ChangeListener()
		{						
			@Override
			public void stateChanged(final ChangeEvent e) 
			{
				if (!slider.getValueIsAdjusting()) 
				{												
					refresh(dataSet,dateIdx);
				}
			}
		};
		this.slider.addChangeListener(cl);
		
		Instances clusterAssignementDataSet0=dataSet;
		try 
		{
			clusterAssignementDataSet0=buildClusteredDataSet(dataSet,slider.getValue());		
			final Attribute clusterAttr=clusterAssignementDataSet0.attribute(CLUSTER_ATTRIBUTE_NAME+CLUTERING_IDX);
			this.currentChartPanel=TimeSeriesChartUtil.buildChartPanelForNominalAttribute(clusterAssignementDataSet0,clusterAttr,dateIdx);
			this.jxp.add(this.currentChartPanel,BorderLayout.CENTER);
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
		}
		final Instances clusterAssignementDataSet=clusterAssignementDataSet0;
		
		if (this.al!=null) addClustButton.removeActionListener(this.al);
		
		this.al=new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				atv.pushDataChange(new DataChange(clusterAssignementDataSet,DataChangeTypeEnum.Update));			
			}
		};		
		this.addClustButton.addActionListener(al);
	}
	
	
	//
	// Static methods
	//
	
	private static Instances buildClusteredDataSet(final Instances dataSet,final int k) throws Exception
	{
		CLUTERING_IDX++;
		return WekaTimeSeriesUtil.buildClusteredDataSet(dataSet, k, CLUSTER_ATTRIBUTE_NAME+CLUTERING_IDX);
	}

}
