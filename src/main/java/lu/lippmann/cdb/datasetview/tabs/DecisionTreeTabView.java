/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.context.*;
import lu.lippmann.cdb.dsl.ASCIIGraphDsl;
import lu.lippmann.cdb.dt.*;
import lu.lippmann.cdb.dt.ui.DecisionTreeToGraphViewHelper;
import lu.lippmann.cdb.dt.weka.J48DecisionTreeFactory;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.JXPanel;
import weka.core.Instances;


/**
 * DecisionTreeTabView.
 *
 * @author the WP1 team
 */
public final class DecisionTreeTabView extends AbstractTabView
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
	private DecisionTreeFactory dtFactory;
	/** */
	private GraphView gv;	
	/** */
	private JXPanel panel;
	
	/** */
	private final JSlider slider;
	/** */
	private ChangeListener cl;
	
	private boolean tweakedGraph;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public DecisionTreeTabView(final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher,final ApplicationContext applicationContext)
	{
		super();
		
		this.commandDispatcher=commandDispatcher;
		this.eventPublisher=eventPublisher;
		this.applicationContext=applicationContext;
		
		final double defaultValueConfidenceFactor=DecisionTreeHelper.HIGH_CONFIDENCE_FACTOR;
		this.dtFactory=new J48DecisionTreeFactory(defaultValueConfidenceFactor,false);
		this.panel=new JXPanel();
		this.panel.setLayout(new BorderLayout());
		
		this.slider=new JSlider();
		this.slider.setBorder(new TitledBorder("Confidence factor"));
		this.slider.setOpaque(false);
		this.slider.setMaximum((int)(defaultValueConfidenceFactor*100));
		this.slider.setValue((int)(defaultValueConfidenceFactor*100));
		this.slider.setMinimum(1);
		this.slider.setMinorTickSpacing(1);
		this.slider.setMajorTickSpacing(10);
		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);		
		
		this.panel.add(this.slider,BorderLayout.SOUTH);
		
		this.tweakedGraph = false;
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
	public String getName() 
	{
		return "Decision tree";
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
		if (this.gv!=null) this.panel.remove(this.gv.asComponent());	
		
		if (this.cl!=null) this.slider.removeChangeListener(cl);
		
		this.cl=new ChangeListener()
		{						
			@Override
			public void stateChanged(final ChangeEvent e) 
			{
				if (!slider.getValueIsAdjusting()) 
				{												
					dtFactory=new J48DecisionTreeFactory(slider.getValue()/100d,false);
					update(dataSet);
				}
			}
		};
		this.slider.addChangeListener(cl);

		final DecisionTree dt=dtFactory.buildDecisionTree(dataSet);
		
		this.gv=DecisionTreeToGraphViewHelper.buildGraphView(dt,eventPublisher,commandDispatcher);
		this.gv.addMetaInfo("Size="+dt.getSize(),"");
		this.gv.addMetaInfo("Depth="+dt.getDepth(),"");
		this.gv.addMetaInfo("Error-rate="+FormatterUtil.DECIMAL_FORMAT.format(100d*dt.getErrorRate())+"%","");
		
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
		this.gv.addMetaInfoComponent(toggleDecisionTreeDetails);
		
		final JButton openInEditorButton = new JButton("Open in editor");
		openInEditorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 GraphUtil.importDecisionTreeInEditor(dtFactory, dataSet, applicationContext, eventPublisher, commandDispatcher);
			}
		});
		this.gv.addMetaInfoComponent(openInEditorButton);
		
		final JButton showTextButton = new JButton("In text");
		showTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				JOptionPane.showMessageDialog(null,graphDsl.getDslString(dt.getGraphWithOperations()));
			}
		});
		this.gv.addMetaInfoComponent(showTextButton);
		
		this.panel.add(this.gv.asComponent(),BorderLayout.CENTER);

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
