/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.common.ArraysUtil;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.lab.mds.*;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import weka.clusterers.*;
import weka.core.*;


/**
 * UnsupervisedFeatureEvaluationTabView.
 *
 * @author the WP1 team
 */
public final class UnsupervisedFeatureEvaluationTabView extends AbstractTabView
{
	//
	// Static fields
	//

	/** */
	private static final String FEATUREDESC_ATTRNAME="FEATUREDESC_ATTRNAME";

	
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	
	/** */
	private JXPanel mdsView;
	
	/** */
	private final JSlider slider;
	/** */
	private ChangeListener cl;
	
	/** */
	private JXComboBox combo;
	/** */
	private ActionListener al;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public UnsupervisedFeatureEvaluationTabView()
	{
		super();		
		
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	
		
		this.slider=new JSlider();
		this.slider.setBorder(new TitledBorder("Count of features to highlight"));
		this.slider.setOpaque(false);
		this.slider.setMaximum(10);
		this.slider.setValue(2);
		this.slider.setMinimum(1);
		this.slider.setMinorTickSpacing(1);
		this.slider.setMajorTickSpacing(3);
		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);
		this.slider.setSnapToTicks(true);		
		this.jxp.add(this.slider,BorderLayout.SOUTH);	
		
		this.combo=new JXComboBox(new Object[]{"Show clusters of features","Show important features"});
		this.combo.setBorder(new TitledBorder("Mode"));
		this.jxp.add(this.combo,BorderLayout.NORTH);	
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSlow() 
	{		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Unsupervised feature evaluation";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return this.jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{
		Instances preparedDataSet=new Instances(dataSet);
		preparedDataSet.setClassIndex(-1);
		
		preparedDataSet=WekaTimeSeriesUtil.buildDataSetWithoutDates(preparedDataSet);		
		preparedDataSet=WekaDataProcessingUtil.buildNumerizedDataSet(preparedDataSet);
		preparedDataSet=WekaDataProcessingUtil.buildNormalizedDataSet(preparedDataSet);		
		
		final Instances newds;
		if (this.combo.getSelectedIndex()==1)
		{			
			newds=buildDerivatedDatasetForBestFeatures(preparedDataSet,this.slider.getValue());
		}
		else
		{
			newds=buildDerivatedDatasetForFeaturesClusters(preparedDataSet,this.slider.getValue());
		}
		
		if (this.mdsView!=null) this.jxp.remove(this.mdsView);
		
		final int limitForCollapsing=1000;
		final boolean normalized = false; //TODO checkbox
		final MDSResult mdsResult=ClassicMDS.doMDS(newds,MDSDistancesEnum.EUCLIDEAN,2,limitForCollapsing,true,normalized);		
		this.mdsView=MDSViewBuilder.buildMDSViewFromDataSet(newds,mdsResult,limitForCollapsing,new Listener<Instances>()
		{
			@Override
			public void onAction(final Instances parameter) {}
		},FEATUREDESC_ATTRNAME);
				
		this.jxp.add(this.mdsView,BorderLayout.CENTER);
		
		if (this.cl!=null) this.slider.removeChangeListener(this.cl);		
		this.cl=new ChangeListener()
		{						
			@Override
			public void stateChanged(final ChangeEvent e) 
			{
				if (!slider.getValueIsAdjusting()) 
				{												
					try 
					{
						update0(dataSet);
					} 
					catch (Exception e1) 
					{						
						e1.printStackTrace();
					}
				}
			}
		};
		this.slider.addChangeListener(this.cl);
		
		if (this.al!=null) this.combo.removeActionListener(this.al);
		this.al=new ActionListener()
		{						
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					update0(dataSet);
				} 
				catch (Exception e1) 
				{						
					e1.printStackTrace();
				}			
			}
		};
		this.combo.addActionListener(this.al);
		
		this.jxp.updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/unsupervised-feature-evaluation.png");
	}
	
	
	//
	// Static methods
	//
	
	private static Instances buildDerivatedDatasetForFeaturesClusters(final Instances dataSet,final int k) throws Exception 
	{		
		final Instances trdataSet=WekaDataProcessingUtil.buildTransposedDataSet(dataSet);		
		
		final EuclideanDistance distanceFunction=new EuclideanDistance(trdataSet);
		
		final SimpleKMeans skm=WekaMachineLearningUtil.buildSimpleKMeansClustererWithK(k,distanceFunction);			
		skm.buildClusterer(trdataSet);
		final ClusterEvaluation eval=new ClusterEvaluation();
		eval.setClusterer(skm);
		eval.evaluateClusterer(trdataSet);	
		
		final int numClusters=eval.getNumClusters();
		final List<String> possibleValues=new ArrayList<String>(numClusters);
		for (int c=0;c<numClusters;c++) possibleValues.add("cluster_"+c);				
		
		final double[] clusterAssignments=eval.getClusterAssignments();
				
	    final int numAttributes=dataSet.numAttributes();
	    final List<Integer> valueForEachFeature=new ArrayList<Integer>(numAttributes);
		for (int j=0;j<numAttributes;j++)
		{		
			//System.out.println(clusterAssignments[j]+" "+(int)clusterAssignments[j]);
			valueForEachFeature.add((int)clusterAssignments[j]);
		}
		
		return buildDerivatedDataset(dataSet,possibleValues,valueForEachFeature);
	}
	
	private static Instances buildDerivatedDatasetForBestFeatures(final Instances dataSet,final int k) throws Exception 
	{
		final List<String> possibleValues=Arrays.asList("normal feature","important feature");
		
		final List<Integer> l=WekaMachineLearningUtil.computeUnsupervisedFeaturesSelection(dataSet,k);
		final int[] majorFeaturesIndexes=ArraysUtil.transform(l);
		
		final List<Integer> valueForEachFeature=new ArrayList<Integer>();
	    final int numAttributes=dataSet.numAttributes();
		for (int j=0;j<numAttributes;j++)
		{	
			final boolean isMajorFeature=ArraysUtil.contains(majorFeaturesIndexes,j);
			valueForEachFeature.add(isMajorFeature?1:0);
		}
		
		return buildDerivatedDataset(dataSet,possibleValues,valueForEachFeature);
	}	
	
	private static Instances buildDerivatedDataset(final Instances dataSet,final List<String> possibleValues,final List<Integer> valueForEachFeature) throws Exception 
	{
		final int numInstances=dataSet.numInstances();
		final ArrayList<Attribute> attrs=new ArrayList<Attribute>(numInstances+2);
		attrs.add(new Attribute(FEATUREDESC_ATTRNAME,(java.util.List<String>)null));
		for (int i=0;i<numInstances;i++)
		{
			attrs.add(new Attribute(i+"_eval"));
		}
		attrs.add(new Attribute("__",possibleValues));
		
	    final Instances newds=new Instances("unsupervisedFeaturesEval",attrs,0);	    
	    final int numAttributes=dataSet.numAttributes();
		for (int j=0;j<numAttributes;j++)
		{						
			double[] val=ArraysUtil.concat(dataSet.attributeToDoubleArray(j),new double[]{0.0d});
			val=ArraysUtil.concat(new double[]{0.0d},val);
			newds.add(new DenseInstance(1.0d,val));
		}		
		for (int j=0;j<numAttributes;j++)
		{						
			newds.instance(j).setValue(0,dataSet.attribute(j).name());
			newds.instance(j).setValue(numInstances+1,possibleValues.get(valueForEachFeature.get(j)));
		}
		newds.setClassIndex(numInstances+1);
		return newds;
	}
}
