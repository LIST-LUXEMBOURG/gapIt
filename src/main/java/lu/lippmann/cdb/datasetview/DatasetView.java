/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

import lu.lippmann.cdb.App;
import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.gui.slider.RangeSlider;
import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.datasetview.tabs.*;
import lu.lippmann.cdb.datasetview.tasks.*;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * DatasetView.
 * 
 * @author the WP1 team
 */
public final class DatasetView extends JXFrame implements Display, IDatasetView
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=123479L;
		
	/** */
	private static final String ALL_VAL="- ALL -";
	/** */
	private static final String NO_CLASS="NO_CLASS";


	//
	// Instance fields
	//

	/** */
	private final JXTaskPaneContainer taskPaneContainer; 

	/** */
	private Instances initialDataSet;
	/** */
	private CompletenessComputer initialCompleteness;
	
	/** */
	private Instances notFilteredDataSet;	
	/** */
	private Instances dataSet;
	
	/** */
	private boolean filtered=false;

	/** */
	private JTabbedPane tabbedPane;
	/** */
	private final Map<String,JTabbedPane> supertabsmap;

	
	/** */
	private JComboBox classSelectionCombo;
	/** */
	private ActionListener classSelectionComboListener;
	
	/** */
	private JXList historylist;
	/** */
	private DefaultListModel historyListModel;

	/** */
	private final EventPublisher eventPublisher;
	
	/** */
	private final List<TabView> tabViews;
	
	/** */
	private final JXPanel filterPanel;
	/** */
	private final JScrollPane scrollPane;
	/** */
	private final GridBagConstraints gbc;
	
	/** */
	private final JProgressBar dataCompletenessProgressBar;
	
	/** */
	private final JXTaskPane supervisedTransformPane;
	
	/** */
	private JCheckBox completnessCheckbox;
	
	/** */
	private JXPanel progressBarPanel;

	
	
	//
	// Constructors
	//

	/**
	 * Constructor.
	 * TODO: replace manual dependency injection by guice dependency injection!
	 */
	public DatasetView(final String title,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher,final ApplicationContext applicationContext)
	{
		super();

		this.setTitle(title);
		this.eventPublisher=eventPublisher;

		LogoHelper.setLogo(this);
		
		this.setLayout(new BorderLayout());
		
		this.taskPaneContainer=new JXTaskPaneContainer();	
		addClassSelectionMenu();
		addCompletnessActivatedCheckbox();
		addExportMenu();
		this.supervisedTransformPane=new JXTaskPane();
		addTransformSupervisedMenu();
		addTransformUnsupervisedMenu();
		addTimeSeriesMenu();
		addCorrectMenu();
		addMissingValuesMenu();
		addHistoMenu();
		this.add(taskPaneContainer,BorderLayout.WEST);
		
		this.filterPanel=new JXPanel();
		this.filterPanel.setLayout(new GridBagLayout());	
		this.filterPanel.setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
		this.scrollPane=new JScrollPane(this.filterPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);		
		this.scrollPane.setPreferredSize(new Dimension(200,2000));
		this.gbc=new GridBagConstraints();		 
		this.gbc.gridx=0;
		this.gbc.gridy=0;		 
		this.gbc.weightx = 1;
		this.gbc.weighty = 1;
		this.gbc.fill=GridBagConstraints.HORIZONTAL; 
		this.gbc.insets=new Insets(10,10,10,10);
		this.add(scrollPane,BorderLayout.EAST);
		
		this.tabbedPane=new JTabbedPane();
		this.add(this.tabbedPane,BorderLayout.CENTER);

		this.supertabsmap=new HashMap<String,JTabbedPane>();
		
		this.progressBarPanel=new JXPanel();
		this.progressBarPanel.setVisible(false);
		progressBarPanel.setBorder(new TitledBorder("Completeness"));
		progressBarPanel.setLayout(new BorderLayout());
		this.dataCompletenessProgressBar=new JProgressBar(0,100);		
		this.dataCompletenessProgressBar.setStringPainted(true);
		this.dataCompletenessProgressBar.updateUI();
		this.dataCompletenessProgressBar.setVisible(false);
		this.dataCompletenessProgressBar.setUI(new BasicProgressBarUI() 
		{
		      protected Color getSelectionBackground() { return Color.black; }
		      protected Color getSelectionForeground() { return Color.black; }
		});
		progressBarPanel.add(this.dataCompletenessProgressBar,BorderLayout.CENTER);
		this.add(progressBarPanel,BorderLayout.SOUTH);		
		
		this.tabViews=new ArrayList<TabView>();
		
		addTabView(new TableTabView(eventPublisher));
		
		addTabView(new DecisionTreeTabView(eventPublisher,commandDispatcher,applicationContext),"Decision trees");		
		addTabView(new WeightedMapOfDecisionTreesTabView(eventPublisher,commandDispatcher,applicationContext),"Decision trees");	
		addTabView(new RegressionTreeTabView(eventPublisher,commandDispatcher,applicationContext),"Decision trees");
		
		addTabView(new MDSTabView(),"Projections");	
		addTabView(new ScatterPlotTabView(),"Projections");
		addTabView(new RegressionTabView(),"Projections");
		
		addTabView(new StatsTabView(),"Stats");
		addTabView(new RulesTabView(),"Stats");
		addTabView(new PieChartTabView(),"Stats");
		addTabView(new AttributesSummaryTabView(),"Stats");
		addTabView(new SymmetricalUncertaintyCorrelationTabView(),"Stats");
		
		addTabView(new TimeSeriesTabView());
		
		addTabView(new UnsupervisedFeatureEvaluationTabView(),"Feature evaluation");
		
	}


	//
	// Instance methods
	//

	
	private void addTabView(final TabView tv,final String... supertabname)
	{		
		tv.setDataChangeListener(new Listener<TabView.DataChange>()
		{
			@Override
			public void onAction(final TabView.DataChange change) 
			{
				notifyTransformation(change.getDataSet(),change.getDataChangeTypeEnum()+" from "+tv.getName());
				if (isComputingOfDataCompletnessEnabled())
				{
					setDataCompleteness(initialCompleteness.computeUnchangedCellsCount(change.getDataSet()));
				}
			}
		});
		this.tabViews.add(tv);
		final JXPanel p=new JXPanel();
		p.setLayout(new BorderLayout());
		p.add(tv.getComponent(),BorderLayout.CENTER);
		p.add(tv.getErrorComponent(),BorderLayout.SOUTH);
		p.add(tv.getBusyComponent(),BorderLayout.NORTH);		
		
		if (supertabname.length>0)
		{
			JTabbedPane supertab;			
			if (!this.supertabsmap.containsKey(supertabname[0]))
			{
				supertab=new JTabbedPane();
				this.supertabsmap.put(supertabname[0], supertab);
				this.tabbedPane.addTab(supertabname[0],tv.getIcon(),supertab,supertabname[0]);
			}
			else
			{				
				supertab=this.supertabsmap.get(supertabname[0]);
			}			
			supertab.addTab(tv.getName(),tv.getIcon(),p,tv.getName());	
			tv.setLocation(supertab,supertab.indexOfTab(tv.getName()));
		}
		else
		{
			this.tabbedPane.addTab(tv.getName(),tv.getIcon(),p,tv.getName());	
			tv.setLocation(tabbedPane,tabbedPane.indexOfTab(tv.getName()));
		}
	}
	
	private void updateFiltersPane(final Instances instances) throws Exception
	{
		filterPanel.removeAll();
		gbc.gridx=0;
		gbc.gridy=0;
		
		final int numAttributes=instances.numAttributes();
		final RangeSlider[] rangeSliders=new RangeSlider[numAttributes]; 
		final JComboBox[] nominalCombos=new JComboBox[numAttributes]; 
		
		boolean hasNumeric=false;
		boolean hasDate=false;
		for (int i = 0 ; i < numAttributes ; i++)
		{
			final int attrIdx=i;
			if ((WekaDataStatsUtil.isInteger(instances,i))||instances.attribute(i).isDate())
			{
				hasNumeric=hasNumeric||(instances.attribute(i).isNumeric()&&!instances.attribute(i).isDate());
				hasDate=hasDate||instances.attribute(i).isDate();
				final long[] minmax=WekaDataStatsUtil.getMinMaxForAttribute(instances,i);
				if (Math.abs(minmax[1]-minmax[0])<0.00001) continue;
				
				if (instances.attribute(i).isDate())
				{
					minmax[0]=(int)(minmax[0]/(1000l));
					minmax[1]=(int)(minmax[1]/(1000l));
				}
				
				long[] oldminmax;
				try
				{
					final Attribute goodAttr=notFilteredDataSet.attribute(instances.attribute(i).name());
					oldminmax=WekaDataStatsUtil.getMinMaxForAttribute(notFilteredDataSet,goodAttr.index());
					if (instances.attribute(i).isDate())
					{
						oldminmax[0]=(int)(oldminmax[0]/(1000l));
						oldminmax[1]=(int)(oldminmax[1]/(1000l));
					}
				}
				catch(Throwable t)
				{
					oldminmax=minmax;
				}						

				rangeSliders[i]=new RangeSlider();
				rangeSliders[i].setMinimum((int)oldminmax[0]);
				rangeSliders[i].setMaximum((int)oldminmax[1]);

				rangeSliders[i].setValue((int)minmax[0]); 				
				rangeSliders[i].setUpperValue((int)minmax[1]);
				// 	hack...
				rangeSliders[i].setValue((int)minmax[0]); 				
				rangeSliders[i].setUpperValue((int)minmax[1]);       
				
				if (!instances.attribute(i).isDate())
				{
					rangeSliders[i].setPaintTicks(true);
					rangeSliders[i].setPaintLabels(true);
					final int rangeWidth=(int)(oldminmax[1]-oldminmax[0]);
					rangeSliders[i].setMinorTickSpacing(rangeWidth/10);
					rangeSliders[i].setMajorTickSpacing(rangeWidth/2);
				}
				
				rangeSliders[i].addChangeListener(new ChangeListener()
				{						
					@Override
					public void stateChanged(final ChangeEvent e) 
					{
						if (!rangeSliders[attrIdx].getValueIsAdjusting()) 
						{									
							processfilters(rangeSliders,nominalCombos,instances.attribute(attrIdx).name(),attrIdx);
						}
						else
						{
							if (instances.attribute(attrIdx).isDate())
							{
								final Calendar cal=Calendar.getInstance();
								cal.setTimeInMillis(rangeSliders[attrIdx].getValue()*1000l);						
								final String minDate=FormatterUtil.DATE_FORMAT.format(cal.getTime());
								cal.setTimeInMillis(rangeSliders[attrIdx].getUpperValue()*1000l);
								final String maxDate=FormatterUtil.DATE_FORMAT.format(cal.getTime());	
								rangeSliders[attrIdx].setBorder(new TitledBorder(instances.attribute(attrIdx).name()+" ["+minDate+" - "+maxDate+"]"));
							}
							else
							{
								rangeSliders[attrIdx].setBorder(new TitledBorder(instances.attribute(attrIdx).name()+" ["+rangeSliders[attrIdx].getValue()+" - "+rangeSliders[attrIdx].getUpperValue()+"]"));
							}
						}
					}
				});
				
				if (instances.attribute(i).isDate())
				{				
					filterPanel.add(rangeSliders[i],gbc);
					gbc.gridy++;					
					final Calendar cal=Calendar.getInstance();
					cal.setTimeInMillis(rangeSliders[i].getValue()*1000l);						
					final String minDate=FormatterUtil.DATE_FORMAT.format(cal.getTime());
					cal.setTimeInMillis(rangeSliders[i].getUpperValue()*1000l);
					final String maxDate=FormatterUtil.DATE_FORMAT.format(cal.getTime());					
					rangeSliders[i].setBorder(new TitledBorder(instances.attribute(i).name()+" ["+minDate+" - "+maxDate+"]"));
				}
				else
				{
					filterPanel.add(rangeSliders[i],gbc);
					gbc.gridy++;
					rangeSliders[i].setBorder(new TitledBorder(instances.attribute(i).name()+" ["+rangeSliders[i].getValue()+" - "+rangeSliders[i].getUpperValue()+"]"));					
				}
			}
			else
			{
				rangeSliders[i]=null;
			}
		}
		
		for (int i=0;i<numAttributes;i++)
		{
			if (instances.attribute(i).isNominal())
			{
				final ArrayList<String> possibleValuesList=new ArrayList<String>();				
				final Enumeration<?> es=notFilteredDataSet.attribute(notFilteredDataSet.attribute(instances.attribute(i).name()).index()).enumerateValues();
				possibleValuesList.add(ALL_VAL);
				while (es.hasMoreElements())
				{
					possibleValuesList.add(es.nextElement().toString().trim());
				}
				if (possibleValuesList.size()==2) continue; // only one choice, no filter needed!
				
				nominalCombos[i]=new JComboBox(possibleValuesList.toArray());				
				if (WekaDataStatsUtil.getPresentValuesForNominalAttribute(instances,i).size()==1)
				{
					nominalCombos[i].setSelectedItem(instances.instance(0).stringValue(i));
				}									
				nominalCombos[i].setBorder(new TitledBorder(instances.attribute(i).name()));
				final int nominalAttrIdx=i;
				nominalCombos[i].addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						processfilters(rangeSliders,nominalCombos,instances.attribute(nominalAttrIdx).name(),nominalAttrIdx);
					}
				});
				filterPanel.add(nominalCombos[i],gbc);
				gbc.gridy++;
			}
		}
				
		filterPanel.setVisible(gbc.gridy>0);
		filterPanel.updateUI();
		scrollPane.setVisible(gbc.gridy>0);
		scrollPane.updateUI();
	}
	
	private void processfilters(final RangeSlider[] rangeSliders,final JComboBox[] nominalCombos,final String name,final int idx)
	{
		Instances newds=new Instances(notFilteredDataSet);
		
		for (int k=0;k<nominalCombos.length;k++)
		{
			if (k==idx&&nominalCombos[k]!=null&&!nominalCombos[k].getSelectedItem().toString().equals(ALL_VAL))
			{
				newds=WekaDataProcessingUtil.filterDataSetOnNominalValue(newds,k,nominalCombos[k].getSelectedItem().toString());
				System.out.println("filter on '"+nominalCombos[k].getSelectedItem().toString()+"'");
			}
		}	
		for (int k=0;k<rangeSliders.length;k++)
		{
			if (k==idx&&rangeSliders[k]!=null)
			{
				if (newds.attribute(k).isDate())
				{
					newds=WekaDataProcessingUtil.filterDataSetOnNumericValue(newds,k,rangeSliders[k].getValue()*1000d,rangeSliders[k].getUpperValue()*1000d);
					System.out.println("filter on '"+k+"'");
				}
				else
				{
					newds=WekaDataProcessingUtil.filterDataSetOnNumericValue(newds,k,(double)rangeSliders[k].getValue(),(double)rangeSliders[k].getUpperValue());
					System.out.println("filter on '"+k+"'");
				}
			}
		}
					
		notifyFilter(newds,"Filtered on '"+name+"'");	
	}
		
	private void addTransformSupervisedMenu() 
	{	
		this.supervisedTransformPane.setTitle("Transform (superv.) ");						
		this.supervisedTransformPane.add(new HierarchizeTask().buildAction(this));		
		this.supervisedTransformPane.add(new SupervisedFeatureSelectionTask().buildAction(this));
		this.supervisedTransformPane.add(new SupervisedFeatureSelectionWithDecisionTreeTask().buildAction(this));
		this.supervisedTransformPane.add(new SupervisedDiscretizeTask().buildAction(this));
		this.supervisedTransformPane.add(new SupervisedMergeValuesOfFirstNominalTask().buildAction(this));
		taskPaneContainer.add(this.supervisedTransformPane);
	}
	
	private void addTransformUnsupervisedMenu() 
	{	
		final JXTaskPane unsupervisedTransformPane=new JXTaskPane();
		unsupervisedTransformPane.setTitle("Transform (unsuperv.)");						
		unsupervisedTransformPane.add(new NominalizeTask().buildAction(this));
		unsupervisedTransformPane.add(new UnsupervisedDiscretizeTask().buildAction(this));
		unsupervisedTransformPane.add(new NumerizeTask().buildAction(this));
		unsupervisedTransformPane.add(new TransposeTask().buildAction(this));
		unsupervisedTransformPane.add(new CompareAttributesTask().buildAction(this));
		unsupervisedTransformPane.add(new SortAttributesTask().buildAction(this));
		unsupervisedTransformPane.add(new SortInstancesTask().buildAction(this));
		unsupervisedTransformPane.add(new UnsupervisedFeatureSelectionTask().buildAction(this));
		unsupervisedTransformPane.add(new DuplicateAttributeTask().buildAction(this));
		unsupervisedTransformPane.add(new AddCentroidDistanceTask().buildAction(this));
		unsupervisedTransformPane.add(new TransformFirstNominalAsBinariesTask().buildAction(this));
		unsupervisedTransformPane.add(new UnsupervisedMergeValuesOfFirstNominalTask().buildAction(this));
		unsupervisedTransformPane.add(new SetAttributeAsTimestampTask().buildAction(this));
		taskPaneContainer.add(unsupervisedTransformPane);
	}

	private void addTimeSeriesMenu() 
	{	
		final JXTaskPane timeSeriesPane=new JXTaskPane();
		timeSeriesPane.setTitle("Time series");				
		timeSeriesPane.add(new AggregateTimeSeriesTask().buildAction(this));
		timeSeriesPane.add(new SortTimeSeriesTask().buildAction(this));
		timeSeriesPane.add(new AddDiscretizedTimeTask().buildAction(this));
		timeSeriesPane.add(new AddFakeTimeTask().buildAction(this));
		timeSeriesPane.add(new FillShortGapsInTimeSeriesByInterpolationTask().buildAction(this));
		timeSeriesPane.add(new FillAllGapsInTimeSeriesWithEMTask().buildAction(this));
		timeSeriesPane.add(new RemoveFirstGapInTimeSeriesTask().buildAction(this));
		taskPaneContainer.add(timeSeriesPane);
	}
	
	private void addCorrectMenu() 
	{	
		final JXTaskPane correctPane=new JXTaskPane();
		correctPane.setTitle("Correct");		
		correctPane.add(new RemoveExtremeValuesTask().buildAction(this));	
		correctPane.add(new RemoveOutliersTask().buildAction(this));			
		correctPane.add(new RemoveDuplicatesTask().buildAction(this));
		correctPane.add(new RemoveConstantAttributesTask().buildAction(this));
		correctPane.add(new MarkExtremeValuesAndOutliersTask().buildAction(this));
		taskPaneContainer.add(correctPane);
	}

	private void addMissingValuesMenu() 
	{
		final JXTaskPane missingValuesPane=new JXTaskPane();
		missingValuesPane.setTitle("Missing values");		
		missingValuesPane.add(new ReplaceMissingValuesTask().buildAction(this));			
		missingValuesPane.add(new ReplaceMissingValuesUsingEMTask().buildAction(this));		
		missingValuesPane.add(new RemoveRowsWithMissingValuesTask().buildAction(this));
		missingValuesPane.add(new RemoveAttrWithMissingValuesTask().buildAction(this));
		missingValuesPane.add(new MarkMissingValuesTask().buildAction(this));
		taskPaneContainer.add(missingValuesPane);
	}	
	
	private void addHistoMenu() 
	{		
		final JXTaskPane histoPane=new JXTaskPane();
		histoPane.setTitle("History");
		this.historyListModel=new DefaultListModel();
		final JXPanel tp=new JXPanel();
		tp.setBorder(new TitledBorder("Changes"));
		this.historylist=new JXList(historyListModel);
		this.historylist.setLayoutOrientation(JList.VERTICAL);
		final JScrollPane listScroller=new JScrollPane(this.historylist);
		listScroller.setPreferredSize(new Dimension(150,75));
		tp.add(listScroller);
		histoPane.add(tp);		
		histoPane.add(new CancelTask().buildAction(this));		
		taskPaneContainer.add(histoPane);
	}

	private final void addClassSelectionMenu()
	{
		final JXTaskPane classSelectionPane=new JXTaskPane();
		classSelectionPane.setTitle("Class selection");
		this.classSelectionCombo=new JComboBox();
		classSelectionPane.add(this.classSelectionCombo);
		this.classSelectionComboListener=new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				final String s=classSelectionCombo.getSelectedItem().toString();
				if (s.equals(NO_CLASS)) dataSet.setClassIndex(-1); 
				else dataSet.setClass(dataSet.attribute(s));
				setDataSet(dataSet);				
			}
		};		
		taskPaneContainer.add(classSelectionPane);				
	}
	
	private final void addCompletnessActivatedCheckbox()
	{
		final JXTaskPane completnessSelectionPane=new JXTaskPane();
		completnessSelectionPane.setTitle("Data completness");
		this.completnessCheckbox=new JCheckBox("Enable computation");
		completnessSelectionPane.add(this.completnessCheckbox);
		this.completnessCheckbox.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				final boolean b = completnessCheckbox.isSelected();
				if (b) setDataCompleteness(initialCompleteness.computeUnchangedCellsCount(dataSet));
				progressBarPanel.setVisible(b);
				dataCompletenessProgressBar.setVisible(b);				
			}
		});
		taskPaneContainer.add(completnessSelectionPane);		
	}
	
	private final void updateClassSelectionMenu()
	{
		if (this.classSelectionCombo.getActionListeners().length>0) 
		{	
			this.classSelectionCombo.removeActionListener(classSelectionComboListener);
		}
		
		this.classSelectionCombo.removeAllItems();
		this.classSelectionCombo.addItem(NO_CLASS);
		for (final Attribute attr:WekaDataStatsUtil.getNominalAttributesList(dataSet))
		{
			this.classSelectionCombo.addItem(attr.name());
		}
		if (dataSet.classIndex()!=-1)
		{
			this.classSelectionCombo.setSelectedItem(dataSet.classAttribute().name());
		}
		else
		{
			this.classSelectionCombo.setSelectedItem(NO_CLASS);
		}
		
		this.classSelectionCombo.addActionListener(classSelectionComboListener);
	}
	
	private final void addExportMenu() 
	{
		final JXTaskPane exportPane=new JXTaskPane();
		exportPane.setTitle("Export");
		exportPane.add(new ExportARFFTask().buildAction(this));			
		exportPane.add(new ExportCSVTask().buildAction(this));
		exportPane.add(new ExportCallunaTask().buildAction(this));		
		taskPaneContainer.add(exportPane);
	}
	
	@Override
	public final void notifyTransformation(final Instances newdataset,final String msg)
	{
		this.filtered=false;
		if (newdataset!=null)
		{			
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run() 
				{
					setDataSet(newdataset).setAsVisible();		
					historyListModel.addElement(msg);
					updateHistoryList();
				}
			});			
		}
		else
		{			
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run() 
				{					
					setDataSet(initialDataSet).setAsVisible();
					historyListModel.removeAllElements();
				}
			});						
		}
	}

	public final void notifyFilter(final Instances newdataset,final String msg)
	{
		this.filtered=true;
		setDataSet(newdataset).setAsVisible();
		this.historyListModel.addElement(msg);
		updateHistoryList();
		if (isComputingOfDataCompletnessEnabled())
		{
			setDataCompleteness(initialCompleteness.computeUnchangedCellsCount(newdataset));
		}
	}
	
	private void updateHistoryList()
	{
		this.historylist.setSelectedIndex(this.historyListModel.size()-1);
		this.historylist.ensureIndexIsVisible(this.historylist.getSelectedIndex());
		this.historylist.clearSelection();
	}
	
	public final void setAsVisible(boolean setSize) 
	{
		if (setSize)
		{			
			this.setPreferredSize(new Dimension(1200,950));
			this.pack();
		}
		this.setVisible(true);
	}

	public final void setAsVisible()
	{
		setAsVisible(false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Component asComponent()
	{
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Instances getInitialDataSet() 
	{
		return this.initialDataSet;
	}

	public final CompletenessComputer getInitialCompleteness() 
	{
		return this.initialCompleteness;
	}
	
	public DatasetView setDataSet(final Instances pdataSet)
	{
		if (pdataSet.classIndex()!=-1&&!pdataSet.classAttribute().isNominal()) pdataSet.setClassIndex(-1);
		
		if (this.initialDataSet==null) 
		{	
			this.initialDataSet=pdataSet;
			this.initialCompleteness=new CompletenessComputer(this.initialDataSet);
			this.dataCompletenessProgressBar.setMaximum(pdataSet.numInstances()*pdataSet.numAttributes());
			reinitDataCompleteness();
		}
		
		this.dataSet=pdataSet;

		if (!filtered) this.notFilteredDataSet=pdataSet;
		
		updateClassSelectionMenu();
		this.supervisedTransformPane.setVisible(pdataSet.classIndex()!=-1);
		
		for (final TabView tv:tabViews)
		{			
			tv.update(dataSet);
		}
		
		try
		{		
			updateFiltersPane(dataSet);
		}
		catch(Exception e)
		{
			eventPublisher.publish(new ErrorOccuredEvent("Error when updating filters",e));
		}
				
		updateTooltipShowingDatasetDimensions();
		
		return this;
	}
	
	private void updateTooltipShowingDatasetDimensions()
	{
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body>");
		sb.append("<b>Original dataset:</b> ").append(this.initialDataSet.numInstances()).append(" rows, ").append(this.initialDataSet.numAttributes()).append(" columns").append("<br/>");		
		sb.append("<b>Current dataset:</b> ").append(this.dataSet.numInstances()).append(" rows, ").append(this.dataSet.numAttributes()).append(" columns").append("<br/>");	
		sb.append("</body></html>");
		this.dataCompletenessProgressBar.setToolTipText(sb.toString());
		//this.dataCompletenessProgressBar.updateUI();
		System.out.println(getDataCompleteness());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataCompleteness(final int completeness) 
	{
		this.dataCompletenessProgressBar.setValue(completeness);
		
		final int ratio=getDataCompletenessRatio();
		if (ratio>90) this.dataCompletenessProgressBar.setForeground(Color.GREEN);
		else if (ratio>60) this.dataCompletenessProgressBar.setForeground(Color.ORANGE);
		else this.dataCompletenessProgressBar.setForeground(Color.RED);	
	}
	
	public void reinitDataCompleteness()
	{
		setDataCompleteness(this.initialDataSet.numInstances()*this.initialDataSet.numAttributes());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDataCompleteness()
	{
		return this.dataCompletenessProgressBar.getValue();
	}
	
	public int getDataCompletenessRatio()
	{
		final int ratio=(int)Math.round((100d*this.dataCompletenessProgressBar.getValue())/(double)this.dataCompletenessProgressBar.getMaximum());
		return ratio;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventPublisher getEventPublisher() 
	{		
		return eventPublisher;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instances getDataSet() 
	{
		return dataSet;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isComputingOfDataCompletnessEnabled()
	{
		return completnessCheckbox.isSelected();
	}
	
}
