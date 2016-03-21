/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import lu.lippmann.cdb.weka.*;
import weka.classifiers.Classifier;
import weka.core.*;


/**
 * TimeSeriesGapFillerClassifier.
 * 
 * @author Olivier PARISOT
 *
 */
public class GapFillerClassifier extends GapFiller 
{
	//
	// Instance fields
	//
	
	/** */
	private final Classifier classifier;

	
	//
	// Constructors
	//	

	/**
	 * Constructor.	 
	 */
	GapFillerClassifier(final boolean wdt,final Classifier classifier) 
	{
		super(wdt);		
		this.classifier=classifier;
	}

	
	//
	// Instance methods
	//
		
	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances fillGaps0(final Instances ds) throws Exception 
	{
		final Instances newds=WekaDataProcessingUtil.buildDataSetWithoutConstantAttributes(ds);				
		
		final int attrWithMissingIdx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(newds);
		if (attrWithMissingIdx==-1) throw new IllegalStateException();
						
		final Instances trainingSet=new Instances(newds,0);
		for (int i=0;i<newds.numInstances();i++)
		{
			if (!newds.instance(i).hasMissingValue()) trainingSet.add(newds.instance(i));
		}				
		//System.out.println(trainingSet);		
		trainingSet.setClassIndex(attrWithMissingIdx);
		
		//System.out.println("Training (size="+trainingSet.numInstances()+") ...");		
		this.classifier.buildClassifier(trainingSet);
		//System.out.println("... trained!");
		
		newds.setClassIndex(attrWithMissingIdx);
		for (int i=0;i<newds.numInstances();i++)
		{
			if (newds.instance(i).isMissing(attrWithMissingIdx))
			{				
				final Instance newrecord=new DenseInstance(newds.instance(i));				
				newrecord.setDataset(newds);
				final double newval=this.classifier.classifyInstance(newrecord);				
				newds.instance(i).setValue(attrWithMissingIdx,newval);
			}
		}
		
		//System.out.println("initial -> "+ds.toSummaryString());
		//System.out.println("corrected -> "+newds.toSummaryString());
	
		this.model=this.classifier.toString();
		
		return newds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasExplicitModel()
	{
		return true;
	}
}
