/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;
import java.util.concurrent.*;
import weka.core.*;


/**
 * CompletenessComputer.
 * 
 * @author Olivier PARISOT
 */
public final class CompletenessComputer 
{
	//
	// Static fields
	//	

	/** */
	private static final ExecutorService EXECUTOR_SERVICE=Executors.newCachedThreadPool();
	
	//
	// Instance fields
	//
	
	/** */
	private Instances initialds;
	/** */
	private Map<Integer,String> signaturePerAttribute;
	/** */
	private Map<Integer,AttributeStats> statsPerAttribute;
	/** */
	private Map<Integer,List<String>> elementsPerAttribute;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public CompletenessComputer(final Instances initialds)
	{
		this.initialds=initialds;
		
		this.signaturePerAttribute=new HashMap<Integer,String>();
		this.statsPerAttribute=new HashMap<Integer,AttributeStats>();
		this.elementsPerAttribute=new HashMap<Integer,List<String>>();
		
		final int numAttributes=this.initialds.numAttributes();
		final int numInstances=initialds.numInstances();
		for (int i=0;i<numAttributes;i++)
		{
			final boolean notNumeric=initialds.attribute(i).isNominal();
			
			if (notNumeric)
			{
				final Map<Object,Integer> mbefore=new TreeMap<Object,Integer>(WekaDataStatsUtil.getNominalRepartition(initialds,i));
				this.signaturePerAttribute.put(i,mbefore.toString());
			}
			
			this.statsPerAttribute.put(i,initialds.attributeStats(i));
			
			final List<String> elements=new ArrayList<String>(numInstances);
			
			for (int j=0;j<numInstances;j++)
			{
				final String e;										
				if (notNumeric) e=initialds.instance(j).stringValue(i);
				else e=String.valueOf(initialds.instance(j).value(i));

				elements.add(e);
			}
			this.elementsPerAttribute.put(i,elements);
		}
	}
	
	
	// 
	// Instance methods
	//
	
	public int computeUnchangedCellsCount(final Instances afterds)
	{				
		final int numAttributes=initialds.numAttributes();
		final List<Callable<Integer>> ll=new ArrayList<Callable<Integer>>(numAttributes);
		for (int i=0;i<numAttributes;i++)
		{
			final int ii=i;
			ll.add(new CompletenessComputerThread(afterds,ii));
		}
		
		int r=0;
		try 
		{
			final List<Future<Integer>> ff=EXECUTOR_SERVICE.invokeAll(ll);
			for (final Future<Integer> f:ff) r+=f.get();
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		}

		return r;
	}
	
	private int computeUnchangedCellsCount(final Instances afterds,final int i)
	{
		final int numInstances=initialds.numInstances();		
		final int numInstancesAfter=afterds.numInstances();
			
		final String attrName=initialds.attribute(i).name();
		
		final Attribute afterAttr=afterds.attribute(attrName);
		if (afterAttr!=null)
		{
			final boolean notNumeric=initialds.attribute(i).isNominal();
			final int idxInAfter=afterAttr.index();
			final boolean afterNotNumeric=afterds.attribute(idxInAfter).isNominal();

			if (notNumeric&&afterNotNumeric)
			{
				final String signatureAfter=new TreeMap<Object,Integer>(WekaDataStatsUtil.getNominalRepartition(afterds,afterAttr.index())).toString();
				if (signaturePerAttribute.get(i).equals(signatureAfter)) 
				{	
					return numInstances;
				}
			}
			else if (!notNumeric&&!afterNotNumeric)
			{
				final AttributeStats statsBefore = statsPerAttribute.get(i);
				final AttributeStats statsAfter=afterds.attributeStats(idxInAfter);
				boolean notChanged=initialds.attribute(i).numValues()==afterAttr.numValues();										
				notChanged&=statsBefore.missingCount==statsAfter.missingCount;
				notChanged&=Math.abs(statsBefore.numericStats.sum-statsAfter.numericStats.sum)<0.01d;
				notChanged&=Math.abs(statsBefore.numericStats.min-statsAfter.numericStats.min)<0.01d;
				notChanged&=Math.abs(statsBefore.numericStats.max-statsAfter.numericStats.max)<0.01d;
				notChanged&=Math.abs(statsBefore.numericStats.mean-statsAfter.numericStats.mean)<0.01d;

				if (notChanged)
				{	
					return numInstances;
				}
			}
			
			final List<String> inInitial=new ArrayList<String>(elementsPerAttribute.get(i));
			final List<String> inAfter=new ArrayList<String>(numInstances);				
			for (int j=0;j<numInstancesAfter;j++)
			{
				final String e;										
				if (afterNotNumeric) e=afterds.instance(j).stringValue(idxInAfter);
				else e=String.valueOf(afterds.instance(j).value(idxInAfter));

				if (inInitial.contains(e))
				{
					inInitial.remove(e);
					inAfter.add(e);
				}															
			}
			
			return inAfter.size();
		}
		else
		{
			return 0;
		}
	}
	
	
	//
	// Inner classes
	//
	
	/**
	 * CompletenessComputerThread.
	 * 
	 * @author Olivier PARISOT
	 */
	private final class CompletenessComputerThread implements Callable<Integer> 
	{
		//
		// Instance fields
		//
		
		/** */
		private final Instances afterds;
		/** */
		private final int attributeIdx;

		
		//
		// Constructors
		//
		
		/**
		 * Constructor.
		 */
		private CompletenessComputerThread(final Instances afterds,final int attributeIdx) 
		{
			this.attributeIdx=attributeIdx;
			this.afterds=afterds;
		}

		
		//
		// Instance methods
		//
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Integer call() throws Exception 
		{
			return computeUnchangedCellsCount(afterds,attributeIdx);
		}
	}
}
