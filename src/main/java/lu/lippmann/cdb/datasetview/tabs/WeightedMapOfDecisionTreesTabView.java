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
import javax.swing.event.*;

import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.gui.MultiPanel;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.dt.ui.DecisionTreeToGraphViewHelper;
import lu.lippmann.cdb.dt.weka.J48DecisionTreeFactory;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.weka.*;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.jdesktop.swingx.JXPanel;
import weka.core.*;


/**
 * WeightedMapOfDecisionTreesTabView.
 *
 * @author the WP1 team
 */
public final class WeightedMapOfDecisionTreesTabView extends AbstractTabView
{
	//
	// Instance fields
	//
	
	/** */
	private final ApplicationContext applicationContext;
	
	/** */
	private final EventPublisher eventPublisher;
	
	/** */
	private final CommandDispatcher commandDispatcher;
	
	/** */
	private DecisionTreeFactory dtFactory;
	/** */
	private MultiPanel mp;	
	/** */
	private JXPanel panel;
	
	/** */
	private final JSlider slider;
	/** */
	private ChangeListener cl;

	/** */
	final JCheckBox withWeightCheckBox;
	/** */
	private JComboBox attrSelectionCombo;
	/** */
	private ActionListener attrSelectionComboListener;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public WeightedMapOfDecisionTreesTabView(final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher,final ApplicationContext applicationContext)
	{
		super();
		
		this.applicationContext=applicationContext;
		this.commandDispatcher=commandDispatcher;
		this.eventPublisher=eventPublisher;
		
		final double defaultValueConfidenceFactor=DecisionTreeHelper.HIGH_CONFIDENCE_FACTOR;
		this.dtFactory=new J48DecisionTreeFactory(defaultValueConfidenceFactor,false);
		this.panel=new JXPanel();
		this.panel.setLayout(new BorderLayout());
		
		this.slider=new JSlider();
		this.slider.setBorder(new TitledBorder("Confidence factor"));
		this.slider.setOpaque(false);
		this.slider.setMaximum((int)(defaultValueConfidenceFactor*100));
		this.slider.setValue((int)(defaultValueConfidenceFactor*100));
		this.slider.setMinimum(1);
		this.slider.setMinorTickSpacing(1);
		this.slider.setMajorTickSpacing(10);
		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);				
		this.panel.add(this.slider,BorderLayout.SOUTH);
		
		this.attrSelectionCombo=new JComboBox();
		this.attrSelectionCombo.setBorder(new TitledBorder("Attribute used to split"));
		
		final JXPanel cmdPanel=new JXPanel();
		cmdPanel.setLayout(new BorderLayout());
		cmdPanel.add(this.attrSelectionCombo,BorderLayout.CENTER);
		this.withWeightCheckBox=new JCheckBox("Weighted");
		this.withWeightCheckBox.setBorder(new TitledBorder("With weight"));
		cmdPanel.add(this.withWeightCheckBox,BorderLayout.EAST);
		this.panel.add(cmdPanel,BorderLayout.NORTH);
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
		return "Weighted map of decision trees";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{		 		
		if (this.mp!=null) this.panel.remove(this.mp);	
		
		if (this.cl!=null) this.slider.removeChangeListener(cl);
		//if (this.cl!=null) this.slider.removeChangeListener(cl);
		
		this.cl=new ChangeListener()
		{						
			@Override
			public void stateChanged(final ChangeEvent e) 
			{
				if (!slider.getValueIsAdjusting()) 
				{												
					dtFactory=new J48DecisionTreeFactory(slider.getValue()/100d,false);
					update(dataSet);
				}
			}
		};
		this.slider.addChangeListener(cl);

		final double frameWidth=this.panel.getSize().getWidth()*0.95d;
		final double frameHeight=this.panel.getSize().getHeight()*0.95d;
		
		final ListOrderedMap<JComponent,Integer> mapPanels=new ListOrderedMap<JComponent,Integer>();
				
		final String oldSelected;
		if (this.attrSelectionCombo.getSelectedItem()==null)
		{			
			oldSelected=dataSet.classAttribute().name();
		}
		else			
		{
			final Attribute oldAttr=dataSet.attribute(this.attrSelectionCombo.getSelectedItem().toString());
			if (oldAttr!=null)
			{
				oldSelected=oldAttr.name();
			}
			else
			{
				oldSelected=dataSet.classAttribute().name();
			}
		}
		final int idx=dataSet.attribute(oldSelected).index();
		final Set<Object> presentValues=WekaDataStatsUtil.getNominalRepartition(dataSet,idx).keySet();
		for (final Object o:presentValues)
		{
			final Instances part=WekaDataProcessingUtil.filterDataSetOnNominalValue(dataSet,idx,o.toString());
			final DecisionTree dti=dtFactory.buildDecisionTree(part);
			
			final int ratio=100*part.numInstances()/dataSet.numInstances();
			final GraphView myGraph=DecisionTreeToGraphViewHelper.buildGraphView(dti,eventPublisher,commandDispatcher);
			myGraph.hideSharedLabel();
			myGraph.addMetaInfo("size="+dti.getSize(),"");
			myGraph.addMetaInfo("depth="+dti.getDepth(),"");
			myGraph.addMetaInfo("err="+FormatterUtil.DECIMAL_FORMAT.format(100d*dti.getErrorRate())+"%","");
			
			final JButton openInEditorButton = new JButton("Edit");
			openInEditorButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					 GraphUtil.importDecisionTreeInEditor(dtFactory, part, applicationContext, eventPublisher, commandDispatcher);
				}
			});
			myGraph.addMetaInfoComponent(openInEditorButton);
			
			myGraph.fitGraphToSubPanel(frameWidth-10*presentValues.size(),frameHeight-10,ratio);
			mapPanels.put((JComponent)myGraph,ratio);

		}
		this.mp=new MultiPanel(mapPanels,(int)frameWidth,(int)frameHeight,this.withWeightCheckBox.isSelected());
		
		this.panel.add(this.mp,BorderLayout.CENTER);
		
		if (this.attrSelectionCombo.getActionListeners().length>0) 
		{	
			this.attrSelectionCombo.removeActionListener(attrSelectionComboListener);
		}
		if (this.withWeightCheckBox.getActionListeners().length>0) 
		{	
			this.withWeightCheckBox.removeActionListener(attrSelectionComboListener);
		}
		
		this.attrSelectionCombo.removeAllItems();
		for (final Attribute attr:WekaDataStatsUtil.getNominalAttributesList(dataSet))
		{
			this.attrSelectionCombo.addItem(attr.name());
		}
		this.attrSelectionCombo.setSelectedItem(oldSelected);
		
		this.attrSelectionComboListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update(dataSet);		
			}
		};	
		this.attrSelectionCombo.addActionListener(attrSelectionComboListener);
		this.withWeightCheckBox.addActionListener(attrSelectionComboListener);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/weighted-decision-trees.png");
	}


	
}
