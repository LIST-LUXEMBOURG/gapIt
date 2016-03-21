/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka.filters;

import lu.lippmann.cdb.weka.*;
import weka.core.*;
import weka.filters.unsupervised.attribute.Discretize;


/**
 * UnsupervisedReadableDiscretize.
 * 
 * @author Olivier PARISOT
 */
public final class UnsupervisedReadableDiscretize extends Discretize
{
	//
	// Static fields
	//	

	/** Serial version UID. */
	private static final long serialVersionUID=1234L;


	//
	// Instance methods
	//

	private boolean isAttrToDiscretize(final int i)
	{
		return (m_DiscretizeCols.isInRange(i)) && (getInputFormat().attribute(i).isNumeric()) && (!getInputFormat().attribute(i).isDate());
	}
	
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
			if (isAttrToDiscretize(i)) 
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void convertInstance(Instance instance) 
	{
		int index = 0;
		double[] vals = new double[outputFormatPeek().numAttributes()];
		// Copy and convert the values
		for (int i = 0; i < getInputFormat().numAttributes(); i++) 
		{
			if (isAttrToDiscretize(i)
					&& (getInputFormat().classIndex() != i)) 
			{
				int j;
				double currentVal = instance.value(i);
				if (m_CutPoints[i] == null) 
				{
					if (instance.isMissing(i)) 
					{
						vals[index] = Utils.missingValue();
					} 
					else 
					{
						vals[index] = 0;
					}
					index++;
				} 
				else 
				{
					if (!m_MakeBinary) 
					{
						if (instance.isMissing(i)) 
						{
							vals[index] = Utils.missingValue();
						} 
						else 
						{
							for (j = 0; j < m_CutPoints[i].length; j++) 
							{
								if (currentVal <= m_CutPoints[i][j]) 
								{
									break;
								}
							}
							vals[index] = j;
						}
						index++;
					} 
					else 
					{
						for (j = 0; j < m_CutPoints[i].length; j++) 
						{
							if (instance.isMissing(i)) 
							{
								vals[index] = Utils.missingValue();
							} 
							else if (currentVal <= m_CutPoints[i][j]) 
							{
								vals[index] = 0;
							} 
							else 
							{
								vals[index] = 1;
							}
							index++;
						}
					}
				}
			} 
			else 
			{
				vals[index] = instance.value(i);
				index++;
			}
		}

		Instance inst = null;
		if (instance instanceof SparseInstance) 
		{
			inst = new SparseInstance(instance.weight(), vals);
		} 
		else 
		{
			inst = new DenseInstance(instance.weight(), vals);
		}
		inst.setDataset(getOutputFormat());
		copyValues(inst, false, instance.dataset(), getOutputFormat());
		inst.setDataset(getOutputFormat());
		push(inst);
	}
	
}
