/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import lu.lippmann.cdb.lab.regression.Regression;
import lu.lippmann.cdb.weka.*;
import weka.core.Instances;


/**
 * TimeSeriesGapFillerRegressions.
 * 
 * @author Olivier PARISOT
 *
 */
public class GapFillerRegressions extends GapFiller 
{
	//
	// Constructors
	//
	
	/**
	 * Constructor.	 
	 */
	GapFillerRegressions(final boolean wdt) 
	{
		super(wdt);		
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
		final int numInstances=newds.numInstances();
		
		final int attrWithMissingIdx=WekaDataStatsUtil.getFirstAttributeWithMissingValue(newds);
		if (attrWithMissingIdx==-1) throw new IllegalStateException();
						
		final Instances trainingSet=new Instances(newds,0);
		for (int i=0;i<numInstances;i++)
		{
			if (!newds.instance(i).hasMissingValue()) trainingSet.add(newds.instance(i));
		}				
		//System.out.println(trainingSet);
		
		final Regression reg=new Regression(trainingSet,attrWithMissingIdx);
		final double[] coeffs=reg.getCoe();
		//System.out.println(reg.getR2());
		//System.out.println(reg.getCoeDesc());

		for (int i=0;i<numInstances;i++)
		{
			if (newds.instance(i).isMissing(attrWithMissingIdx))
			{				
				double newval=coeffs[0];
				for (int j=1;j<trainingSet.numAttributes();j++) 
				{
					if (j==attrWithMissingIdx) continue;
										
					final String attrName=trainingSet.attribute(j).name();
					
					//System.out.println(reg.getCoef(attrName)+" * "+attrName);
					
					newval+=reg.getCoef(attrName)*newds.instance(i).value(newds.attribute(attrName));
				}
				//System.out.println("oldval -> "+newds.instance(i).value(attrWithMissingIdx));
				//System.out.println("newval -> "+newval);
				newds.instance(i).setValue(attrWithMissingIdx,newval);
			}
		}
		
		//System.out.println("corrected -> "+newds);
		
		this.model=reg.getCoeDesc();
		
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
