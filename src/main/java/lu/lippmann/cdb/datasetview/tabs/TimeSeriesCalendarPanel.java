/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.gui.ColorHelper;
import lu.lippmann.cdb.lab.timeseries.*;
import lu.lippmann.cdb.lab.timeseries.MonthCalendarView.Mode;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.*;
import weka.core.*;


/**
 * TimeSeriesCalendarPanel.
 * 
 * @author the WP1 team
 */
public final class TimeSeriesCalendarPanel 
{	
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	/** */
	private final Color[] colors;
	/** */
	private final int firstColorIdx;
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public TimeSeriesCalendarPanel()
	{	
		this.jxp=new JXPanel();		
		this.jxp.setLayout(new BorderLayout());		

		this.colors=ColorHelper.YIGnBu; // TODO: put this elsewhere
		this.firstColorIdx=2;
	}

	
	//
	// Instance methods
	//
	
	public Component getComponent() 
	{					
		return this.jxp;
	}

	public void refresh(final Instances dataSet,final int dateIdx)	
	{
		refresh(dataSet,dateIdx,dataSet.classIndex(),Mode.ONE_DAY_IN_TWO_PIECHARTS);
	}
	
	public void refresh(final Instances dataSet,final int dateIdx,final int attrToHighlightIdx,final Mode calendarMode)	
	{
		this.jxp.removeAll();		
		
		final SimpleDateFormat f=new SimpleDateFormat(dataSet.attribute(dateIdx).getDateFormat());
		
		final LinkedHashMap<Date,Color> map=new LinkedHashMap<Date,Color>();	
		final AttributeStats attributeStats=(attrToHighlightIdx<0)?null
																	:dataSet.attributeStats(attrToHighlightIdx);
		for (int i=0;i<dataSet.numInstances();i++)
		{
			//System.out.println(i+" "+dataSet.instance(i).value(dateIdx));
			final String val=dataSet.instance(i).stringValue(dateIdx);
			
			try 
			{
				final Date d=f.parse(val);
				if (attrToHighlightIdx<0)
				{
					map.put(d,Color.BLACK);
				}
				else if (dataSet.attribute(attrToHighlightIdx).isNominal())
				{					
					final int idxOfColor=((int)dataSet.instance(i).value(attrToHighlightIdx)*(this.colors.length-1-this.firstColorIdx))/attributeStats.nominalCounts.length;
					map.put(d,this.colors[idxOfColor+this.firstColorIdx]);
				}
				else
				{
					final double normalizedValue=(dataSet.instance(i).value(attrToHighlightIdx)-attributeStats.numericStats.min)/(attributeStats.numericStats.max-attributeStats.numericStats.min);
					final int idxOfColor=(int)(normalizedValue*(this.colors.length-1-this.firstColorIdx));
					//System.out.println(normalizedValue+" "+idxOfColor);
					map.put(d,this.colors[idxOfColor+this.firstColorIdx]);
				}
			} 
			catch (ParseException e) 
			{				
				e.printStackTrace();
			}
		}
		
		final JScrollPane scrollp=new JScrollPane(MonthCalendarView.buildMultPanel(map,calendarMode,(int)jxp.getSize().getWidth()),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		scrollp.setPreferredSize(new Dimension((int)jxp.getSize().getWidth()*95/100,(int)jxp.getSize().getHeight()*95/100));
		
		this.jxp.add(scrollp,BorderLayout.CENTER);
		
		if (attrToHighlightIdx>=0)
		{	
			final JXPanel legendPanel=new JXPanel();
			legendPanel.setBorder(new TitledBorder("Legend"));
			legendPanel.setBackground(Color.WHITE);
			legendPanel.setLayout(new GridLayout(0,1));

			if (dataSet.attribute(attrToHighlightIdx).isNominal())
			{
				int c=0;
				final Map<Object,Integer> pv=WekaDataStatsUtil.getNominalRepartition(dataSet,attrToHighlightIdx);			
				for (final Map.Entry<Object,Integer> entry:pv.entrySet())
				{				
					final JXLabel comp=new JXLabel(entry.getKey().toString());
					final int idxOfColor=(c*(this.colors.length-this.firstColorIdx))/attributeStats.nominalCounts.length;				
					comp.setForeground(this.colors[idxOfColor+this.firstColorIdx]);
					legendPanel.add(comp);
					c++;
				}				
			}
			else if (dataSet.attribute(attrToHighlightIdx).isNumeric())
			{
				final JXLabel compMin=new JXLabel("Min: "+attributeStats.numericStats.min);
				compMin.setForeground(this.colors[this.firstColorIdx]);
				legendPanel.add(compMin);
				final JXLabel compMax=new JXLabel("Max: "+attributeStats.numericStats.max);
				compMax.setForeground(this.colors[this.colors.length-1]);
				legendPanel.add(compMax);
			}
			this.jxp.add(legendPanel,BorderLayout.NORTH);
		}
		
		final JXPanel settingsPanel=new JXPanel();
		settingsPanel.setLayout(new GridLayout(1,0));
		final JComboBox calendarModeCombo=new JComboBox(Mode.values());
		calendarModeCombo.setBorder(new TitledBorder("Mode"));
		final JComboBox attrToHighlightCombo=new JComboBox(WekaDataStatsUtil.getAttributeNames(dataSet).toArray());
		attrToHighlightCombo.setBorder(new TitledBorder("Attribute to highlight"));
		
		calendarModeCombo.setSelectedItem(calendarMode);
		calendarModeCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				refresh(dataSet,dateIdx,attrToHighlightCombo.getSelectedIndex(),Mode.valueOf(calendarModeCombo.getSelectedItem().toString()));				
			}
		});
		settingsPanel.add(calendarModeCombo);
		
		attrToHighlightCombo.setSelectedIndex(attrToHighlightIdx);
		attrToHighlightCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				refresh(dataSet,dateIdx,attrToHighlightCombo.getSelectedIndex(),Mode.valueOf(calendarModeCombo.getSelectedItem().toString()));				
			}
		});
		settingsPanel.add(attrToHighlightCombo);
		this.jxp.add(settingsPanel,BorderLayout.SOUTH);
	}
}
