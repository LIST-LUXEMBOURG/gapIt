/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka.filters;

import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.filters.*;

/**
 * 
 * @author didry
 *
 */
public final class AllToNumeric extends Filter implements UnsupervisedFilter {

	private static final long serialVersionUID = 6632238367701058526L;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setInputFormat(Instances ds) throws Exception {
		super.setInputFormat(ds);

		final Instances outputFormat = new Instances(ds,0);

		for(int i = 0 ; i < outputFormat.numAttributes() ; i++)
		{
			final Attribute attr = outputFormat.attribute(i);
			if(!attr.isNumeric() && attr.index()!=ds.classIndex())
			{
				outputFormat.deleteAttributeAt(i);
				String newName=attr.name()+"_num";
				while (outputFormat.attribute(newName)!=null) newName+="_";
				final Attribute newAttr = new Attribute(newName);
				outputFormat.insertAttributeAt(newAttr,i);
			}
		}

		for(int i = 0 ; i < ds.numInstances() ; i++){
			outputFormat.add(new DenseInstance(1.0,ds.instance(i).toDoubleArray()));
		}

		setOutputFormat(outputFormat);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean input(Instance instance) throws Exception {
		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		
		push(new DenseInstance(1.0,instance.toDoubleArray()));
		 
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Capabilities getCapabilities() 
	{
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.NO_CLASS);

		return result;
	}

}
