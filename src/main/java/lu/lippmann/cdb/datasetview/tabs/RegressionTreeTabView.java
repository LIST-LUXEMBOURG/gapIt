/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.dsl.ASCIIGraphDsl;
import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.dt.ui.DecisionTreeToGraphViewHelper;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.*;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.*;
import weka.core.*;


/**
 * RegressionTreeTabView.
 *
 * @author the WP1 team
 */
public final class RegressionTreeTabView extends AbstractTabView
{
	//
	// Instance fields
	//
	
	/** */
	private final ApplicationContext applicationContext;
	
	/** */
	private final EventPublisher eventPublisher;
	
	/** */
	private final CommandDispatcher commandDispatcher;
	
	/** FIXME: use dependency injection here, there are multiple implementations of graphDSL. */
	private final ASCIIGraphDsl graphDsl=new ASCIIGraphDsl();
	
	/** */
	private GraphView gv;	
	/** */
	private JXPanel panel;
		
	private boolean tweakedGraph;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public RegressionTreeTabView(final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher,final ApplicationContext applicationContext)
	{
		super();
		
		this.commandDispatcher=commandDispatcher;
		this.eventPublisher=eventPublisher;
		this.applicationContext=applicationContext;
		
		this.panel=new JXPanel();
		this.panel.setLayout(new BorderLayout());
				
		this.tweakedGraph = false;
	}
	
	
	//
	// Instance methods
	//
	
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
	public boolean isSlow() 
	{		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Regression tree";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void update0(final Instances dataSet) throws Exception 
	{	
		this.panel.removeAll();
		
		//final Object[] attrNames=WekaDataStatsUtil.getNumericAttributesNames(dataSet).toArray();
		final Object[] attrNames=WekaDataStatsUtil.getAttributeNames(dataSet).toArray();
		final JComboBox xCombo=new JComboBox(attrNames);
		xCombo.setBorder(new TitledBorder("Attribute to evaluate"));
		
		final JXPanel comboPanel=new JXPanel();
		comboPanel.setLayout(new GridLayout(1,2));
		comboPanel.add(xCombo);
		final JXButton jxb=new JXButton("Compute");
		comboPanel.add(jxb);
		this.panel.add(comboPanel,BorderLayout.NORTH);
		
		jxb.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					if (gv!=null) panel.remove((Component)gv);					
					
					dataSet.setClassIndex(xCombo.getSelectedIndex());
					
					final REPTree rt=new REPTree();
					rt.setNoPruning(true);
					//rt.setMaxDepth(3);
					rt.buildClassifier(dataSet);
					
					/*final M5P rt=new M5P();
					rt.buildClassifier(dataSet);*/
					
					final Evaluation eval=new Evaluation(dataSet);
					double[] d=eval.evaluateModel(rt,dataSet);			
					System.out.println("PREDICTED -> "+FormatterUtil.buildStringFromArrayOfDoubles(d));					
					System.out.println(eval.errorRate());
					System.out.println(eval.sizeOfPredictedRegions());					
					System.out.println(eval.toSummaryString("",true));
					
					final GraphWithOperations gwo=GraphUtil.buildGraphWithOperationsFromWekaRegressionString(rt.graph());			
					final DecisionTree dt=new DecisionTree(gwo,eval.errorRate());
					
					gv=DecisionTreeToGraphViewHelper.buildGraphView(dt,eventPublisher,commandDispatcher);
					gv.addMetaInfo("Size="+dt.getSize(),"");
					gv.addMetaInfo("Depth="+dt.getDepth(),"");
					
					gv.addMetaInfo("MAE="+FormatterUtil.DECIMAL_FORMAT.format(eval.meanAbsoluteError())+"","");
					gv.addMetaInfo("RMSE="+FormatterUtil.DECIMAL_FORMAT.format(eval.rootMeanSquaredError())+"","");
					
					final JCheckBox toggleDecisionTreeDetails = new JCheckBox("Toggle details");
					toggleDecisionTreeDetails.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(!tweakedGraph){
								final Object[] mapRep = WekaDataStatsUtil.buildNodeAndEdgeRepartitionMap(dt.getGraphWithOperations(),dataSet);
								gv.updateVertexShapeTransformer((Map<CNode,Map<Object,Integer>>)mapRep[0]);
								gv.updateEdgeShapeRenderer((Map<CEdge,Float>)mapRep[1]);
							}else{
								gv.resetVertexAndEdgeShape();
							}
							tweakedGraph= !tweakedGraph;
						}
					});
					gv.addMetaInfoComponent(toggleDecisionTreeDetails);
					
					/*final JButton openInEditorButton = new JButton("Open in editor");
					openInEditorButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							 GraphUtil.importDecisionTreeInEditor(dtFactory, dataSet, applicationContext, eventPublisher, commandDispatcher);
						}
					});
					this.gv.addMetaInfoComponent(openInEditorButton);*/
					
					final JButton showTextButton = new JButton("In text");
					showTextButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {				
							JOptionPane.showMessageDialog(null,graphDsl.getDslString(dt.getGraphWithOperations()));
						}
					});
					gv.addMetaInfoComponent(showTextButton);
					
					panel.add(gv.asComponent(),BorderLayout.CENTER);					
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
					panel.add(new JXLabel("Error during computation: "+e1.getMessage()),BorderLayout.CENTER);
				}
				
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/decision-tree.png");
	}


	
}
