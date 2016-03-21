/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.timeseries;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import lu.lippmann.cdb.common.gui.*;

import org.jdesktop.swingx.*;


/**
 * MonthCalendarView.
 * 
 * @author the WP1 team
 */
public final class MonthCalendarView extends JXPanel 
{
	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=54321l;
	
	private static final SimpleDateFormat MONTH_FORMAT=new SimpleDateFormat("MMMM");

	private static final Map<String,LinkedHashMap<Date,Color>> CACHE=new HashMap<String,LinkedHashMap<Date,Color>>();
	
	
	//
	// Instance fields
	//
	
	/** */
	private final Date currentMonth;
	/** */
	private final LinkedHashMap<Date,Color> listOfDatesForThisMonth;
	/** */
	private final int month;
	/** */
	private final int year;
	/** */
	private final Mode mode;

	
	//
	// Constructors
	//
	
	/** 
	 * Constructor.
	 */
	public MonthCalendarView(final Date currentMonth,final LinkedHashMap<Date,Color> listOfDates,final Mode mode) 
	{
		this.currentMonth=currentMonth;

		final Calendar cal=Calendar.getInstance();
		cal.setTime(currentMonth);
		
		this.month=cal.get(Calendar.MONTH);
		this.year=cal.get(Calendar.YEAR);
		
		this.listOfDatesForThisMonth=filter(this.year,this.month,-1,listOfDates);
			
		this.mode=mode;
		
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
		final int size=getWidth()/10;	
		
		g.setFont(new Font("Arial",Font.PLAIN,10));
		//System.out.println(g.getFont());
		
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		g.drawString(MONTH_FORMAT.format(currentMonth), 34, 36);
		g.drawString(this.year+"", 235, 36);

		final Calendar tmpcal=Calendar.getInstance();
		tmpcal.setTime(currentMonth);
		tmpcal.set(Calendar.DATE, 1);
		tmpcal.add(Calendar.DATE, -tmpcal.get(Calendar.DAY_OF_WEEK) + 1);
		for (int week = 0; week < 6; week++) 
		{
			for (int d = 0; d < 7; d++) 
			{
				if ((tmpcal.get(Calendar.MONTH)==this.month))
				{
					final int dof=tmpcal.get(Calendar.DAY_OF_MONTH);
					final LinkedHashMap<Date,Color> listOfDatesForThisDay=filter(this.year,this.month,dof,this.listOfDatesForThisMonth);								

					if (mode.equals(Mode.ONE_DAY_IN_TWO_PIECHARTS))
					{
						final int x = d * (2*size + 10) + 23 + 4;
						final int y = week * (size + 10) + 40 + 20;
						g.drawString(dof+"",x,y);
						
						DayPieChartView.paintComponent0(g, x+2, y+2, size, listOfDatesForThisDay,DayPieChartView.DayPieChartViewMode.MORNING);
						DayPieChartView.paintComponent0(g, x+2+1+size, y+2, size, listOfDatesForThisDay,DayPieChartView.DayPieChartViewMode.AFTERNOON);
					}
					else if (mode.equals(Mode.ONE_DAY_IN_ONE_PIECHART))
					{
						final int x = d * (size + 10) + 23 + 4;
						final int y = week * (size + 10) + 40 + 20;
						g.drawString(dof+"",x,y);
						
						DayPieChartView.paintComponent0(g, x+2, y+2, size, listOfDatesForThisDay,DayPieChartView.DayPieChartViewMode.ALLDAY);						
					}
					else if (mode.equals(Mode.ONE_DAY_IN_ONE_LAYERED_PIECHART))
					{
						final int x = d * (size + 10) + 23 + 4;
						final int y = week * (size + 10) + 40 + 20;
						g.drawString(dof+"",x,y);
						
						DayPieChartView.paintComponent0(g,x+2,y+2,size,listOfDatesForThisDay,DayPieChartView.DayPieChartViewMode.AFTERNOON);
						DayPieChartView.paintComponent0(g,x+2+(size/4),y+2+(size/4),size/2,listOfDatesForThisDay,DayPieChartView.DayPieChartViewMode.MORNING);
					}
					else
					{
						throw new IllegalStateException();
					}
					
					g.setColor(Color.black);
				}
				// g.(x, y, width, height)
				tmpcal.add(Calendar.DATE, +1);
			}
		}
	}

	
	//
	// Static methods
	//
	
	public static final JXPanel buildMultPanel(final LinkedHashMap<Date,Color> map,final Mode mode,final int width)
	{		
		final int height2 = (width*7)/9;
		
		final Iterator<Map.Entry<Date,Color>> iter=map.entrySet().iterator();
		final Date first=iter.next().getKey();
		Date last=first;
		while (iter.hasNext()) last=iter.next().getKey();
		
		final Calendar firstMonthDate=Calendar.getInstance();
		firstMonthDate.setTime(first);
		final Calendar lastMonthDate=Calendar.getInstance();
		lastMonthDate.setTime(last);
		
		final JXPanel allPanel=new JXPanel()
		{
			{
				this.setScrollableHeightHint(ScrollableSizeHint.VERTICAL_STRETCH);
				this.setScrollableWidthHint(ScrollableSizeHint.FIT);
			}
		};
		allPanel.setLayout(new FlowLayout());
		allPanel.setBackground(Color.white);
		
		int c=0;		
		while (firstMonthDate.get(Calendar.MONTH)+(100*firstMonthDate.get(Calendar.YEAR))<=lastMonthDate.get(Calendar.MONTH)+(100*lastMonthDate.get(Calendar.YEAR)))
		{			
			final MonthCalendarView ecv=new MonthCalendarView(firstMonthDate.getTime(),map,mode);			
			ecv.setPreferredSize(new Dimension(width,height2)); // yes, magic number
			firstMonthDate.add(Calendar.MONTH,1);
			allPanel.add(ecv);
			c++;
		}
		allPanel.setPreferredSize(new Dimension(width,c*height2+20));
		
		return allPanel;
	}
	
	private static final String getCacheKey(final int year,final int month,final int day,final LinkedHashMap<Date,Color> map)
	{
		return year+" "+month+" "+day+" "+map.hashCode();
	}
	
	private static LinkedHashMap<Date,Color> filter(final int year,final int month,final int day,final LinkedHashMap<Date,Color> map) 
	{
		final String ck=getCacheKey(year,month,day,map);
		if (CACHE.containsKey(ck)) return CACHE.get(ck);
		
		//System.out.println("Filter for "+year+" "+month+" "+day+" -> "+map.size());
		final LinkedHashMap<Date,Color> filtered=new LinkedHashMap<Date,Color>();
		final Calendar tmpcal=Calendar.getInstance();
		for (final Map.Entry<Date,Color> entry:map.entrySet())
		{
			tmpcal.setTime(entry.getKey());
			if (day!=-1&&day!=tmpcal.get(Calendar.DAY_OF_MONTH)) continue;
			if (month!=tmpcal.get(Calendar.MONTH)) continue;
			if (year!=tmpcal.get(Calendar.YEAR)) continue;
			filtered.put(entry.getKey(),entry.getValue());
		}
		CACHE.put(ck,filtered);
		return filtered;
	}

	
	//
	// Inner enums
	//
	
	public static enum Mode
	{
		ONE_DAY_IN_TWO_PIECHARTS,ONE_DAY_IN_ONE_PIECHART,ONE_DAY_IN_ONE_LAYERED_PIECHART;
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
		frame.setTitle("Calendar test");
		LogoHelper.setLogo(frame);
		frame.setPreferredSize(new Dimension(700,600));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		final LinkedHashMap<Date,Color> map=new LinkedHashMap<Date,Color>();	
		for (int i=-100000;i<0;i++)
		{
			final Calendar t1=Calendar.getInstance();
			t1.add(Calendar.MINUTE,i);
			map.put(t1.getTime(),(Math.random()<0.2)?Color.GREEN:Color.RED);
		}
				
		final int width=680;
		final Component mp=buildMultPanel(map,Mode.ONE_DAY_IN_ONE_LAYERED_PIECHART,width);
		final JScrollPane scrollp=new JScrollPane(mp,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollp.setPreferredSize(new Dimension(width,550));
		
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(scrollp);

		frame.pack();
		frame.setVisible(true);
	}
}
