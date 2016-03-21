/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.util.ArrayList;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.lab.mds.*;
import lu.lippmann.cdb.lab.timeseries.DynamicTimeWarping;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;

import weka.core.*;


/**
 * TimeSeriesSimilarityPanel.
 *
 * @author the WP1 team
 */
public final class TimeSeriesSimilarityPanel
{
	//
	// Static fields
	//	
	
	/** */
	private static final String FEATUREDESC_ATTRNAME="FEATUREDESC_ATTRNAME";

	/** */
	private static final int MAX_ROWS_COUNT=5000; // magic number
	
	
	//
	// Instance fields
	//

	/** */
	private final Mode mode;
	/** */
	private final boolean withNormalization;
	
	/** */
	private final JXPanel jxp;	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public TimeSeriesSimilarityPanel(final Mode mode,final boolean withNormalization)
	{	
		this.mode=mode;		
		this.withNormalization=withNormalization;
		
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new BorderLayout());		
	}

	
	//
	// Instance methods
	//
	
	public Component getComponent() 
	{					
		return jxp;
	}
	
	public void refresh(final Instances dataSet)
	{
		this.jxp.add(new JXLabel("Computing in progress..."),BorderLayout.CENTER);
		final Thread t=new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				refresh0(dataSet);				
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	private void refresh0(final Instances dataSet)
	{		 	
		if (dataSet.numInstances()<MAX_ROWS_COUNT)
		{
			try 
			{			
				final Instances newds=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet, WekaDataStatsUtil.getNumericAttributesIndexesAsArray(dataSet));
				final Instances featuresDs=buildFeatureDS(newds);
				
				final JXPanel projView;				
				if (mode.equals(Mode.MDS))
				{
					final int limitForCollapsing=1000;
					final boolean normalized = false; //TODO checkbox
					final MDSResult mdsResult=ClassicMDS.doMDS(featuresDs,MDSDistancesEnum.EUCLIDEAN,2,limitForCollapsing,true,normalized);		
					projView=MDSViewBuilder.buildMDSViewFromDataSet(featuresDs,mdsResult,limitForCollapsing,new Listener<Instances>()
					{
						@Override
						public void onAction(final Instances parameter) {}
					}
					,FEATUREDESC_ATTRNAME);
				}
				else
				{
					projView=null;
				}
				this.jxp.removeAll();
				this.jxp.add(projView,BorderLayout.CENTER);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			this.jxp.removeAll();
			this.jxp.add(new JXLabel("Time series are too big (limit="+MAX_ROWS_COUNT+")."),BorderLayout.CENTER);
		}
	}
	
	private Instances buildFeatureDS(final Instances dataSet) throws Exception
	{
		final int numAttributes=dataSet.numAttributes();
		
		final java.util.List<String> namesOfFeaturesToConsider=new ArrayList<String>();
		namesOfFeaturesToConsider.addAll(WekaDataStatsUtil.getAttributeNames(dataSet));
		namesOfFeaturesToConsider.removeAll(WekaDataStatsUtil.getDateAttributeNames(dataSet));
		
		final double[][] simMatrix=new double[numAttributes][numAttributes];
		for (int i=0;i<numAttributes;i++)
		{
			final double[] arrayI=dataSet.attributeToDoubleArray(i);
			if (this.withNormalization) MathsUtil.normalize(arrayI);
			simMatrix[i][i]=0d;;
			for (int j=i+1;j<numAttributes;j++)
			{				
				final double[] arrayJ=dataSet.attributeToDoubleArray(j);
				if (this.withNormalization) MathsUtil.normalize(arrayJ);
				simMatrix[i][j]=new DynamicTimeWarping(arrayI,arrayJ).getDistance();
				//System.out.println(i+" "+j);
			}

		}
		for (int i=0;i<numAttributes;i++)
		{
			for (int j=0;j<i+1;j++)
			{
				simMatrix[i][j]=simMatrix[j][i];
			}
		}		
		
		/*for (int i=0;i<numAttributes;i++)
		{
			System.out.println(i+" -> "+FormatterUtil.buildStringFromArrayOfDoubles(simMatrix[i]));
		}*/
		
		final ArrayList<Attribute> attrs=new ArrayList<Attribute>(numAttributes+1);		
		for (int i=0;i<numAttributes;i++)
		{
			attrs.add(new Attribute(dataSet.attribute(i).name()+"-feat"));
		}
		attrs.add(new Attribute(FEATUREDESC_ATTRNAME,namesOfFeaturesToConsider));
		final Instances ds=new Instances("featuresComparisonDs",attrs,0);
		ds.setClassIndex(attrs.size()-1);	
		
		for (int i=0;i<simMatrix.length;i++)
		{
			final DenseInstance di=new DenseInstance(1.0d,ArraysUtil.concat(simMatrix[i],new double[]{0d}));
			di.setDataset(ds);
			di.setValue(simMatrix.length,dataSet.attribute(i).name());
			ds.add(di);
		}
		return ds;
	}

	
	//
	// Inner enums
	//
	
	public static enum Mode {MDS,PCA};
}
