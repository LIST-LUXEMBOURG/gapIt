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
import javax.swing.event.*;

import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.common.gui.dataset.InstanceTableModel;
import lu.lippmann.cdb.datasetview.tabs.AbstractTabView;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;
import lu.lippmann.cdb.lab.timeseries.WekaTimeSeriesSimilarityUtil;
import lu.lippmann.cdb.weka.*;

import org.jdesktop.swingx.*;
import org.jfree.chart.ChartPanel;
import weka.core.*;


/**
 * GapsOverviewPanel.
 *
 * @author the HYDVIGA team
 */
public final class GapsOverviewPanel
{	
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	/** */
	private final AbstractTabView atv;

	/** */
	private final JXPanel tablePanel;
	/** */
	private final JXPanel visualOverviewPanel;
	
	/** */
	private final StationsDataProvider gcp;
	
	/** */
	private final JXPanel geomapPanel;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public GapsOverviewPanel(final AbstractTabView atv,final StationsDataProvider gcp)
	{	
		this.atv=atv;
		
		this.gcp=gcp;
		
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new GridLayout(2,1));
		
		final JXPanel highPanel=new JXPanel();
		highPanel.setLayout(new BoxLayout(highPanel,BoxLayout.X_AXIS));
		this.jxp.add(highPanel);
		
		this.tablePanel=new JXPanel();
		this.tablePanel.setLayout(new BorderLayout());		
		highPanel.add(this.tablePanel);
		
		this.geomapPanel=new JXPanel();
		this.geomapPanel.add(gcp.getMapPanel(new ArrayList<String>(),new ArrayList<String>(),false));
		highPanel.add(this.geomapPanel);
		
		this.visualOverviewPanel=new JXPanel();
		this.visualOverviewPanel.setLayout(new BorderLayout());
		this.jxp.add(this.visualOverviewPanel);
	}

	
	//
	// Instance methods
	//
	
	public Component getComponent() 
	{					
		return jxp;
	}
	
	public void refresh(final Instances dataSet,final int dateIdx)
	{
		final Instances gapsDescriptionsDataset=GapsUtil.buildGapsDescription(gcp,dataSet,dateIdx);
		
		final JXTable gapsDescriptionsTable=new JXTable();			
		final InstanceTableModel gapsDescriptionsTableModel=new InstanceTableModel(false);
		gapsDescriptionsTableModel.setDataset(gapsDescriptionsDataset);
		gapsDescriptionsTable.setModel(gapsDescriptionsTableModel);
		gapsDescriptionsTable.setEditable(true);
		gapsDescriptionsTable.setShowHorizontalLines(false);
		gapsDescriptionsTable.setShowVerticalLines(false);
		gapsDescriptionsTable.setVisibleRowCount(5);
		gapsDescriptionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		gapsDescriptionsTable.setSortable(false);
		gapsDescriptionsTable.packAll();
		
		gapsDescriptionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gapsDescriptionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
	    {
            @Override
            public void valueChanged(ListSelectionEvent e) 
            {
                if (!e.getValueIsAdjusting()) 
                {
                	int modelRow=gapsDescriptionsTable.getSelectedRow();
                	if (modelRow<0) modelRow=0;
                	
    				final String attrname=gapsDescriptionsTableModel.getValueAt(modelRow,1).toString();
    				final Attribute attr=dataSet.attribute(attrname);
    				final int gapsize=(int)Double.valueOf(gapsDescriptionsTableModel.getValueAt(modelRow,5).toString()).doubleValue();
    				final int position=(int)Double.valueOf(gapsDescriptionsTableModel.getValueAt(modelRow,6).toString()).doubleValue(); 														

    				try
					{
						final ChartPanel cp=GapsUIUtil.buildGapChartPanel(dataSet,dateIdx,attr,gapsize,position);							
						
						visualOverviewPanel.removeAll();
						visualOverviewPanel.add(cp,BorderLayout.CENTER);							
						
						geomapPanel.removeAll();
						geomapPanel.add(gcp.getMapPanel(Arrays.asList(attrname),new ArrayList<String>(),false));
						
						jxp.updateUI();
					}
					catch(Exception ee)
					{
						ee.printStackTrace();
					}
                }
            }
	    });
		
		gapsDescriptionsTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(final MouseEvent e)
			{
				final InstanceTableModel instanceTableModel=(InstanceTableModel)gapsDescriptionsTable.getModel();
				final int row=gapsDescriptionsTable.rowAtPoint(e.getPoint());
				//final int row=gapsDescriptionsTable.getSelectedRow();
				final int modelRow=gapsDescriptionsTable.convertRowIndexToModel(row);				
				//final int modelRow=(int)Double.valueOf(instanceTableModel.getValueAt(row,0).toString()).doubleValue();
				//System.out.println(row+" "+modelRow);

				final String attrname=instanceTableModel.getValueAt(modelRow,1).toString();
				final Attribute attr=dataSet.attribute(attrname);
				final int gapsize=(int)Double.valueOf(instanceTableModel.getValueAt(modelRow,5).toString()).doubleValue();
				final int position=(int)Double.valueOf(instanceTableModel.getValueAt(modelRow,6).toString()).doubleValue(); 														
				
				if (!e.isPopupTrigger())
				{
					// nothing?
				}
				else
				{
					final JPopupMenu jPopupMenu=new JPopupMenu("feur");

					final JMenuItem interactiveFillMenuItem=new JMenuItem("Fill this gap (interactively)");
					interactiveFillMenuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							final GapFillingFrame jxf=new GapFillingFrame(atv,dataSet,attr,dateIdx,GapsUtil.getCountOfValuesBeforeAndAfter(gapsize),position,gapsize,gcp,false);
							jxf.setSize(new Dimension(900,700));
							//jxf.setExtendedState(Frame.MAXIMIZED_BOTH);								
							jxf.setLocationRelativeTo(jPopupMenu);
							jxf.setVisible(true);						
							//jxf.setResizable(false);
						}
					});
					jPopupMenu.add(interactiveFillMenuItem);													              		             		                									
					
					final JMenuItem lookupInKnowledgeDBMenuItem=new JMenuItem("Show the most similar cases from KnowledgeDB");
					lookupInKnowledgeDBMenuItem.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							final double x=gcp.getCoordinates(attrname)[0];
							final double y=gcp.getCoordinates(attrname)[1];
							final String season=instanceTableModel.getValueAt(modelRow,3).toString().split("/")[0];
							final boolean isDuringRising=instanceTableModel.getValueAt(modelRow,11).toString().equals("true");
							try 
							{
								final Calendar cal=Calendar.getInstance();
								final String dateAsString=instanceTableModel.getValueAt(modelRow,2).toString().replaceAll("'","");
								cal.setTime(FormatterUtil.DATE_FORMAT.parse(dateAsString));
								final int year=cal.get(Calendar.YEAR);
								
								new SimilarCasesFrame(dataSet,dateIdx,gcp,attrname,gapsize,position,x,y,year,season,isDuringRising);
							} 
							catch (Exception e1) 
							{									
								e1.printStackTrace();
							}
						}
					});
					jPopupMenu.add(lookupInKnowledgeDBMenuItem);												
					
					final JMenuItem mExport=new JMenuItem("Export this table as CSV");
					mExport.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							final JFileChooser fc = new JFileChooser();
							fc.setAcceptAllFileFilterUsed(false);
							final int returnVal = fc.showSaveDialog(gapsDescriptionsTable);
							if (returnVal == JFileChooser.APPROVE_OPTION) 
							{
								try 
								{
									final File file=fc.getSelectedFile();
									WekaDataAccessUtil.saveInstancesIntoCSVFile(gapsDescriptionsDataset,file);
								} 
								catch (Exception ee) 
								{
									ee.printStackTrace();
								}
							} 							
						}
					});
					jPopupMenu.add(mExport);	
					
					jPopupMenu.show(gapsDescriptionsTable,e.getX(),e.getY());
				}
			}
		});	 
		
		
		final int tableWidth=(int)gapsDescriptionsTable.getPreferredSize().getWidth()+30;
		final JScrollPane scrollPane=new JScrollPane(gapsDescriptionsTable);
		scrollPane.setPreferredSize(new Dimension(Math.min(tableWidth,500), 500));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);			
		this.tablePanel.removeAll();			
		this.tablePanel.add(scrollPane,BorderLayout.CENTER);
		
		this.visualOverviewPanel.removeAll();
					
		/* automatically compute the most similar series and the 'rising' flag */
		new AbstractSimpleAsync<Void>(false)
		{
			@Override
			public Void execute() throws Exception 
			{	
				final int rc=gapsDescriptionsTableModel.getRowCount();
				for (int i=0;i<rc;i++)
				{
					final int modelRow=i;
					
					try 
					{														
						final String attrname=gapsDescriptionsTableModel.getValueAt(modelRow,1).toString();
						final Attribute attr=dataSet.attribute(attrname);
						final int gapsize=(int)Double.valueOf(gapsDescriptionsTableModel.getValueAt(modelRow,5).toString()).doubleValue();
						final int position=(int)Double.valueOf(gapsDescriptionsTableModel.getValueAt(modelRow,6).toString()).doubleValue(); 														

						/* most similar */
						gapsDescriptionsTableModel.setValueAt("...",modelRow,7);
						final int cvba=GapsUtil.getCountOfValuesBeforeAndAfter(gapsize);
						Instances gapds=WekaDataProcessingUtil.buildFilteredDataSet(dataSet,
												0,
												dataSet.numAttributes()-1,
												Math.max(0,position-cvba),
												Math.min(position+gapsize+cvba,dataSet.numInstances()-1));
						final String mostsimilar=WekaTimeSeriesSimilarityUtil.findMostSimilarTimeSerie(gapds,attr,WekaTimeSeriesUtil.getNamesOfAttributesWithoutGap(gapds),false);
						gapsDescriptionsTableModel.setValueAt(mostsimilar,modelRow,7);							
						
						/* 'rising' flag */
						gapsDescriptionsTableModel.setValueAt("...",modelRow,11);
						final List<String> attributeNames=WekaDataStatsUtil.getAttributeNames(dataSet);
						attributeNames.remove("timestamp");
						final String nearestStationName=gcp.findNearestStation(attr.name(),attributeNames);
						final Attribute nearestStationAttr=dataSet.attribute(nearestStationName);
						//System.out.println(nearestStationName+" "+nearestStationAttr);
						final boolean isDuringRising=GapsUtil.isDuringRising(dataSet,position,gapsize,new int[]{dateIdx,attr.index(),nearestStationAttr.index()});
						gapsDescriptionsTableModel.setValueAt(isDuringRising,modelRow,11);
						
						gapsDescriptionsTableModel.fireTableDataChanged();
					} 
					catch (Exception e) 
					{	
						gapsDescriptionsTableModel.setValueAt("n/a",modelRow,7);
						gapsDescriptionsTableModel.setValueAt("n/a",modelRow,11);
						e.printStackTrace();
					}
				}
				return null;
			}

			@Override
			public void onSuccess(Void result) {}

			@Override
			public void onFailure(Throwable caught) 
			{
				caught.printStackTrace();
			}
			
		}.start();								

		/* select the first row */
		gapsDescriptionsTable.setRowSelectionInterval(0, 0);
	}
	
}
