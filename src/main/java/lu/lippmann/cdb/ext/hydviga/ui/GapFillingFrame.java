/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.common.*;
import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.common.gui.*;
import lu.lippmann.cdb.common.gui.ts.TimeSeriesChartUtil;
import lu.lippmann.cdb.datasetview.tabs.*;
import lu.lippmann.cdb.datasetview.tabs.TabView.*;
import lu.lippmann.cdb.ext.hydviga.cbr.GapFillingCase;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.gaps.*;
import lu.lippmann.cdb.ext.hydviga.gaps.GapFillerFactory.Algo;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;
import lu.lippmann.cdb.lab.timeseries.*;
import lu.lippmann.cdb.main.MainViewLoadingFrame;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.ui.*;

import weka.core.*;


/**
 * GapFillingFrame.
 * 
 * @author Olivier PARISOT
 */
public final class GapFillingFrame extends JXFrame
{
	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=1234L;

	/** */
	private static final boolean DEFAULT_HIDE_OTHER_SERIES_OPTION=true;
	/** */
	private static final boolean DEFAULT_ZOOM_OPTION=true;

	/** */
	//private static final int VALUES_BEFORE_AND_AFTER_FOR_MAE=500;
	
	
	//
	// Instance fields
	//	

	/** */
	private final AbstractTabView atv;
	
	/** */
	private final Instances dataSet;
	/** */
	private final Attribute attr;
	/** */
	private final int dateIdx;
	/** */
	private int valuesBeforeAndAfter;
	/** */
	private int position;
	/** */
	private int gapsize;
	
	//private final Object[] attrNamesObj;
	
	private final java.util.List<String> attrNames;

	/** */
	private final JXPanel centerPanel;
	
	/** */
	private GapFiller gapFiller;

	/** */
	private boolean isGapSimulated;
	
	/** */
	private double[] originalDataBeforeGapSimulation;
	
	/** */
	private String mostSimilar;
	/** */
	private String nearest;
	/** */
	private String upstream;
	/** */
	private String downstream;

	/** */
	private final StationsDataProvider gcp;

	/** */
	private final Instances originaldataSet;
	
	/** */
	private final boolean inBatchMode;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public GapFillingFrame(final AbstractTabView atv,final Instances dataSet,final Attribute attr,final int dateIdx,final int valuesBeforeAndAfter,final int position,final int gapsize,final StationsDataProvider gcp,final boolean inBatchMode) 
	{
		super();
		
		setTitle("Gap filling for "+attr.name()+" ("+dataSet.attribute(dateIdx).formatDate(dataSet.instance(position).value(dateIdx))+" -> "+dataSet.attribute(dateIdx).formatDate(dataSet.instance(position+gapsize).value(dateIdx))+")");
		LogoHelper.setLogo(this);
		
		this.atv=atv;
		
		this.dataSet=dataSet;
		this.attr=attr;
		this.dateIdx=dateIdx;
		this.valuesBeforeAndAfter=valuesBeforeAndAfter;
		this.position=position;
		this.gapsize=gapsize;		
		
		this.gcp=gcp;
		
		final Instances testds=WekaDataProcessingUtil.buildFilteredDataSet(dataSet,
				0,
				dataSet.numAttributes()-1,
				Math.max(0,position-valuesBeforeAndAfter),
				Math.min(position+gapsize+valuesBeforeAndAfter,dataSet.numInstances()-1));
		
		this.attrNames=WekaTimeSeriesUtil.getNamesOfAttributesWithoutGap(testds);
		
		this.isGapSimulated=(this.attrNames.contains(attr.name()));
		this.originaldataSet=new Instances(dataSet);
		if (this.isGapSimulated) 
		{	
			setTitle(getTitle()+ " [SIMULATED GAP]");
			/*final JXLabel fictiveGapLabel=new JXLabel("                                                                        FICTIVE GAP");
			fictiveGapLabel.setForeground(Color.RED);
			fictiveGapLabel.setFont(new Font(fictiveGapLabel.getFont().getName(), Font.PLAIN,fictiveGapLabel.getFont().getSize()*2));
			final JXPanel fictiveGapPanel=new JXPanel();
			fictiveGapPanel.setLayout(new BorderLayout());
			fictiveGapPanel.add(fictiveGapLabel,BorderLayout.CENTER);
			getContentPane().add(fictiveGapPanel,BorderLayout.NORTH);*/
			this.attrNames.remove(attr.name());
			this.originalDataBeforeGapSimulation=dataSet.attributeToDoubleArray(attr.index());
			for (int i=position;i<position+gapsize;i++) dataSet.instance(i).setMissing(attr);
		}
		
		final Object[] attrNamesObj=this.attrNames.toArray();
		
		this.centerPanel=new JXPanel();
		this.centerPanel.setLayout(new BorderLayout());
		getContentPane().add(this.centerPanel,BorderLayout.CENTER);
		
		//final JXPanel algoPanel=new JXPanel();
		//getContentPane().add(algoPanel,BorderLayout.NORTH);
		final JXPanel filterPanel=new JXPanel();		
		//filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));		
		filterPanel.setLayout(new GridBagLayout());
		final GridBagConstraints gbc=new GridBagConstraints();		 
		gbc.gridx=0;
		gbc.gridy=0;		 
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill=GridBagConstraints.HORIZONTAL; 
		gbc.insets=new Insets(10,10,10,10);		
		getContentPane().add(filterPanel,BorderLayout.WEST);
				
		final JXComboBox algoCombo=new JXComboBox(Algo.values());
		algoCombo.setBorder(new TitledBorder("Algorithm"));
		filterPanel.add(algoCombo,gbc);
		gbc.gridy++;
		
		final JXLabel infoLabel=new JXLabel("Usable = with no missing values on the period");
		//infoLabel.setBorder(new TitledBorder(""));
		filterPanel.add(infoLabel,gbc);
		gbc.gridy++;
		
		final JList<Object> timeSeriesList=new JList<Object>(attrNamesObj);
		timeSeriesList.setBorder(new TitledBorder("Usable time series"));
		timeSeriesList.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final JScrollPane jcpMap=new JScrollPane(timeSeriesList);
		jcpMap.setPreferredSize(new Dimension(225,150));
		jcpMap.setMinimumSize(new Dimension(225,150));
		filterPanel.add(jcpMap,gbc);
		gbc.gridy++;
		
		final JXPanel mapPanel=new JXPanel();
		mapPanel.setBorder(new TitledBorder(""));
		mapPanel.setLayout(new BorderLayout());
		mapPanel.add(gcp.getMapPanel(Arrays.asList(attr.name()),this.attrNames,true),BorderLayout.CENTER);
		filterPanel.add(mapPanel,gbc);
		gbc.gridy++;
		
		final JXLabel mssLabel=new JXLabel("<html>Most similar usable serie: <i>[... computation ...]</i></html>");
		mssLabel.setBorder(new TitledBorder(""));
		filterPanel.add(mssLabel,gbc);
		gbc.gridy++;
		
		final JXLabel nsLabel=new JXLabel("<html>Nearest usable serie: <i>[... computation ...]</i></html>");
		nsLabel.setBorder(new TitledBorder(""));
		filterPanel.add(nsLabel,gbc);
		gbc.gridy++;

		final JXLabel ussLabel=new JXLabel("<html>Upstream serie: <i>[... computation ...]</i></html>");
		ussLabel.setBorder(new TitledBorder(""));
		filterPanel.add(ussLabel,gbc);
		gbc.gridy++;

		final JXLabel dssLabel=new JXLabel("<html>Downstream serie: <i>[... computation ...]</i></html>");
		dssLabel.setBorder(new TitledBorder(""));
		filterPanel.add(dssLabel,gbc);
		gbc.gridy++;
		
		final JCheckBox hideOtherSeriesCB=new JCheckBox("Hide the others series");
		hideOtherSeriesCB.setSelected(DEFAULT_HIDE_OTHER_SERIES_OPTION);
		filterPanel.add(hideOtherSeriesCB,gbc);
		gbc.gridy++;

		final JCheckBox showErrorCB=new JCheckBox("Show error on plot");
		filterPanel.add(showErrorCB,gbc);
		gbc.gridy++;
		
		final JCheckBox zoomCB=new JCheckBox("Auto-adjusted size");
		zoomCB.setSelected(DEFAULT_ZOOM_OPTION);
		filterPanel.add(zoomCB,gbc);
		gbc.gridy++;
		
		final JCheckBox multAxisCB=new JCheckBox("Multiple axis");
		filterPanel.add(multAxisCB,gbc);
		gbc.gridy++;
		
		final JCheckBox showEnvelopeCB=new JCheckBox("Show envelope (all algorithms, SLOW)");
		filterPanel.add(showEnvelopeCB,gbc);
		gbc.gridy++;
		
		final JXButton showModelButton=new JXButton("Show the model");
		filterPanel.add(showModelButton,gbc);
		gbc.gridy++;				
		
		showModelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{	
				final JXFrame dialog=new JXFrame();
				dialog.setTitle("Model");
				LogoHelper.setLogo(dialog);
				dialog.getContentPane().removeAll();
				dialog.getContentPane().setLayout(new BorderLayout());
				final JTextPane modelTxtPane=new JTextPane();				
				modelTxtPane.setText(gapFiller.getModel());
				modelTxtPane.setBackground(Color.WHITE);
				modelTxtPane.setEditable(false);
				final JScrollPane jsp=new JScrollPane(modelTxtPane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				jsp.setSize(new Dimension(400-20,400-20));
				dialog.getContentPane().add(jsp,BorderLayout.CENTER);
				dialog.setSize(new Dimension(400,400));
				dialog.setLocationRelativeTo(centerPanel);
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		
		algoCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
					showModelButton.setEnabled(gapFiller.hasExplicitModel());
				} 
				catch (final Exception e1) 
				{
					e1.printStackTrace();
				}			
			}
		});		

		timeSeriesList.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(final ListSelectionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
					mapPanel.removeAll();
					final List<String> currentlySelected=new ArrayList<String>();
					currentlySelected.add(attr.name());
					for (final Object sel:timeSeriesList.getSelectedValues()) currentlySelected.add(sel.toString());
					mapPanel.add(gcp.getMapPanel(currentlySelected,attrNames,true),BorderLayout.CENTER);
				} 
				catch (final Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});				

		hideOtherSeriesCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
				} 
				catch (final Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});			
		
		showErrorCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});	
		
		zoomCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		
		showEnvelopeCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});	
		
		multAxisCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e) 
			{
				try 
				{
					refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),timeSeriesList.getSelectedIndices(),hideOtherSeriesCB.isSelected(),showErrorCB.isSelected(),zoomCB.isSelected(),showEnvelopeCB.isSelected(),multAxisCB.isSelected());
				} 
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		
		this.inBatchMode=inBatchMode;
		
		if (!inBatchMode)
		{
			try 
			{
				refresh(Algo.valueOf(algoCombo.getSelectedItem().toString()),new int[0],DEFAULT_HIDE_OTHER_SERIES_OPTION,false,DEFAULT_ZOOM_OPTION,false,false);
				showModelButton.setEnabled(gapFiller.hasExplicitModel());
			} 
			catch (Exception e1) 
			{			
				e1.printStackTrace();
			}
		}
		
		if (!inBatchMode)
		{
			/* automatically select computed series */
			new AbstractSimpleAsync<Void>(true)
			{
				@Override
				public Void execute() throws Exception 
				{
					mostSimilar=WekaTimeSeriesSimilarityUtil.findMostSimilarTimeSerie(testds,attr,attrNames,false);
					mssLabel.setText("<html>Most similar usable serie: <b>"+mostSimilar+"</b></html>");
					
					nearest=gcp.findNearestStation(attr.name(),attrNames);
					nsLabel.setText("<html>Nearest usable serie: <b>"+nearest+"</b></html>");
					
					upstream=gcp.findUpstreamStation(attr.name(),attrNames);
					if (upstream!=null)
					{
						ussLabel.setText("<html>Upstream usable serie: <b>"+upstream+"</b></html>");
					}
					else
					{
						ussLabel.setText("<html>Upstream usable serie: <b>N/A</b></html>");
					}
	
					downstream=gcp.findDownstreamStation(attr.name(),attrNames);
					if (downstream!=null)
					{
						dssLabel.setText("<html>Downstream usable serie: <b>"+downstream+"</b></html>");
					}
					else
					{
						dssLabel.setText("<html>Downstream usable serie: <b>N/A</b></html>");
					}
					
					timeSeriesList.setSelectedIndices(new int[]{attrNames.indexOf(mostSimilar),
																attrNames.indexOf(nearest),
																attrNames.indexOf(upstream),
																attrNames.indexOf(downstream)});
					
					return null;
				}
	
				@Override
				public void onSuccess(final Void result) {}
	
				@Override
				public void onFailure(final Throwable caught) 
				{
					caught.printStackTrace();
				}			
			}.start();	
		}
		else
		{
			try 
			{
				mostSimilar=WekaTimeSeriesSimilarityUtil.findMostSimilarTimeSerie(testds,attr,attrNames,false);
			} 
			catch (Exception e1) 
			{				
				e1.printStackTrace();
			}
			nearest=gcp.findNearestStation(attr.name(),attrNames);
			upstream=gcp.findUpstreamStation(attr.name(),attrNames);
			downstream=gcp.findDownstreamStation(attr.name(),attrNames);
		}
	}
	
	
	//
	// Instance methods
	//		
	
	public List<String> getUsedAttributes()
	{
		return this.attrNames;
	}

	public GapFillingCase refresh(final Algo algo,final int[] indexesOfUsedSeries) throws Exception
	{	
		final Set<Integer> set=new HashSet<Integer>();
		for (int i:indexesOfUsedSeries) set.add(i);
		final int[] newarray=new int[set.size()];		
		int k=0;
		for (Integer ii:set) newarray[k++]=ii.intValue(); 
		return refresh(algo,newarray,false,false,false,false,false);
	}
	
	private GapFillingCase refresh(final Algo algo,final int[] indexesOfUsedSeries,final boolean hideOthers,final boolean showError,final boolean zoom,final boolean showEnvelope,final boolean multAxis) throws Exception
	{				
		if (!inBatchMode) this.centerPanel.removeAll();
		
		int[] arr=new int[]{attr.index(),dateIdx};
		for (final int iii:indexesOfUsedSeries)
		{
			arr=ArraysUtil.concat(arr,new int[]{dataSet.attribute(this.attrNames.get(iii)).index()});
		}
		
		Instances filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,arr);
		//System.out.println(filteredDs.toSummaryString());
		
		Attribute original=null;
		Instances filteredDsWithOriginal=null;
		if (this.isGapSimulated)
		{
			original=new Attribute("original");
			filteredDsWithOriginal=new Instances(filteredDs);
			filteredDsWithOriginal.insertAttributeAt(original,filteredDsWithOriginal.numAttributes()-1);
			final Attribute origAttr=filteredDsWithOriginal.attribute(original.name());
			for (int ii=position-1;ii<position+gapsize+1;ii++)
			{
				filteredDsWithOriginal.instance(ii).setValue(origAttr,this.originalDataBeforeGapSimulation[ii]);
			}
		}
				
		filteredDs=WekaDataProcessingUtil.buildFilteredDataSet(filteredDs,
																0,
																filteredDs.numAttributes()-1,
																Math.max(0,this.position-this.valuesBeforeAndAfter),
																Math.min(this.position+this.gapsize+this.valuesBeforeAndAfter,filteredDs.numInstances()-1));
		
		this.gapFiller=GapFillerFactory.getGapFiller(algo);				
		
		final Instances completedds=this.gapFiller.fillGaps(filteredDs);			
		final Instances diff=WekaTimeSeriesUtil.buildDiff(filteredDs,completedds);
				
		final int valuesToCheckForError=this.valuesBeforeAndAfter/4;
		
		double maeByEnlargingGap=Double.NaN;
		double maeByAddingAGapBefore=Double.NaN;
		double maeByAddingAGapAfter=Double.NaN;		
		double maeByComparingWithOriginal=Double.NaN;
		
		double rmseByEnlargingGap=Double.NaN;
		double rmseByAddingAGapBefore=Double.NaN;
		double rmseByAddingAGapAfter=Double.NaN;
		double rmseByComparingWithOriginal=Double.NaN;
	
		double rsrByEnlargingGap=Double.NaN;
		double rsrByAddingAGapBefore=Double.NaN;
		double rsrByAddingAGapAfter=Double.NaN;
		double rsrByComparingWithOriginal=Double.NaN;
		
		double pbiasByEnlargingGap=Double.NaN;
		double pbiasByAddingAGapBefore=Double.NaN;
		double pbiasByAddingAGapAfter=Double.NaN;
		double pbiasByComparingWithOriginal=Double.NaN;
		
		double nsByEnlargingGap=Double.NaN;
		double nsByAddingAGapBefore=Double.NaN;
		double nsByAddingAGapAfter=Double.NaN;
		double nsByComparingWithOriginal=Double.NaN;
		
		double indexOfAgreementByEnlargingGap=Double.NaN;
		double indexOfAgreementByAddingAGapBefore=Double.NaN;
		double indexOfAgreementByAddingAGapAfter=Double.NaN;
		double indexOfAgreementByComparingWithOriginal=Double.NaN;
		
		if (this.isGapSimulated)
		{
			//System.out.println(attr.index()+" begin="+(this.position)+" end="+(this.position+this.gapsize));
			
			final Instances correctedDataSet=buildCorrectedDataset(diff);
			
			final double[] cad=correctedDataSet.attributeToDoubleArray(attr.index());
			maeByComparingWithOriginal=MathsUtil.mae(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);
			rmseByComparingWithOriginal=MathsUtil.rmse(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);
			rsrByComparingWithOriginal=MathsUtil.rsr(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);
			pbiasByComparingWithOriginal=MathsUtil.pbias(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);						
			nsByComparingWithOriginal=MathsUtil.nashSutcliffe(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);
			indexOfAgreementByComparingWithOriginal=MathsUtil.indexOfAgreement(this.originalDataBeforeGapSimulation,cad,this.position,this.position+this.gapsize);						
		}
		else
		{
			maeByEnlargingGap=this.gapFiller.evaluateMAEByEnlargingGap(filteredDs,valuesToCheckForError);
			maeByAddingAGapBefore=this.gapFiller.evaluateMAEByAddingAGapBefore(filteredDs,valuesToCheckForError);
			maeByAddingAGapAfter=this.gapFiller.evaluateMAEByAddingAGapAfter(filteredDs,valuesToCheckForError);
			
			rmseByEnlargingGap=this.gapFiller.evaluateRMSEByEnlargingGap(filteredDs,valuesToCheckForError);
			rmseByAddingAGapBefore=this.gapFiller.evaluateRMSEByAddingAGapBefore(filteredDs,valuesToCheckForError);
			rmseByAddingAGapAfter=this.gapFiller.evaluateRMSEByAddingAGapAfter(filteredDs,valuesToCheckForError);
			
			nsByEnlargingGap=this.gapFiller.evaluateNSByEnlargingGap(filteredDs,valuesToCheckForError);
			nsByAddingAGapBefore=this.gapFiller.evaluateNSByAddingAGapBefore(filteredDs,valuesToCheckForError);
			nsByAddingAGapAfter=this.gapFiller.evaluateNSByAddingAGapAfter(filteredDs,valuesToCheckForError);
		}
		
		if (hideOthers)
		{
			if (this.isGapSimulated)
			{
				filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(filteredDsWithOriginal,new int[]{0,1,filteredDsWithOriginal.attribute(original.name()).index()});
				filteredDs=WekaDataProcessingUtil.buildFilteredDataSet(filteredDs,
						0,
						filteredDs.numAttributes()-1,
						Math.max(0,this.position-this.valuesBeforeAndAfter),
						Math.min(this.position+this.gapsize+this.valuesBeforeAndAfter,filteredDs.numInstances()-1));				
			}
			else
			{
				filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(filteredDs,new int[]{0,1});
			}			
		}
		
		final Instances decomposition=WekaTimeSeriesUtil.buildMergedDataSet(filteredDs,diff);
		
		final Attribute diffAttribute=decomposition.attribute(attr.name()+"_diff");
		
		final List<XYAnnotation> aaa=new ArrayList<XYAnnotation>();
		if (showError)
		{	    							
			showError(this.isGapSimulated?maeByComparingWithOriginal:maeByEnlargingGap,decomposition,diffAttribute,aaa);			
		}
		
		if (showEnvelope)
		{
			final MainViewLoadingFrame loadingFrame=new MainViewLoadingFrame();
			loadingFrame.setVisible(true);
			loadingFrame.pack();
			loadingFrame.repaint();			
			showEnvelope(arr,aaa);			
			loadingFrame.setVisible(false);
		}
		
		
		if (!inBatchMode) 
		{
			final ChartPanel cp;
			/*if (showError)
			{
				cp=TimeSeriesChartUtil.buildChartPanelForAllAttributesInterval(decomposition,WekaDataStatsUtil.getFirstDateAttributeIdx(decomposition),mae,diffAttribute.index());
			}
			else
			{*/
			cp=TimeSeriesChartUtil.buildChartPanelForAllAttributes(decomposition,multAxis,WekaDataStatsUtil.getFirstDateAttributeIdx(decomposition),null,aaa);			
			/*}*/
			
			final Marker gapBeginMarker=new ValueMarker(dataSet.instance(Math.max(0,position-1)).value(dateIdx));
			gapBeginMarker.setPaint(Color.RED);
			gapBeginMarker.setLabel("Gap begin");
			gapBeginMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			gapBeginMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
			cp.getChart().getXYPlot().addDomainMarker(gapBeginMarker);
				
			final Marker gapEndMarker=new ValueMarker(dataSet.instance(Math.min(dataSet.numInstances()-1,position+gapsize)).value(dateIdx));
			gapEndMarker.setPaint(Color.RED);
			gapEndMarker.setLabel("Gap end");
			gapEndMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			gapEndMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
			cp.getChart().getXYPlot().addDomainMarker(gapEndMarker);		
				
			if (!zoom)
			{
				final NumberAxis na=(NumberAxis)(cp.getChart().getXYPlot().getRangeAxis());
				na.setRange(0, WekaDataStatsUtil.getMaxValue(dataSet,attrNames));
			}
				
			String errorInfo;
			if (!this.isGapSimulated)
			{		
				errorInfo="By enlarging the gap:\t MAE="+FormatterUtil.DECIMAL_FORMAT_4.format(maeByEnlargingGap)
												+"\t RMSE="+FormatterUtil.DECIMAL_FORMAT_4.format(rmseByEnlargingGap)
												+"\t NASH-SUTCLIFFE="+FormatterUtil.DECIMAL_FORMAT_4.format(nsByEnlargingGap)
						+"\nBy adding a gap before:\t MAE="+FormatterUtil.DECIMAL_FORMAT_4.format(maeByAddingAGapBefore)
												+"\t RMSE="+FormatterUtil.DECIMAL_FORMAT_4.format(rmseByAddingAGapBefore)							
												+"\t NASH-SUTCLIFFE="+FormatterUtil.DECIMAL_FORMAT_4.format(nsByAddingAGapBefore)
						+"\nBy adding a gap after:\t MAE="+FormatterUtil.DECIMAL_FORMAT_4.format(maeByAddingAGapAfter)
												+"\t RMSE="+FormatterUtil.DECIMAL_FORMAT_4.format(rmseByAddingAGapAfter)
												+"\t NASH-SUTCLIFFE="+FormatterUtil.DECIMAL_FORMAT_4.format(nsByAddingAGapAfter);			
			}
			else
			{
				errorInfo="MAE: "+FormatterUtil.DECIMAL_FORMAT_4.format(maeByComparingWithOriginal);
				errorInfo+="\n";
				errorInfo+="RMSE: "+FormatterUtil.DECIMAL_FORMAT_4.format(rmseByComparingWithOriginal);
				errorInfo+="\n";
				errorInfo+="RSR: "+FormatterUtil.DECIMAL_FORMAT_4.format(rsrByComparingWithOriginal);
				errorInfo+="\n";
				errorInfo+="PBIAS: "+FormatterUtil.DECIMAL_FORMAT_4.format(pbiasByComparingWithOriginal);
				errorInfo+="\n";						
				errorInfo+="NASH-SUTCLIFFE: "+FormatterUtil.DECIMAL_FORMAT_4.format(nsByComparingWithOriginal);
				errorInfo+="\n";						
				errorInfo+="INDEX OF AGREEMENT: "+FormatterUtil.DECIMAL_FORMAT_4.format(indexOfAgreementByComparingWithOriginal);						
			}
			cp.setBorder(new TitledBorder(""));   
			final JTextArea errorTextArea=new JTextArea(errorInfo);
			errorTextArea.setBorder(new TitledBorder(""));
			this.centerPanel.add(errorTextArea,BorderLayout.NORTH);
			this.centerPanel.add(cp,BorderLayout.CENTER);
			
			final JXPanel cmdPanel=new JXPanel();
			final JXButton okButton=new JXButton("Ok");
			okButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					final Instances correctedDataSet=buildCorrectedDataset(diff);
					
					final DataChange change=new DataChange(correctedDataSet,TabView.DataChangeTypeEnum.Update);
					atv.pushDataChange(change);
					
					setVisible(false);												
				}
			});									
			cmdPanel.add(okButton);
			final JXButton cancelButton=new JXButton("Cancel");
			cancelButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					setVisible(false);									
				}
			});
			cmdPanel.add(cancelButton);
			this.centerPanel.add(cmdPanel,BorderLayout.SOUTH);
			
			this.centerPanel.updateUI();
			
			getContentPane().repaint();
		}
		
		final double globalMAE=(this.isGapSimulated)?maeByComparingWithOriginal
													:((maeByEnlargingGap+maeByAddingAGapBefore+maeByAddingAGapAfter)/3);
		
		final double globalRMSE=(this.isGapSimulated)?rmseByComparingWithOriginal
													 :((rmseByEnlargingGap+rmseByAddingAGapBefore+rmseByAddingAGapAfter)/3);		

		final double globalRSR=(this.isGapSimulated)?rsrByComparingWithOriginal
				 									:((rsrByEnlargingGap+rsrByAddingAGapBefore+rsrByAddingAGapAfter)/3);				

		final double globalPBIAS=(this.isGapSimulated)?pbiasByComparingWithOriginal
													:((pbiasByEnlargingGap+pbiasByAddingAGapBefore+pbiasByAddingAGapAfter)/3);						
		
		final double globalNS=(this.isGapSimulated)?nsByComparingWithOriginal
													:((nsByEnlargingGap+nsByAddingAGapBefore+nsByAddingAGapAfter)/3);				

		final double globalIndexOfAgreement=(this.isGapSimulated)?indexOfAgreementByComparingWithOriginal
																 :((indexOfAgreementByEnlargingGap+indexOfAgreementByAddingAGapBefore+indexOfAgreementByAddingAGapAfter)/3);						
		
		// usage logs for stats		
		final long firstTimestamp=(long)dataSet.instance(position).value(dateIdx);			
		final boolean isDuringRising;
		if (nearest==null) isDuringRising=GapsUtil.isDuringRising(dataSet,position,gapsize,new int[]{dateIdx,attr.index()});
		else isDuringRising=GapsUtil.isDuringRising(dataSet,position,gapsize,new int[]{dateIdx,attr.index(),dataSet.attribute(nearest).index()});
		
		return new GapFillingCase(DateUtil.getSeason(firstTimestamp),
								  			DateUtil.getYear(firstTimestamp),
											algo,
											gapsize,
											position,
											attr,
											gcp.getCoordinates(attr.name())[0],
											gcp.getCoordinates(attr.name())[1],
											gcp.findDownstreamStation(attr.name())!=null,
											gcp.findUpstreamStation(attr.name())!=null,
											globalMAE,
											globalRMSE,
											globalRSR,
											globalPBIAS,
											globalNS,
											globalIndexOfAgreement,
											ArraysUtil.contains(indexesOfUsedSeries,attrNames.indexOf(mostSimilar)),
											ArraysUtil.contains(indexesOfUsedSeries,attrNames.indexOf(nearest)),
											ArraysUtil.contains(indexesOfUsedSeries,attrNames.indexOf(downstream)),
											ArraysUtil.contains(indexesOfUsedSeries,attrNames.indexOf(upstream)),
											isDuringRising,
											GapsUtil.measureHighMiddleLowInterval(dataSet,attr.index(),position-1));
	}

	private Instances buildCorrectedDataset(final Instances diff)
	{
		//System.out.println("Build a corrected dataset ...");
		
		final Instances correctedDataSet=new Instances(dataSet);
		final int corrNumInstances=correctedDataSet.numInstances();				
						
		final int diffNumInstances=diff.numInstances();
		final int diffNumAttributes=diff.numAttributes();
		
		final int idxInDiff=0;
						
		for (int k=0;k<diffNumInstances;k++)
		{
			final Instance diffInstanceK=diff.instance(k);
			
			if (diffInstanceK.isMissing(idxInDiff)) continue;
			
			final long timestamp=(long)diffInstanceK.value(diffNumAttributes-1);
								
			for (int h=0;h<corrNumInstances;h++)
			{
				if ((long)correctedDataSet.instance(h).value(dateIdx)==timestamp)
				{
					correctedDataSet.instance(h).setValue(attr,diffInstanceK.value(idxInDiff));					
					break;
				}
			}
		}
						
		//System.out.println("... corrected dataset built!");
		
		return correctedDataSet;
	}
	

	private void showEnvelope(final int[] arr,final List<XYAnnotation> aaa) throws Exception 
	{
	    final Color cc=ColorHelper.getColorForAString(attr.name());
	    final Color newcc=new Color(cc.getRed(),cc.getGreen(),cc.getBlue(),cc.getAlpha()/4).brighter();

		
	    final Map<Double,Double> minMap=new HashMap<Double,Double>();
	    final Map<Double,Double> maxMap=new HashMap<Double,Double>();
		
		for (final Algo aall:Algo.values())
		{
			Instances filteredDs2=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,arr);
			
			filteredDs2=WekaDataProcessingUtil.buildFilteredDataSet(filteredDs2,
																	0,
																	filteredDs2.numAttributes()-1,
																	Math.max(0,this.position-this.valuesBeforeAndAfter),
																	Math.min(this.position+this.gapsize+this.valuesBeforeAndAfter,filteredDs2.numInstances()-1));
			
			final GapFiller gp=GapFillerFactory.getGapFiller(aall);				
			
			final Instances completedds2=gp.fillGaps(filteredDs2);			
			final Instances diff2=WekaTimeSeriesUtil.buildDiff(filteredDs2,completedds2);
			
			//final double mae2=this.gapFiller.evaluateMeanAbsoluteError(filteredDs2,valuesBeforeAndAfterForMAE);
			
			final Instances decomposition2=WekaTimeSeriesUtil.buildMergedDataSet(filteredDs2,diff2);
			final Attribute diffAttribute2=decomposition2.attribute(attr.name()+"_diff");
			
			final Attribute timestampDiffAttribute2=decomposition2.attribute(WekaDataStatsUtil.getFirstDateAttributeIdx(decomposition2));

		    for (int i=1;i<decomposition2.numInstances()-1;i++) 
		    {
		    	if (!decomposition2.instance(i).isMissing(diffAttribute2)/*&&i%10==0*/)
		    	{
		    		final double d=decomposition2.instance(i).value(diffAttribute2);
		     		final double timestamp=decomposition2.instance(i).value(timestampDiffAttribute2);

		     		aaa.add(new XYDrawableAnnotation(timestamp,d,1,1,new AnnotationDrawer(newcc)));
		    		
		    		if (!minMap.containsKey(timestamp)||minMap.get(timestamp)>d) minMap.put(timestamp,d);
		    		if (!maxMap.containsKey(timestamp)||maxMap.get(timestamp)<d) maxMap.put(timestamp,d);
		    	}
		    }
		}
		
		for (final Map.Entry<Double,Double> entry:minMap.entrySet())
		{		
			final Double timestamp=entry.getKey();
			final Double min=minMap.get(timestamp);
			final Double max=maxMap.get(timestamp);
			if (min>max) throw new IllegalStateException("min>max -> "+min+">"+max);
			else if (max>min)
			{
				final double step=(max-min)/20d;			
				for (double dd=min;dd<=max;dd+=step)
				{
					aaa.add(new XYDrawableAnnotation(timestamp,dd,1,1,new AnnotationDrawer(newcc)));
				}
			}
		}		
		
		System.out.println("done");
	}


	private void showError(final double mae,final Instances decomposition,final Attribute diffAttribute, final List<XYAnnotation> aaa) 
	{
		//System.out.println("*************** SHOW ERROR **************************");
		final Attribute timestampDiffAttribute=decomposition.attribute(WekaDataStatsUtil.getFirstDateAttributeIdx(decomposition));
		final Color cc=ColorHelper.getColorForAString(diffAttribute.name());
		final Color newcc=new Color(cc.getRed(),cc.getGreen(),cc.getBlue(),cc.getAlpha()/4).brighter();
		for (int i=1;i<decomposition.numInstances()-1;i++) 
		{
			//if (i%10!=1) continue;
			if (!decomposition.instance(i).isMissing(diffAttribute)/*&&i%10==0*/)
			{
				final double d=decomposition.instance(i).value(diffAttribute);
		 		final double timestamp=decomposition.instance(i).value(timestampDiffAttribute);
		 			         		         		
				aaa.add(new XYDrawableAnnotation(timestamp,d+mae,0.5,0.5,new AnnotationDrawer(newcc)));
				aaa.add(new XYDrawableAnnotation(timestamp,d-mae,0.5,0.5,new AnnotationDrawer(newcc)));
				for (double dd=d-mae;dd<=d+mae;dd+=mae/20)
				{
					aaa.add(new XYDrawableAnnotation(timestamp,dd,1,1,new AnnotationDrawer(newcc)));
				}
				//aaa.add(new XYDrawableAnnotation(timestamp,d,1,1,new AnnotationDrawer(cc)));
			}
		}
		//System.out.println("*****************************************************");
	}

	public void fillGapWithBestConfig() 
	{		
		throw new IllegalStateException("Not implemented!");
	}


	public String getMostSimilar() 
	{
		return this.mostSimilar;
	}

	public String getNearest() 
	{
		return this.nearest;
	}

	public String getUpstream() 
	{
		return this.upstream;
	}

	public String getDownstream() 
	{
		return this.downstream;
	}

	public Instances getDataset()
	{
		return this.dataSet;
	}

	public boolean isGapSimulated()
	{
		return this.isGapSimulated;
	}
}
