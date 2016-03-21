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

import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.lab.beta.*;
import lu.lippmann.cdb.lab.beta.shih.Shih2010;
import lu.lippmann.cdb.lab.kmeans.KmeansImproved;
import lu.lippmann.cdb.lab.mds.*;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;


/**
 * MDSTabView.
 *
 * @author the WP1 team
 */
public final class MDSTabView extends AbstractTabView
{
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	/** */
	private final JXComboBox distCombo;
	/** */
	private ActionListener distComboListener;
	/** */
	private ActionListener shihListener;
	private ActionListener ignoreListener;
	private ActionListener normalizeListener;
	private ActionListener distanceParametersListener;
	/** */
	private KeyListener    maxInstancesListener;


	/** */
	private JTextArea maxInstances;
	/** */
	private JCheckBox shihCheckbox;
	private JCheckBox ignoreClassCheckbox;
	private JCheckBox normalizeCheckbox;
	/** */
	private Object currentDist;
	private String currentParameter;
	private JTextField distanceParameters;
	private JLabel distanceParametersLabel;

	private JTextField maxKField;
	private JButton kmeansButton;




	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public MDSTabView()
	{
		super();

		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	

		this.distCombo=new JXComboBox(MDSDistancesEnum.values());
		this.currentDist=this.distCombo.getSelectedItem();
		this.distCombo.setBorder(new TitledBorder("Distance"));

		this.maxInstances = new JTextArea("Max instances");
		this.maxInstances.setText(""+MDSViewBuilder.DEFAULT_MAX_INSTANCES);
		this.maxInstances.setToolTipText("If more instances are provided, k-means with the provided value will be performed");

		this.shihCheckbox = new JCheckBox("Use shih numerization");
		this.shihCheckbox.setToolTipText("If checked, will use Shih method instead of default Weka's one");

		this.ignoreClassCheckbox = new JCheckBox("Ignore class");
		this.ignoreClassCheckbox.setToolTipText("If checked, won't take into account the class attribute in the distance");
		this.ignoreClassCheckbox.setSelected(true);

		this.normalizeCheckbox = new JCheckBox("Normalize result");
		this.normalizeCheckbox.setToolTipText("If checked, the MDS result will be normalized");
		this.normalizeCheckbox.setSelected(false);

		this.distanceParameters 	 = new JTextField("2.0");
		this.distanceParameters.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				int indexOf = distanceParameters.getText().indexOf(".");
				char c = e.getKeyChar();
				if(c =='.'){
					if(indexOf != -1) e.consume();
				}else
					if ( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE) ) {
						e.consume();
					}
			}
		});

		this.distanceParametersLabel = new JLabel("Parameter:");
		this.currentParameter="2.0";

		distanceParameters.setVisible(false);
		distanceParametersLabel.setVisible(false);
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
		return "MDS";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{
		this.jxp.removeAll();

		if (this.distComboListener!=null) distCombo.removeActionListener(this.distComboListener);
		this.distComboListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (!currentDist.equals(distCombo.getSelectedItem())) update(dataSet);
				currentDist=distCombo.getSelectedItem();

				final MDSDistancesEnum mde=MDSDistancesEnum.valueOf(currentDist.toString());
				boolean showDistanceParameters=(mde.equals(MDSDistancesEnum.MINKOWSKI));
				distanceParameters.setVisible(showDistanceParameters);
				distanceParametersLabel.setVisible(showDistanceParameters);
			}
		};
		this.distCombo.addActionListener(this.distComboListener);

		if (this.distanceParametersListener!=null) distanceParameters.removeActionListener(this.distanceParametersListener);
		this.distanceParameters.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!currentParameter.equals(distanceParameters.getText())) update(dataSet);
				currentParameter=distanceParameters.getText();
			}
		});
		this.distanceParameters.addActionListener(this.distanceParametersListener);


		if (this.shihListener!=null) shihCheckbox.removeActionListener(this.shihListener);
		this.shihListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update(dataSet);
			}
		};
		this.shihCheckbox.addActionListener(this.shihListener);
		this.shihCheckbox.setEnabled(!WekaDataStatsUtil.areAllAttributesNominal(dataSet));


		if (this.ignoreListener!=null) ignoreClassCheckbox.removeActionListener(this.ignoreListener);
		this.ignoreListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update(dataSet);
			}
		};
		this.ignoreClassCheckbox.addActionListener(this.ignoreListener);
		this.ignoreClassCheckbox.setEnabled(dataSet.classIndex()!=-1);

		if (this.maxInstancesListener!=null) maxInstances.removeKeyListener(this.maxInstancesListener);
		this.maxInstancesListener=new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				final int cCode = e.getKeyCode();
				if(cCode == KeyEvent.VK_ENTER){
					update(dataSet);	
					e.consume();
				}
			}
		};
		this.maxInstances.addKeyListener(maxInstancesListener);

		if (this.normalizeListener!=null) normalizeCheckbox.removeActionListener(this.normalizeListener);
		this.normalizeListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				update(dataSet);
			}
		};
		this.normalizeCheckbox.addActionListener(this.normalizeListener);

		//TODO : use proper layout ...
		final JXPanel northPanel = new JXPanel();
		northPanel.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=0;
		gbc.gridwidth=2;
		gbc.weightx=1;
		gbc.fill=GridBagConstraints.BOTH;
		northPanel.add(this.distCombo,gbc);
		gbc.weightx=0;
		gbc.gridwidth=1;
		gbc.gridy=1;
		northPanel.add(this.distanceParametersLabel,gbc);
		gbc.gridx=1;
		northPanel.add(this.distanceParameters,gbc);

		this.jxp.add(northPanel,BorderLayout.NORTH);

		final MDSDistancesEnum mde=MDSDistancesEnum.valueOf(distCombo.getSelectedItem().toString());
		final String strOrder=distanceParameters.getText();
		if(mde.equals(MDSDistancesEnum.MINKOWSKI)){
			mde.setParameters(new String[]{strOrder});
		}
		Instances usedDataSet = dataSet;
		if(shihCheckbox.isSelected()){
			//Modify instance using SHIH Algorithm
			final Shih2010 shih = new Shih2010(dataSet);
			usedDataSet = shih.getModifiedInstances();
		}

		this.kmeansButton = new JButton("K-means");
		this.maxKField	  = new JTextField("10");

		//Create whole panel
		final JXPanel southPanel = new JXPanel();
		southPanel.add(shihCheckbox);
		southPanel.add(ignoreClassCheckbox);
		southPanel.add(normalizeCheckbox);
		southPanel.add(maxInstances);
		southPanel.add(new JLabel("Maximum K"));
		southPanel.add(maxKField);
		southPanel.add(kmeansButton);
		this.jxp.add(southPanel,BorderLayout.SOUTH);

		
		//Compute MDS
		final MDSResult mdsResult = ClassicMDS.doMDS(usedDataSet,mde,2,Integer.valueOf(maxInstances.getText()),ignoreClassCheckbox.isSelected(),normalizeCheckbox.isSelected());

		final JXPanel mdsView=MDSViewBuilder.buildMDSViewFromDataSet(
				dataSet,mdsResult,Integer.valueOf(maxInstances.getText())
				,new Listener<Instances>()
				{
					@Override
					public void onAction(final Instances parameter) 
					{
						pushDataChange(new DataChange(parameter,TabView.DataChangeTypeEnum.Selection));
					}
				});
		this.jxp.add(mdsView,BorderLayout.CENTER);

		this.kmeansButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					//List of coordinates (x,y) of collapsed instances
					final Instances coordsInstances = mdsResult.buildInstancesFromMatrix();
					//FIXME dangerous : K-means on ordered collapsedInstance coordinates
					final KmeansImproved km = new KmeansImproved(coordsInstances,Integer.valueOf(maxKField.getText()));
					final double[] ass = km.getClusteredInstances();
					int usedK 		   = km.getUsedKmeans().getNumClusters();
					final StringBuilder labels = new StringBuilder();
					for(int i = 0 ; i < usedK ; i++){
						labels.append("cluster").append((i+1));
						if(i < usedK- 1) labels.append(",");
					}

					//Build modified dataset
					String attributeName = "cluster_proj";
					while (dataSet.attribute(attributeName)!=null) attributeName+="_proj"; 
					final Add addFilter = new Add();
					addFilter.setAttributeIndex("last");
					addFilter.setAttributeName(attributeName);
					addFilter.setNominalLabels(labels.toString());
					addFilter.setInputFormat(dataSet);
					final Instances modDataset = Filter.useFilter(dataSet,addFilter);
					final int nbInstances  = modDataset.numInstances();
					final int nbAttributes = modDataset.numAttributes();

					if(mdsResult.getCInstances().isCollapsed()){
						//
						final KmeansResult kmr = mdsResult.getCInstances().getCentroidMap();
						final List<Instances> clusters = kmr.getClusters();
						int nbClusters = clusters.size();

						//Build a map between any instance and it's cluster's centroid
						final Map<ComparableInstance,Integer> mapCentroid = new HashMap<ComparableInstance,Integer>();
						for(int i = 0 ; i < nbClusters ; i++){
							final Instances cluster  = clusters.get(i);
							final int clusterSize    = cluster.size(); 
							for(int k = 0 ; k < clusterSize ; k++){
								mapCentroid.put(new ComparableInstance(cluster.instance(k)),i);
							}
						}

						//Use the previous map to add the additionnal feature for every element !
						for(int i = 0 ; i < nbInstances ; i++){
							final int centroidIndex = mapCentroid.get(new ComparableInstance(dataSet.instance(i)));
							final String value = "cluster"+(int)(ass[centroidIndex]+1);
							modDataset.instance(i).setValue(nbAttributes-1,value);	
						}
					}else{
						for(int i = 0 ; i < nbInstances ; i++){
							final String value = "cluster"+(int)(ass[i]+1);
							modDataset.instance(i).setValue(nbAttributes-1,value);	
						}
					}
					pushDataChange(new DataChange(modDataset,TabView.DataChangeTypeEnum.Update));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});


		this.jxp.repaint();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/mds.png");
	}
}
