/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import lu.lippmann.cdb.common.ArraysUtil;
import lu.lippmann.cdb.common.gui.ts.TimeSeriesChartUtil;
import lu.lippmann.cdb.ext.hydviga.gaps.*;
import lu.lippmann.cdb.ext.hydviga.util.GapsUtil;
import lu.lippmann.cdb.weka.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.ui.*;
import weka.core.*;


/**
 * GapsUIUtil.
 * 
 * @author the HYDVIGA team
 */
public class GapsUIUtil 
{
	//
	// Constructors
	// 
	
	/**
	 * Private constructor.
	 */
	private GapsUIUtil() {}
	
	
	//
	// Static methods
	//
	
	public static ChartPanel buildGapChartPanel(final Instances dataSet,final int dateIdx,final Attribute attr,final int gapsize,final int position) throws Exception 
	{
		Instances filteredDs=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSet,new int[]{attr.index(),dateIdx});									
		filteredDs=WekaDataProcessingUtil.buildFilteredDataSet(filteredDs,
																0,
																filteredDs.numAttributes()-1,
																Math.max(0,position-GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize),
																Math.min(position+gapsize+GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize,filteredDs.numInstances()-1));
		
		final ChartPanel cp=TimeSeriesChartUtil.buildChartPanelForAllAttributes(filteredDs,false,WekaDataStatsUtil.getFirstDateAttributeIdx(filteredDs),null);
		
		final XYPlot xyp=(XYPlot)cp.getChart().getPlot();
		xyp.getDomainAxis().setLabel("");
		xyp.getRangeAxis().setLabel("");
		
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
		
		addExportPopupMenu(filteredDs,cp);
		
		return cp;
	}
	
	public static ChartPanel buildGapChartPanelWithCorrection(final Instances pdataSet,final int dateIdx,final Attribute attr,final int gapsize,final int position,final GapFiller gapFiller,final java.util.Collection<String> attrs) throws Exception 
	{
		final Instances dataSetWithTheGap=new Instances(pdataSet);
		for (int i=position;i<position+gapsize;i++) dataSetWithTheGap.instance(i).setMissing(attr);
		
		int[] arr=new int[]{attr.index(),dateIdx};
		for (final String sss:attrs)
		{
			arr=ArraysUtil.concat(arr,new int[]{dataSetWithTheGap.attribute(sss).index()});
		}

		
		Instances filteredDsWithTheGap=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(dataSetWithTheGap,arr);									
		filteredDsWithTheGap=WekaDataProcessingUtil.buildFilteredDataSet(filteredDsWithTheGap,
																0,
																filteredDsWithTheGap.numAttributes()-1,
																Math.max(0,position-GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize),
																Math.min(position+gapsize+GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize,filteredDsWithTheGap.numInstances()-1));
		
		final Instances completedds=gapFiller.fillGaps(filteredDsWithTheGap);			
		final Instances diff=WekaTimeSeriesUtil.buildDiff(filteredDsWithTheGap,completedds);
		
		Instances filteredDsWithoutTheGap=WekaDataProcessingUtil.buildFilteredByAttributesDataSet(pdataSet,arr);									
		filteredDsWithoutTheGap=WekaDataProcessingUtil.buildFilteredDataSet(filteredDsWithoutTheGap,
																0,
																filteredDsWithoutTheGap.numAttributes()-1,
																Math.max(0,position-GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize),
																Math.min(position+gapsize+GapsUtil.VALUES_BEFORE_AND_AFTER_RATIO*gapsize,filteredDsWithoutTheGap.numInstances()-1));
		
		
		diff.insertAttributeAt(new Attribute(attr.name()+"_orig"),diff.numAttributes());
		for (int i=0;i<filteredDsWithoutTheGap.numInstances();i++) 
		{	
			diff.instance(i).setValue(diff.numAttributes()-1,filteredDsWithoutTheGap.instance(i).value(filteredDsWithoutTheGap.attribute(attr.name())));
		}
		//System.out.println(attr.name()+"\n"+diff.toSummaryString());
		
		final java.util.List<String> toRemove=new java.util.ArrayList<String>();
		for (int j=0;j<diff.numAttributes();j++) 
		{	
			final String consideredAttrName=diff.attribute(j).name();
			if (!consideredAttrName.contains("timestamp")&&!consideredAttrName.contains(attr.name())) toRemove.add(consideredAttrName);
		}
		diff.setClassIndex(-1);
		for (final String ssss:toRemove) diff.deleteAttributeAt(diff.attribute(ssss).index());
		//System.out.println(attr.name()+"\n"+diff.toSummaryString());
		
		
		final ChartPanel cp=TimeSeriesChartUtil.buildChartPanelForAllAttributes(diff,false,WekaDataStatsUtil.getFirstDateAttributeIdx(diff),null);
		
		final XYPlot xyp=(XYPlot)cp.getChart().getPlot();
		xyp.getDomainAxis().setLabel("");
		xyp.getRangeAxis().setLabel("");
		
		final Marker gapBeginMarker=new ValueMarker(dataSetWithTheGap.instance(Math.max(0,position-1)).value(dateIdx));
		gapBeginMarker.setPaint(Color.RED);
		gapBeginMarker.setLabel("Gap begin");
		gapBeginMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		gapBeginMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		cp.getChart().getXYPlot().addDomainMarker(gapBeginMarker);
		
		final Marker gapEndMarker=new ValueMarker(dataSetWithTheGap.instance(Math.min(dataSetWithTheGap.numInstances()-1,position+gapsize)).value(dateIdx));
		gapEndMarker.setPaint(Color.RED);
		gapEndMarker.setLabel("Gap end");
		gapEndMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		gapEndMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		cp.getChart().getXYPlot().addDomainMarker(gapEndMarker);				
		
		addExportPopupMenu(diff,cp);
		
		return cp;
	}

	private static void addExportPopupMenu(final Instances ds,final ChartPanel cp) 
	{
		cp.addChartMouseListener(new ChartMouseListener() 
		{
		    public void chartMouseClicked(ChartMouseEvent e) 
		    {
				final JPopupMenu jPopupMenu=new JPopupMenu("feur");
				final JMenuItem mi1=new JMenuItem("Export as CSV");
				mi1.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e) 
					{
						final JFileChooser fc=new JFileChooser();
						fc.setAcceptAllFileFilterUsed(false);
						final int returnVal=fc.showSaveDialog(cp);
						if (returnVal==JFileChooser.APPROVE_OPTION) 
						{
							try 
							{
								final File file=fc.getSelectedFile();
								WekaDataAccessUtil.saveInstancesIntoCSVFile(ds,file);
							} 
							catch (final Exception ee) 
							{
								ee.printStackTrace();
							}
						} 							
					}
				});
				jPopupMenu.add(mi1);													              		             		                																													
				jPopupMenu.show(cp,e.getTrigger().getX(),e.getTrigger().getY());
		    }

		    public void chartMouseMoved(ChartMouseEvent e) {}
		});
	}	
}
