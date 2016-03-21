/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import java.util.*;

import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.dt.weka.J48DecisionTreeFactory;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.weka.WekaDataProcessingUtil;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class SupervisedFeatureSelectionWithDecisionTreeTask extends Task
{
	//
	// Instance fields
	//
	
	/** */
	private final DecisionTreeFactory dtFactory;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public SupervisedFeatureSelectionWithDecisionTreeTask()
	{
		this(true);
	}
	
	/**
	 * Constructor.
	 */
	public SupervisedFeatureSelectionWithDecisionTreeTask(final boolean pruned)
	{
		final double defaultValueConfidenceFactor=DecisionTreeHelper.LOW_CONFIDENCE_FACTOR;
		this.dtFactory=new J48DecisionTreeFactory(defaultValueConfidenceFactor,!pruned);
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
		return "Simplify with pruned DT (superv.)";
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
		if (dataSet.classIndex()==-1) throw new Exception("Need a selected class!");
		
		final DecisionTree dt=dtFactory.buildCachedDecisionTree(dataSet);
		final Set<Integer> attrToKeep=new HashSet<Integer>();
		
		for (final CNode cn:dt.getGraphWithOperations().getVertices())
		{
			final Attribute attribute=dataSet.attribute(cn.getName());
			if (attribute!=null) attrToKeep.add(attribute.index());
		}
		attrToKeep.add(dataSet.classIndex());
		
		final int size=attrToKeep.size();
		final int[] array=new int[size];
		int i=0;
		final Iterator<Integer> iter=attrToKeep.iterator();
		while (iter.hasNext())
		{	
			array[i]=iter.next().intValue();
			i++;
		}
		
		return WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,array);
	}
}
