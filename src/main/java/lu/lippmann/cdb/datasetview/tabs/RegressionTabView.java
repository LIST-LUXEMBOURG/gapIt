/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.SingleColumnTableModel;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.lab.regression.Regression;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * RegressionTabView.
 *
 * @author the WP1 team
 */
public final class RegressionTabView extends AbstractTabView
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
	public RegressionTabView()
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
		return "Regression";
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
		this.panel.removeAll();
		
		final Object[] attrNames=WekaDataStatsUtil.getAttributeNames(dataSet).toArray();
		final JComboBox xCombo=new JComboBox(attrNames);
		xCombo.setBorder(new TitledBorder("Attribute to evaluate"));
		
		final JXPanel comboPanel=new JXPanel();
		comboPanel.setLayout(new GridLayout(1,2));
		comboPanel.add(xCombo);
		final JXButton jxb=new JXButton("Compute");
		comboPanel.add(jxb);
		this.panel.add(comboPanel,BorderLayout.NORTH);
		
		jxb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					final Regression reg=new Regression(dataSet,xCombo.getSelectedIndex());
					
					final Instances newds=new Instances(dataSet);
					newds.insertAttributeAt(new Attribute("_regEval_"),newds.numAttributes());
					
					final double[] estims=reg.getEstims();
					for (int i=0;i<estims.length;i++)
					{
						newds.instance(i).setValue(newds.numAttributes()-1,estims[i]);
					}
					
					final ScatterPlotTabView scatterPlotView=new ScatterPlotTabView();
					scatterPlotView.update0(newds);					
					panel.add(scatterPlotView.getComponent(),BorderLayout.CENTER);
					
					final JXPanel tblpanel=new JXPanel();
					tblpanel.setLayout(new BorderLayout());
					tblpanel.setBorder(new TitledBorder("Coefficients"));
					final JXTable tbl=new JXTable();
					final SingleColumnTableModel mdl=new SingleColumnTableModel("Coeffs");
					final java.util.List<String> l=new ArrayList<String>();
					l.add("R2: "+reg.getR2());
					int ii=0;
					for (final double d:reg.getCoe()) 
					{
						if (ii==0) l.add(" -> "+d);
						else l.add(dataSet.attribute(ii-1).name()+" -> "+d);
						ii++;
					}					
					mdl.setData(l);
					tbl.setModel(mdl);
					tbl.setEditable(true);
					tbl.setShowHorizontalLines(false);
					tbl.setShowVerticalLines(false);
					tbl.setVisibleRowCount(5);
					tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					tblpanel.add(tbl,BorderLayout.CENTER);
					panel.add(tblpanel,BorderLayout.SOUTH);
					//panel.add(new JXLabel("R2: "+reg.getR2()+", "+reg.getCoeDesc()),BorderLayout.SOUTH);
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
					panel.add(new JXLabel("Error during computation: "+e1.getMessage()),BorderLayout.CENTER);
				}
				
			}
		});
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/piechart.png"); // TODO: change
	}
}
