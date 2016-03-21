/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.util;

import java.io.File;
import weka.core.*;
import lu.lippmann.cdb.weka.WekaDataAccessUtil;


/**
 * TransformTimeSeries.
 * 
 * @author
 */
public class TransformTimeSeries 
{
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static final void main(final String[] args)
	{
		try
		{
			final Instances dataSet=WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File("."+File.separatorChar+"data_fake"+File.separatorChar+"all_valid_q_series_complete2.arff"));
			System.out.println(dataSet.toSummaryString());
			
			final int numAttributes=dataSet.numAttributes();
			final int numInstances=dataSet.numInstances();
			for (int i=0;i<numAttributes;i++)
			{
				final int i_bis=(int)(Math.random()*(double)(numAttributes-3));
				final int i_tri=(int)(Math.random()*(double)(numAttributes-3));
								
				for (int j=0;j<numInstances;j++)
				{
					final Instance instance_j=dataSet.instance(j);
					
					if (instance_j.isMissing(i)) continue;
					if (instance_j.isMissing(i_bis)) continue;
					if (instance_j.isMissing(i_tri)) continue;
						
					final double iValue=instance_j.value(i);
					final double iBisValue=instance_j.value(i_bis);
					final double iTriValue=instance_j.value(i_tri);
					
					instance_j.setValue(i,(iValue+iBisValue+iTriValue));
				}
			}
			
			WekaDataAccessUtil.saveInstancesIntoARFFFile(dataSet,new File("."+File.separatorChar+"data_fake"+File.separatorChar+"all_valid_q_series_complete2_fake.arff"));
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
	}
}
