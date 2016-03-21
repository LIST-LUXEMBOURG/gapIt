/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka.filters;

import lu.lippmann.cdb.weka.WekaFormatterUtil;
import weka.core.*;
import weka.filters.supervised.attribute.Discretize;


/**
 * SupervisedReadableDiscretize.
 * 
 * Based on the following method: Fayyad & Irani's MDL.
 * 
 * @author Olivier PARISOT
 */
public class SupervisedReadableDiscretize extends Discretize
{
	//
	// Static fields
	//	

	/** Serial version UID. */
	private static final long serialVersionUID=1234L;


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setOutputFormat() 
	{
		if (m_CutPoints == null) 
		{
			setOutputFormat(null);
			return;
		}
		final FastVector attributes = new FastVector(getInputFormat().numAttributes());
		int classIndex = getInputFormat().classIndex();
		final int numAttributes=getInputFormat().numAttributes();

		//Round cut points ...
		for(int i = 0 ; i < m_CutPoints.length ; i++){
			if(m_CutPoints[i]!=null){
				for(int j = 0 ; j < m_CutPoints[i].length ; j++){
					m_CutPoints[i][j] = Utils.roundDouble(m_CutPoints[i][j], 4);
				}
			}
		}

		for(int i = 0; i < numAttributes; i++) 
		{
			if ((m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric())) 
			{
				final String attrname=getInputFormat().attribute(i).name()+"_disc";
				if (!m_MakeBinary) 
				{
					final FastVector attribValues = new FastVector(1);
					if (m_CutPoints[i] == null) 
					{
						attribValues.addElement("'All'");
					} 
					else 
					{
						for(int j = 0; j <= m_CutPoints[i].length; j++) 
						{
							if (j == 0) 
							{
								attribValues.addElement("<="+WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j]) + "");
							} 
							else if (j == m_CutPoints[i].length) 
							{
								attribValues.addElement(">"+ WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j - 1]) + "");
							} 
							else 
							{
								attribValues.addElement(""+ WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j - 1]) + "<.<="+ WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j]) + "");
							}
						}
					}
					attributes.addElement(new Attribute(attrname,attribValues));
				} 
				else 
				{
					if (m_CutPoints[i] == null) 
					{
						final FastVector attribValues = new FastVector(1);
						attribValues.addElement("'All'");
						attributes.addElement(new Attribute(attrname,attribValues));
					} 
					else 
					{
						if (i < getInputFormat().classIndex()) 
						{
							classIndex += m_CutPoints[i].length - 1;
						}
						for(int j = 0; j < m_CutPoints[i].length; j++) 
						{
							final FastVector attribValues = new FastVector(2);
							attribValues.addElement("<="+ WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j]) + "");
							attribValues.addElement(">"+ WekaFormatterUtil.formatAttributeValue(getInputFormat().attribute(i),m_CutPoints[i][j]) + "");
							attributes.addElement(new Attribute(attrname + "_" + (j+1),attribValues));
						}
					}
				}
			} 
			else 
			{
				attributes.addElement(getInputFormat().attribute(i).copy());
			}
		}
		final Instances outputFormat = new Instances(getInputFormat().relationName(), attributes, 0);
		outputFormat.setClassIndex(classIndex);
		setOutputFormat(outputFormat);
	}
	
}
