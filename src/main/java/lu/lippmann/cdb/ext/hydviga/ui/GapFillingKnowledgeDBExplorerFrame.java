/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.common.gui.*;
import lu.lippmann.cdb.common.gui.dataset.InstanceTableModel;
import lu.lippmann.cdb.ext.hydviga.cbr.GapFillingKnowledgeDB;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.gaps.*;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;
import lu.lippmann.cdb.lab.timeseries.WekaTimeSeriesSimilarityUtil;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jfree.chart.ChartPanel;
import weka.core.*;


/**
 * GapFillingKnowledgeDBExplorerFrame.
 * 
 * @author the HYDVIGA team
 */
public final class GapFillingKnowledgeDBExplorerFrame extends JXFrame 
{
	//
	// Static fields
	//

	/** */
	private static final long serialVersionUID=1L;
	
	/** */
	private static final int COMPONENT_WIDTH=1150;	
	/** */
	private static final int FRAME_WIDTH=(int)(COMPONENT_WIDTH*1.05);
	/** */
	private static final Dimension CHART_DIMENSION=new Dimension(COMPONENT_WIDTH,120);

	
	//
	// Instance fields
	//
	
	/** */
	private JXPanel tablePanel;
	/** */
	private final JXPanel geomapPanel;
	
	/** */
	private JXPanel caseChartPanel;
	/** */
	private JXPanel mostSimilarChartPanel;
	/** */
	private JXPanel nearestChartPanel;
	/** */
	private JXPanel downstreamChartPanel;
	/** */
	private JXPanel upstreamChartPanel;

	/** */
	private final StationsDataProvider gcp;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	GapFillingKnowledgeDBExplorerFrame(final Instances ds,final int dateIdx,final StationsDataProvider gcp) throws Exception
	{
		LogoHelper.setLogo(this);
		this.setTitle("KnowledgeDB: explorer");
		
		this.gcp=gcp;
		
		this.tablePanel=new JXPanel();
		this.tablePanel.setBorder(new TitledBorder("Cases"));
		
		final JXPanel highPanel=new JXPanel();
		highPanel.setLayout(new BoxLayout(highPanel,BoxLayout.X_AXIS));
		highPanel.add(this.tablePanel);		
		
		this.geomapPanel=new JXPanel();
		this.geomapPanel.add(buildGeoMapChart(new ArrayList<String>(),new ArrayList<String>()));
		highPanel.add(this.geomapPanel);
		
		
		this.caseChartPanel=new JXPanel();
		this.caseChartPanel.setBorder(new TitledBorder("Inspected fake gap"));

		this.mostSimilarChartPanel=new JXPanel();
		this.mostSimilarChartPanel.setBorder(new TitledBorder("Most similar"));		
		this.nearestChartPanel=new JXPanel();
		this.nearestChartPanel.setBorder(new TitledBorder("Nearest"));
		this.downstreamChartPanel=new JXPanel();
		this.downstreamChartPanel.setBorder(new TitledBorder("Downstream"));
		this.upstreamChartPanel=new JXPanel();
		this.upstreamChartPanel.setBorder(new TitledBorder("Upstream"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		//getContentPane().add(new JCheckBox("Use incomplete series"));
		getContentPane().add(highPanel);
		//getContentPane().add(new JXButton("Export"));		
		getContentPane().add(caseChartPanel);
		getContentPane().add(mostSimilarChartPanel);
		getContentPane().add(nearestChartPanel);
		getContentPane().add(downstreamChartPanel);
		getContentPane().add(upstreamChartPanel);
		
		//final Instances kdbDS=GapFillingKnowledgeDB.getKnowledgeDBWithBestCasesOnly();		
		final Instances kdbDS=GapFillingKnowledgeDB.getKnowledgeDB();
		
		final JXTable gapsTable=buidJXTable(kdbDS);		
		final JScrollPane tableScrollPane=new JScrollPane(gapsTable);
		tableScrollPane.setPreferredSize(new Dimension(COMPONENT_WIDTH-100,40+(int)(tableScrollPane.getPreferredSize().getHeight())));
		this.tablePanel.add(tableScrollPane);
		
		gapsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gapsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
	    {
            @Override
            public void valueChanged(final ListSelectionEvent e) 
            {
                if (!e.getValueIsAdjusting()) 
                {
                	final int modelRow=gapsTable.getSelectedRow();
                	
    				final String attrname=gapsTable.getModel().getValueAt(modelRow,1).toString();
    				final int gapsize=(int)Double.valueOf(gapsTable.getModel().getValueAt(modelRow,4).toString()).doubleValue();
    				final int position=(int)Double.valueOf(gapsTable.getModel().getValueAt(modelRow,5).toString()).doubleValue(); 														
                	
    				final String mostSimilarFlag=gapsTable.getModel().getValueAt(modelRow,14).toString();
    				final String nearestFlag=gapsTable.getModel().getValueAt(modelRow,15).toString();
    				final String downstreamFlag=gapsTable.getModel().getValueAt(modelRow,16).toString();
    				final String upstreamFlag=gapsTable.getModel().getValueAt(modelRow,17).toString();
    				
    				final String algoname=gapsTable.getModel().getValueAt(modelRow,12).toString();
    				final boolean useDiscretizedTime=Boolean.valueOf(gapsTable.getModel().getValueAt(modelRow,13).toString());
    				
					try 
					{
						geomapPanel.removeAll();
						
						caseChartPanel.removeAll();
						
						mostSimilarChartPanel.removeAll();
						nearestChartPanel.removeAll();
						downstreamChartPanel.removeAll();
						upstreamChartPanel.removeAll();
												
						final Set<String> selected=new HashSet<String>();
						
						final Instances tmpds=WekaDataProcessingUtil.buildFilteredDataSet(ds,
								0,
								ds.numAttributes()-1,
								Math.max(0,position-GapsUtil.getCountOfValuesBeforeAndAfter(gapsize)),
								Math.min(position+gapsize+GapsUtil.getCountOfValuesBeforeAndAfter(gapsize),ds.numInstances()-1));												
						
						final List<String> attributeNames=WekaTimeSeriesUtil.getNamesOfAttributesWithoutGap(tmpds);
						//final List<String> attributeNames=WekaDataStatsUtil.getAttributeNames(ds);
						attributeNames.remove(attrname);
						attributeNames.remove("timestamp");						
						
						if (Boolean.valueOf(mostSimilarFlag))
						{
							final String mostSimilarStationName=WekaTimeSeriesSimilarityUtil.findMostSimilarTimeSerie(tmpds,tmpds.attribute(attrname),attributeNames,false);
							selected.add(mostSimilarStationName);
							final Attribute mostSimilarStationAttr=tmpds.attribute(mostSimilarStationName);
							
							final ChartPanel cp0=GapsUIUtil.buildGapChartPanel(ds,dateIdx,mostSimilarStationAttr,gapsize,position);
							cp0.getChart().removeLegend();
							cp0.setPreferredSize(CHART_DIMENSION);						
							mostSimilarChartPanel.add(cp0);
						}
						
						if (Boolean.valueOf(nearestFlag))
						{
							final String nearestStationName=gcp.findNearestStation(attrname,attributeNames);
							selected.add(nearestStationName);
							final Attribute nearestStationAttr=ds.attribute(nearestStationName);
							
							final ChartPanel cp0=GapsUIUtil.buildGapChartPanel(ds,dateIdx,nearestStationAttr,gapsize,position);
							cp0.getChart().removeLegend();
							cp0.setPreferredSize(CHART_DIMENSION);						
							nearestChartPanel.add(cp0);
						}
						
						if (Boolean.valueOf(downstreamFlag))
						{
							final String downstreamStationName=gcp.findDownstreamStation(attrname,attributeNames);
							selected.add(downstreamStationName);
							final Attribute downstreamStationAttr=ds.attribute(downstreamStationName);
							
							final ChartPanel cp0=GapsUIUtil.buildGapChartPanel(ds,dateIdx,downstreamStationAttr,gapsize,position);
							cp0.getChart().removeLegend();
							cp0.setPreferredSize(CHART_DIMENSION);						
							downstreamChartPanel.add(cp0);
						}
						
						if (Boolean.valueOf(upstreamFlag))
						{
							final String upstreamStationName=gcp.findUpstreamStation(attrname,attributeNames);
							selected.add(upstreamStationName);
							final Attribute upstreamStationAttr=ds.attribute(upstreamStationName);
							
							final ChartPanel cp0=GapsUIUtil.buildGapChartPanel(ds,dateIdx,upstreamStationAttr,gapsize,position);
							cp0.getChart().removeLegend();
							cp0.setPreferredSize(CHART_DIMENSION);						
							upstreamChartPanel.add(cp0);
						}
						
						final GapFiller gapFiller=GapFillerFactory.getGapFiller(algoname,useDiscretizedTime);
						final ChartPanel cp=GapsUIUtil.buildGapChartPanelWithCorrection(ds,dateIdx,ds.attribute(attrname),gapsize,position,gapFiller,selected);
						cp.getChart().removeLegend();
						cp.setPreferredSize(new Dimension((int)CHART_DIMENSION.getWidth(),(int)(CHART_DIMENSION.getHeight()*1.5)));						
						caseChartPanel.add(cp);

						
						geomapPanel.add(buildGeoMapChart(Arrays.asList(attrname),selected));
						
						getContentPane().repaint();
						pack();
					} 
					catch (Exception e1) 
					{
						e1.printStackTrace();
					}
                }
            }
	    });
		
		gapsTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if (!e.isPopupTrigger())
				{
					// nothing?
				}
				else
				{
					final JPopupMenu jPopupMenu=new JPopupMenu("feur");

					final JMenuItem mExport=new JMenuItem("Export this table as CSV");
					mExport.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							final JFileChooser fc = new JFileChooser();
							fc.setAcceptAllFileFilterUsed(false);
							final int returnVal = fc.showSaveDialog(gapsTable);
							if (returnVal == JFileChooser.APPROVE_OPTION) 
							{
								try 
								{
									final File file=fc.getSelectedFile();
									WekaDataAccessUtil.saveInstancesIntoCSVFile(kdbDS,file);
								} 
								catch (Exception ee) 
								{
									ee.printStackTrace();
								}
							} 							
						}
					});
					jPopupMenu.add(mExport);													              		             		                																									
					
					jPopupMenu.show(gapsTable,e.getX(),e.getY());
				}
			}
		});	 
		
		setPreferredSize(new Dimension(FRAME_WIDTH,1000));
		
		pack();
		setVisible(true);									

		/* select the first row */
		gapsTable.setRowSelectionInterval(0, 0);
	}


	private ChartPanel buildGeoMapChart(final Collection<String> sel,final Collection<String> usable)
	{
		final ChartPanel geoMapChart=gcp.getMapPanel(sel,usable,false);
		geoMapChart.setPreferredSize(new Dimension(134,180)); //112,150
		geoMapChart.setMinimumSize(new Dimension(134,180));
		return geoMapChart;
	}
	
	private JXTable buidJXTable(final Instances cases) 
	{
		final InstanceTableModel outputCasesTableModel=new InstanceTableModel();
		outputCasesTableModel.setDataset(cases);
		final JXTable outputCasesTable=new JXTable(outputCasesTableModel);
		outputCasesTable.setEditable(false);
		outputCasesTable.setShowHorizontalLines(false);
		outputCasesTable.setShowVerticalLines(false);
		outputCasesTable.setVisibleRowCount(5);
		outputCasesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		final HighlightPredicate myPredicate=new HighlightPredicate() 
		{										  
			@Override
			public boolean isHighlighted(final Component arg0,final ComponentAdapter arg1) 
			{
				final String trueColumnName=(arg1.column>0)?cases.attribute(arg1.column-1).name():"id";
				final java.util.List<String> inputFieldsList=Arrays.asList(GapFillingKnowledgeDB.INPUT_FIELDS);											
				return (inputFieldsList.contains(trueColumnName));
			}
		};			
		outputCasesTable.addHighlighter(new ColorHighlighter(myPredicate,null,Color.BLUE));
		outputCasesTable.packAll();
		return outputCasesTable;
	}

	
}
