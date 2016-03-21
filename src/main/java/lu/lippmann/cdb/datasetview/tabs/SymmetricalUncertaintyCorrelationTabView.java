/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.SingleColumnTableModel;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * SymmetricalUncertaintyCorrelationTabView.
 *
 * @author the WP1 team
 */
public final class SymmetricalUncertaintyCorrelationTabView extends AbstractTabView
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
	public SymmetricalUncertaintyCorrelationTabView()
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
		return "Symmetrical uncertainty correlation";
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
					final Instances newds=new Instances(dataSet);
					newds.setClassIndex(xCombo.getSelectedIndex());

					final Map<Attribute,Double> mapCoefficients=WekaDataStatsUtil.computeSymmetricUncertaintyCorrelation(newds);
					
					final JXPanel tblpanel=new JXPanel();
					tblpanel.setLayout(new BorderLayout());
					tblpanel.setBorder(new TitledBorder("Coefficients"));
					final JXTable tbl=new JXTable();
					final SingleColumnTableModel mdl=new SingleColumnTableModel("Coeffs");
					final java.util.List<String> l=new ArrayList<String>();
					for (final Map.Entry<Attribute,Double> entry:mapCoefficients.entrySet()) 
					{
						l.add(entry.getKey().name()+" -> "+entry.getValue());						
					}					
					mdl.setData(l);
					tbl.setModel(mdl);
					tbl.setEditable(true);
					tbl.setShowHorizontalLines(false);
					tbl.setShowVerticalLines(false);
					tbl.setVisibleRowCount(5);
					tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					tblpanel.add(tbl,BorderLayout.CENTER);
					panel.add(tblpanel,BorderLayout.CENTER);
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
