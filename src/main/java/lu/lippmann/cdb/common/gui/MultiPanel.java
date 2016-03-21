/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.apache.commons.collections15.map.ListOrderedMap;


/**
 * MultiPanel.
 * 
 * @author the WP1 team
 */
public final class MultiPanel extends JPanel  
{  
	//
	// Static fields
	//
	
	/** */
	private static final long serialVersionUID=144160778L;
	
	/** */
	private static final Color[] COLORS=new Color[100];
	static
	{	
		for (int i=0;i<ColorHelper.Pastel1.length;i++) COLORS[i]=ColorHelper.Pastel1[i];
		for (int i=ColorHelper.Pastel1.length;i<COLORS.length;i++) COLORS[i]=ColorHelper.getRandomBrightColor();
	}
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public MultiPanel(final ListOrderedMap<JComponent,Integer> mapPanels, final int w, final int h)
	{
		this(mapPanels,w,h,true);
	}
	
	/**
	 * Constructor.
	 */
	public MultiPanel(final ListOrderedMap<JComponent,Integer> mapPanels, final int w, final int h,final boolean withWeight){
		
		final int total = mapPanels.keySet().size();

		if (!withWeight) setLayout(new GridLayout(0,1));

		final int w2 = w - 10*total;
		final int h2 = h - 75;
		
		final Map<JComponent,Color> choosedColor = new HashMap<JComponent,Color>();
		
		int i=0;
		for(final JComponent p : mapPanels.keySet()){
			final Dimension size = new Dimension((int)(w2 * (mapPanels.get(p) / 100.0)),h2);
			p.setPreferredSize(size);
			choosedColor.put(p,COLORS[i]);
			p.setBackground(COLORS[i]);
			p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			add(p);
			i++;
		}
		
		if (withWeight)
		{
			/** add percents **/
			for(final JComponent p : mapPanels.keySet()){
				final int perc    = mapPanels.get(p);
				final int percent = (int)(w2 * (mapPanels.get(p) / 100.0));
				final Dimension size = new Dimension(percent,20);
				final JPanel arrow = new JPanel();
				arrow.setPreferredSize(size);
				final JLabel label = new JLabel(perc+"%");
				label.setForeground(choosedColor.get(p).darker());
				arrow.add(label);
				add(arrow); 
			}
		}
	}
	
	//
	// Static methods
	//
	
	public static final void main(String[] args)  {  
		int w = 1024, h = 768;
		
		JPanel panel1 = new JPanel();
		panel1.setToolTipText("Panel 1");
		JPanel panel2 = new JPanel();
		panel2.setToolTipText("Panel 2");
		JPanel panel3 = new JPanel();
		panel3.setToolTipText("Panel 3");
		JPanel panel4 = new JPanel();
		panel4.setToolTipText("Panel 4");
		JPanel panel5 = new JPanel();
		panel5.setToolTipText("Panel 5");
		
		final ListOrderedMap<JComponent,Integer> mapPanels = new ListOrderedMap<JComponent,Integer>();
		
		
		//add in increasing order or in the order you want to maintain ...
		mapPanels.put(panel4,5);
		mapPanels.put(panel5,5);
		mapPanels.put(panel3,10);
		mapPanels.put(panel1,30);
		mapPanels.put(panel2,50);
		
		MultiPanel mp = new MultiPanel(mapPanels,w,h,false);  
		JFrame f = new JFrame( "MultiPanel test" );  
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		f.setSize( w, h );  
		f.getContentPane().add(mp);  
		f.setVisible( true );  
	}  
}  