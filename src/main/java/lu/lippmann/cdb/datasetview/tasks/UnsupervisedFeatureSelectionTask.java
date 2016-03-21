/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import lu.lippmann.cdb.common.ArraysUtil;
import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class UnsupervisedFeatureSelectionTask extends Task
{
	//
	// Instance fields
	//
	
	/** */
	private final double ratio;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public UnsupervisedFeatureSelectionTask()
	{
		this(-1);
	}
	
	/**
	 * Constructor.
	 */
	public UnsupervisedFeatureSelectionTask(final double ratio)
	{
		this.ratio=ratio;
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Simplify (unsuperv.)";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/simplify.png";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{		
		final int k;
		if (this.ratio==-1) k=getFeaturesCountFromInput(null,dataSet.numAttributes());
		else k=(int)Math.round(this.ratio*dataSet.numAttributes());
		
		final List<Integer> attrToKeep=WekaMachineLearningUtil.computeUnsupervisedFeaturesSelection(dataSet,k);
		if (!attrToKeep.contains(dataSet.classIndex())) attrToKeep.add(dataSet.classIndex());
		final int[] array=ArraysUtil.transform(attrToKeep);
		
		System.out.println("unsupervised fs -> before="+dataSet.numAttributes()+" after="+array.length);
		
		final Instances newds=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,array);
		final Attribute clsAttr=newds.attribute(dataSet.classAttribute().name());
		System.out.println(clsAttr+" "+dataSet.classAttribute().name());
		newds.setClass(clsAttr);
		return newds;
	}
	
	
	//
	// Static methods
	//
	
	private static int getFeaturesCountFromInput(final Component parent,final int maxFeaturesCount) 
	{
		final JOptionPane optionPane = new JOptionPane();
		final JSlider slider=new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(maxFeaturesCount);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(maxFeaturesCount/2);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		final ChangeListener changeListener=new ChangeListener() 
		{
			@Override
			public void stateChanged(final ChangeEvent changeEvent) 
			{
				if (!slider.getValueIsAdjusting()) 
				{
					optionPane.setInputValue(new Integer(slider.getValue()));
				}
			}
		};		
		slider.addChangeListener(changeListener);
		slider.setValue(maxFeaturesCount/2);
		optionPane.setMessage(new Object[] { "Features count: ", slider });
		optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		optionPane.setOptionType(JOptionPane.YES_OPTION);
		final JDialog dialog=optionPane.createDialog(parent,"Input");
		dialog.setVisible(true);
		return Integer.valueOf(optionPane.getInputValue().toString());
	}
}
