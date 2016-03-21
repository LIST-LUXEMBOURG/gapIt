/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tasks;

import javax.swing.JOptionPane;

import lu.lippmann.cdb.weka.*;
import weka.core.*;


/**
 * Task.
 * 
 * @author the WP1 team
 */
public final class CompareAttributesTask extends Task
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	String getName() 
	{
		return "Compare attributes";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String getIconPath() 
	{
		return "menu/hierarchize.png"; // TODO: change icon
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Instances process0(final Instances dataSet) throws Exception 
	{
		
		final String s1=(String)JOptionPane.showInputDialog(null,"Select an attribute:\n",
				"Attribute selection",
				JOptionPane.PLAIN_MESSAGE,
				null,
				WekaDataStatsUtil.getNominalAttributesNames(dataSet).toArray(),
				"");
		
		final Attribute attr1=dataSet.attribute(s1);
		final WekaClusteringResult wcr1=WekaMachineLearningUtil.computeClustersResultFromNominalAttributeValues(dataSet,attr1.index());
		
		final String s2=(String)JOptionPane.showInputDialog(null,"Select an attribute:\n",
				"Attribute selection",
				JOptionPane.PLAIN_MESSAGE,
				null,
				WekaDataStatsUtil.getNominalAttributesNames(dataSet).toArray(),
				"");
		
		final Attribute attr2=dataSet.attribute(s2);
		final WekaClusteringResult wcr2=WekaMachineLearningUtil.computeClustersResultFromNominalAttributeValues(dataSet,attr2.index());

		JOptionPane.showMessageDialog(null,new WekaClusteringResultsComparison(wcr1,wcr2));

		
		return null;
	}

}
