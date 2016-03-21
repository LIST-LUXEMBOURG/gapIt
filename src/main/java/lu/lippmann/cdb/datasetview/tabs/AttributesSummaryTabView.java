/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import javax.swing.*;

import org.jdesktop.swingx.JXPanel;

import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.weka.WekaDataProcessingUtil;
import weka.core.Instances;
import weka.gui.beans.AttributeSummarizer;


/**
 * AttributesSummaryTabView.
 *
 * @author the WP1 team
 */
public final class AttributesSummaryTabView extends AbstractTabView
{
	//
	// Instance fields
	//
	
	/** */
	private final JXPanel jxp;
	/** */
	private final AttributeSummarizer as;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public AttributesSummaryTabView()
	{
		super();
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	
		this.as=new AttributeSummarizer();		
		this.jxp.add(this.as,BorderLayout.CENTER);
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
		return "Attributes summary";
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
	public Component getComponent() 
	{			
		return this.jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{					
		final int numAttr=dataSet.numAttributes();
		
		final JTabbedPane tabbedPane=new JTabbedPane();		
				
		this.as.setGridWidth(Math.min(3,numAttr));		
		this.as.setColoringIndex(dataSet.classIndex());
		this.as.setInstances(dataSet);		
		tabbedPane.addTab("All",this.as);
		
		for (int i=0;i<numAttr;i++)
		{
			final Instances nds=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,new int[]{i});
			final AttributeSummarizer as0=new AttributeSummarizer();
			as0.setGridWidth(1);		
			as0.setColoringIndex(nds.classIndex());
			as0.setInstances(nds);			
			tabbedPane.addTab(dataSet.attribute(i).name(),as0);
		}		
		
		this.jxp.removeAll();
		this.jxp.add(tabbedPane,BorderLayout.CENTER);
		this.jxp.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/attributes-summary.png");
	}
}
