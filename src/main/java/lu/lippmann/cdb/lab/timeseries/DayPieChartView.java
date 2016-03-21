/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.timeseries;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import lu.lippmann.cdb.common.gui.LogoHelper;

import org.jdesktop.swingx.*;


/**
 * DayPieChartView.
 * Can build:
 *  - a classic piechart,
 *  - a layered piechart (external layer is afternoon, internal layer is morning),
 *  - two piecharts, like a 'double clock' (one for morning, one for afternoon).
 * 
 * @author the WP1 team
 */
public final class DayPieChartView extends JXPanel 
{
	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=12345l;
	

	//
	// Instance fields
	//
	
	/** */
	private final LinkedHashMap<Date,Color> colorsByDates;

	
	//
	// Constructors
	//
	
	/** 
	 * Constructor.
     * For a given instance, all the dates should be related to the same day!
     * This constrainst is not checked in the method for performances reasons.
	 */
	public DayPieChartView(final LinkedHashMap<Date,Color> colorsByDates) 
	{
		this.colorsByDates=colorsByDates;

		this.setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
		this.setScrollableWidthHint(ScrollableSizeHint.FIT);
	}

	
	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paintComponent(final Graphics g) 
	{
		final int size=getWidth()/2;
		paintComponent0(g,0,0,size,this.colorsByDates,DayPieChartViewMode.MORNING);
		paintComponent0(g,size,0,size,this.colorsByDates,DayPieChartViewMode.AFTERNOON);
	}
	
	
	//
	// Static methods
	//
	
	public static void paintComponent0(final Graphics g,final int x,final int y,final int size,final LinkedHashMap<Date,Color> colorsByDates,final DayPieChartViewMode mode) 
	{	
		if (colorsByDates.isEmpty()) return;
		//else System.out.println("Draw for "+colorsByDates);			
		
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		//g.setColor(Color.white);
		//g.fillRect(x, y, size, size);
		//g.setColor(Color.GRAY);
		//g.drawRect(x, y, size, size);
		g.setColor(Color.black);
		
		final Calendar cal=Calendar.getInstance();
		cal.setTime(colorsByDates.entrySet().iterator().next().getKey());
		//d.getTime();
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		final long millisecAtBeginOfTheDay=cal.getTimeInMillis();
		cal.add(Calendar.DAY_OF_YEAR,1);
		final long millisecAtEndOfTheDay=cal.getTimeInMillis();
		
		final long millisecAtHalfOfTheDay=millisecAtBeginOfTheDay+((millisecAtEndOfTheDay-millisecAtBeginOfTheDay)/2);
		
		//g.setColor(Color.LIGHT_GRAY);
		//g.fillOval(x,y,size,size);
		
		for (final Map.Entry<Date,Color> entry:colorsByDates.entrySet())
		{
			final Date d=entry.getKey();
			cal.setTime(d);
			final long millisec=cal.getTimeInMillis();
			
			if (mode.equals(DayPieChartViewMode.MORNING)&&millisec>millisecAtHalfOfTheDay) continue;
			if (mode.equals(DayPieChartViewMode.AFTERNOON)&&millisec<millisecAtHalfOfTheDay) continue;			
			
			final double percent;
			if (mode.equals(DayPieChartViewMode.MORNING))
			{
				percent=(double)(millisec-millisecAtBeginOfTheDay)/(double)(millisecAtHalfOfTheDay-millisecAtBeginOfTheDay);
			}
			else if (mode.equals(DayPieChartViewMode.AFTERNOON))
			{
				percent=(double)(millisec-millisecAtHalfOfTheDay)/(double)(millisecAtEndOfTheDay-millisecAtHalfOfTheDay);
			}
			else if (mode.equals(DayPieChartViewMode.ALLDAY))
			{
				percent=(double)(millisec-millisecAtBeginOfTheDay)/(double)(millisecAtEndOfTheDay-millisecAtBeginOfTheDay);				
			}
			else
			{
				throw new IllegalStateException();
			}
			/*System.out.println("begin\t"+millisecAtBeginOfTheDay);
			System.out.println("end\t"+millisecAtEndOfTheDay);
			System.out.println("current\t"+millisec);
			System.out.println(percent+" %");
			System.out.println("current diff\t"+(millisec-millisecAtBeginOfTheDay));
			System.out.println("max diff\t"+(millisecAtEndOfTheDay-millisecAtBeginOfTheDay));
			System.out.println();*/
			g.setColor(entry.getValue());
			final int angle=(int)(360*(1d-percent));
			g.fillArc(x, y, size, size, angle+90, 1);
		}
	}
	
	
	//
	// Inner enums
	//
	
	public static enum DayPieChartViewMode
	{
		MORNING,AFTERNOON,ALLDAY;
	}
	
	
	//
	// Main method
	//
	
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(String[] args) 
	{
		final JFrame frame = new JFrame();
		frame.setTitle("Day pie chart");
		LogoHelper.setLogo(frame);
		frame.setPreferredSize(new Dimension(350, 600));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		final LinkedHashMap<Date,Color> map=new LinkedHashMap<Date,Color>();
		final Calendar t1=Calendar.getInstance();
		for (int i=0;i<300;i++)
		{			
			t1.add(Calendar.MINUTE,1);
			map.put(t1.getTime(),(Math.random()<0.2)?Color.GREEN:Color.RED);
		}
		
		frame.getContentPane().add(new DayPieChartView(map));

		frame.pack();
		frame.setVisible(true);
	}
	
	

}
