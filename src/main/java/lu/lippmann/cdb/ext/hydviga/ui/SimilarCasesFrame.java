/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import lu.lippmann.cdb.common.gui.LogoHelper;
import lu.lippmann.cdb.common.gui.dataset.InstanceTableModel;
import lu.lippmann.cdb.ext.hydviga.cbr.GapFillingKnowledgeDB;
import lu.lippmann.cdb.ext.hydviga.data.StationsDataProvider;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jfree.chart.ChartPanel;
import weka.core.Instances;


/**
 * SimilarCasesFrame.
 * 
 * @author the HYDVIGA team
 */
public final class SimilarCasesFrame extends JXFrame 
{
	//
	// Static fields
	//

	/** */
	private static final long serialVersionUID=1L;
	
	/** */
	private static final int COMPONENT_WIDTH=900;	
	/** */
	private static final int FRAME_WIDTH=(int)(COMPONENT_WIDTH*1.1);
	/** */
	private static final Dimension CHART_DIMENSION=new Dimension(COMPONENT_WIDTH,225);

	
	//
	// Instance fields
	//
	
	/** */
	private JXPanel inputCaseTablePanel;
	/** */
	private JXPanel inputCaseChartPanel;
	/** */
	private JXPanel outputCasesTablePanel;
	/** */
	private JXPanel outputCasesChartPanel;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	SimilarCasesFrame(final Instances ds,final int dateIdx,final StationsDataProvider gcp,String attrname,final int gapsize,final int position,final double x,final double y,final int year,final String season,final boolean isDuringRising) throws Exception
	{
		LogoHelper.setLogo(this);
		this.setTitle("KnowledgeDB: Suggested configurations / similar cases");
		
		this.inputCaseTablePanel=new JXPanel();
		this.inputCaseTablePanel.setBorder(new TitledBorder("Present case"));
		this.inputCaseChartPanel=new JXPanel();
		this.inputCaseChartPanel.setBorder(new TitledBorder("Profile of the present case"));
		this.outputCasesTablePanel=new JXPanel();
		this.outputCasesTablePanel.setBorder(new TitledBorder("Suggested cases"));
		this.outputCasesChartPanel=new JXPanel();
		this.outputCasesChartPanel.setBorder(new TitledBorder("Profile of the selected suggested case"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
		getContentPane().add(inputCaseTablePanel);
		getContentPane().add(inputCaseChartPanel);
		getContentPane().add(outputCasesTablePanel);
		getContentPane().add(outputCasesChartPanel);
		
		final Instances res=GapFillingKnowledgeDB.findSimilarCases(attrname,
																		x,
																		y,
																		year,
																		season,
																		gapsize,
																		position,
																		isDuringRising,
																		gcp.findDownstreamStation(attrname)!=null,
																		gcp.findUpstreamStation(attrname)!=null,
																		GapsUtil.measureHighMiddleLowInterval(ds,ds.attribute(attrname).index(),position-1));
		
		final Instances inputCase=new Instances(res);
		while (inputCase.numInstances()>1) inputCase.remove(1);
		final JXTable inputCaseTable=buidJXTable(inputCase);		
		final JScrollPane inputScrollPane=new JScrollPane(inputCaseTable);
		//System.out.println(inputScrollPane.getPreferredSize());
		inputScrollPane.setPreferredSize(new Dimension(COMPONENT_WIDTH,(int)(50+inputScrollPane.getPreferredSize().getHeight())));
		this.inputCaseTablePanel.add(inputScrollPane);
		
		final ChartPanel inputcp=GapsUIUtil.buildGapChartPanel(ds,dateIdx,ds.attribute(attrname),gapsize,position);
		inputcp.getChart().removeLegend();		
		inputcp.setPreferredSize(CHART_DIMENSION);
		this.inputCaseChartPanel.add(inputcp);
		
		final Instances outputCases=new Instances(res);
		outputCases.remove(0);				
		final JXTable outputCasesTable=buidJXTable(outputCases);
		final JScrollPane outputScrollPane=new JScrollPane(outputCasesTable);
		outputScrollPane.setPreferredSize(new Dimension(COMPONENT_WIDTH,(int)(50+outputScrollPane.getPreferredSize().getHeight())));
		this.outputCasesTablePanel.add(outputScrollPane);
		
		outputCasesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		outputCasesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
	    {
            @Override
            public void valueChanged(ListSelectionEvent e) 
            {
                if (!e.getValueIsAdjusting()) 
                {
                	final int modelRow=outputCasesTable.getSelectedRow();
                	
    				final String attrname=outputCasesTable.getModel().getValueAt(modelRow,1).toString();
    				final int gapsize=(int)Double.valueOf(outputCasesTable.getModel().getValueAt(modelRow,4).toString()).doubleValue();
    				final int position=(int)Double.valueOf(outputCasesTable.getModel().getValueAt(modelRow,5).toString()).doubleValue(); 														
                	
					try 
					{
						final ChartPanel cp=GapsUIUtil.buildGapChartPanel(ds,dateIdx,ds.attribute(attrname),gapsize,position);
						cp.getChart().removeLegend();
						cp.setPreferredSize(CHART_DIMENSION);
						outputCasesChartPanel.removeAll();
						outputCasesChartPanel.add(cp);
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
		
		outputCasesTable.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(final MouseEvent e)
			{
				final InstanceTableModel instanceTableModel=(InstanceTableModel)outputCasesTable.getModel();
				final int row=outputCasesTable.rowAtPoint(e.getPoint());
				final int modelRow=outputCasesTable.convertRowIndexToModel(row);

				final String attrname=instanceTableModel.getValueAt(modelRow,1).toString();
				final int gapsize=(int)Double.valueOf(instanceTableModel.getValueAt(modelRow,4).toString()).doubleValue();
				final int position=(int)Double.valueOf(instanceTableModel.getValueAt(modelRow,5).toString()).doubleValue(); 														
				
				if (e.isPopupTrigger())
				{
					final JPopupMenu jPopupMenu=new JPopupMenu("feur");

					final JMenuItem mi=new JMenuItem("Use this configuration");
					mi.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(final ActionEvent e) 
						{
							System.out.println("not implemented!");
						}
					});
					jPopupMenu.add(mi);													              		             		                															
					jPopupMenu.show(outputCasesTable,e.getX(),e.getY());						
				}
				else
				{
					// nothing?
				}
			}
		});		
		
		setPreferredSize(new Dimension(FRAME_WIDTH,900));
		
		pack();
		setVisible(true);									

		/* select the first row */
		outputCasesTable.setRowSelectionInterval(0, 0);
	}


	private JXTable buidJXTable(final Instances cases) 
	{
		final InstanceTableModel outputCasesTableModel=new InstanceTableModel();
		outputCasesTableModel.setDataset(cases);
		final JXTable outputCasesTable=new JXTable(outputCasesTableModel);
		outputCasesTable.setEditable(false);
		outputCasesTable.setShowHorizontalLines(false);
		outputCasesTable.setShowVerticalLines(false);
		outputCasesTable.setVisibleRowCount(Math.min(10,cases.numInstances())); // FIXME: hardcoded trick
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
