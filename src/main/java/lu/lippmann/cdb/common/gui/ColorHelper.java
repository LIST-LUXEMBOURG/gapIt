/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.common.ArraysUtil;

import org.jdesktop.swingx.JXPanel;


/**
 * ColorHelper.
 *
 * @author the WP1 team
 */
public final class ColorHelper 
{
	//
	// Static fields
	//
	
	/** */
	public static final Color[] WHITE_TO_BLACK_256_COLORS=new Color[256];
	static
	{
		for (int i=0;i<WHITE_TO_BLACK_256_COLORS.length;i++) WHITE_TO_BLACK_256_COLORS[i]=new Color(255-i,255-i,255-i);
	}
	
	public static final Color[] BLUE_BRIGHT_TO_DARK_256_COLORS = new Color[256];
	static 
	{
		for (int i=0;i<BLUE_BRIGHT_TO_DARK_256_COLORS.length;i++) BLUE_BRIGHT_TO_DARK_256_COLORS[i]=new Color(0,127-(i/2),255-(i/2));
	}
	
	/** */
	public static final Color[] YlGnBu_9_COLORS=new Color[9];	
	static
	{
		YlGnBu_9_COLORS[0]=new Color(255,255,217);
		YlGnBu_9_COLORS[1]=new Color(237,248,177);
		YlGnBu_9_COLORS[2]=new Color(199,233,180);
		YlGnBu_9_COLORS[3]=new Color(127,205,187);
		YlGnBu_9_COLORS[4]=new Color(65,182,196);
		YlGnBu_9_COLORS[5]=new Color(29,145,192);
		YlGnBu_9_COLORS[6]=new Color(34,94,168);
		YlGnBu_9_COLORS[7]=new Color(37,52,148);
		YlGnBu_9_COLORS[8]=new Color(8,2,88);
	}

	/** */
	public static final Color[] Divergent_10_COLORS=new Color[10];	
	static
	{
		Divergent_10_COLORS[0]=new Color(127,59,8);
		Divergent_10_COLORS[1]=new Color(179,88,6);
		Divergent_10_COLORS[2]=new Color(224,130,20);
		Divergent_10_COLORS[3]=new Color(253,184,99);
		Divergent_10_COLORS[4]=new Color(254,224,182);
		Divergent_10_COLORS[5]=new Color(216,218,235);
		Divergent_10_COLORS[6]=new Color(178,171,210);
		Divergent_10_COLORS[7]=new Color(128,115,172);
		Divergent_10_COLORS[8]=new Color(84,39,136);
		Divergent_10_COLORS[9]=new Color(45,0,75);
	}
	
	/** */
	public static final Color[] Divergent_6_COLORS=new Color[6];	
	static
	{
		Divergent_6_COLORS[0]=new Color(152,78,163);
		Divergent_6_COLORS[1]=new Color(77,175,184);
		Divergent_6_COLORS[2]=new Color(255,127,0);
		Divergent_6_COLORS[3]=new Color(231,41,138);
		Divergent_6_COLORS[4]=new Color(237,248,177);
		Divergent_6_COLORS[5]=new Color(166,86,40);
	}
	
	//Palettes from ColorBrewer
	//Qualitative
	public static final Color[] Accent = new Color[]{	
		new Color(127,201,127),
		new Color(190,174,212),
		new Color(253,192,134),
		new Color(255,255,153),
		new Color(56,108,176),
		new Color(240,2,127),
		new Color(191,91,23),
		new Color(102,102,102)
	 };
	
	public static final Color[] Dark2 = new Color[]{
		new Color(27,158,119),
		new Color(217,95,2),
		new Color(117,112,179),
		new Color(231,41,138),
		new Color(102,166,30),
		new Color(230,171,2),
		new Color(166,118,29),
		new Color(102,102,102)
	};
	
	public static final Color[] Paired = new Color[]{		
		new Color(166,206,227),
		new Color(31,120,180),
		new Color(178,223,138),
		new Color(51,160,44),
		new Color(251,154,153),
		new Color(227,26,28),
		new Color(253,191,111),
		new Color(255,127,0),
		new Color(202,178,214),
		new Color(106,61,154),
		new Color(255,255,153),
		new Color(177,89,40)
	};
	
	public static final Color[] Pastel1 = new Color[]{
		new Color(251,180,174),
		new Color(179,205,227),
		new Color(204,235,197),
		new Color(222,203,228),
		new Color(254,217,166),
		new Color(255,255,204),
		new Color(229,216,189),
		new Color(253,218,236),
		new Color(242,242,242)
	};
	
	public static final Color[] Pastel2  = new Color[]{
		new Color(179,226,205),
		new Color(253,205,172),
		new Color(203,213,232),
		new Color(244,202,228),
		new Color(230,245,201),
		new Color(255,242,174),
		new Color(241,226,204),
		new Color(204,204,204)
	};	
	
	public static final Color[] Set1 = new Color[]{
		new Color(228,26,28),
		new Color(55,126,184),
		new Color(77,175,74),
		new Color(152,78,163),
		new Color(255,127,0),
		new Color(255,255,51),
		new Color(166,86,40),
		new Color(247,129,191),
		new Color(153,153,153)
	};
	
	public static final Color[] Set2 = new Color[]{
		new Color(102,194,165),
		new Color(252,141,98),
		new Color(141,160,203),
		new Color(231,138,195),
		new Color(166,216,84),
		new Color(255,217,47),
		new Color(229,196,148),
		new Color(179,179,179)
	};
	
	public static final Color[] Set3 = new Color[]{
		new Color(141,211,199),
		new Color(255,255,179),
		new Color(190,186,218),
		new Color(251,128,114),
		new Color(128,177,211),
		new Color(253,180,98),
		new Color(179,222,105),
		new Color(252,205,229),
		new Color(217,217,217),
		new Color(188,128,189),
		new Color(204,235,197),
		new Color(255,237,111)
	};
	
	//Sequential
	public static final Color Blues[] = new Color[]{
		new Color(247,251,255),
		new Color(222,235,247),
		new Color(198,219,239),
		new Color(158,202,225),
		new Color(107,174,214),
		new Color(66,146,198),
		new Color(33,113,181),
		new Color(8,81,156),
		new Color(8,48,107)
	};
		
	public static final Color BuGn[] = new Color[]{
		new Color(247,252,253),
		new Color(229,245,249),
		new Color(204,236,230),
		new Color(153,216,201),
		new Color(102,194,164),
		new Color(65,174,118),
		new Color(35,139,69),
		new Color(0,109,44),
		new Color(0,68,27)
	};
	
	public static final Color BuPu[] = new Color[]{
		new Color(247,252,253),
		new Color(224,236,244),
		new Color(191,211,230),
		new Color(158,188,218),
		new Color(140,150,198),
		new Color(140,107,177),
		new Color(136,65,157),
		new Color(129,15,124),
		new Color(77,0,75)
	};
	
	public static final Color GnBu[] = new Color[]{
		new Color(247,252,240),
		new Color(224,243,219),
		new Color(204,235,197),
		new Color(168,221,181),
		new Color(123,204,196),
		new Color(78,179,211),
		new Color(43,140,190),
		new Color(8,104,172),
		new Color(8,64,129)	
	};
	
	public static final Color Greens[] = new Color[]{
		new Color(247,252,245),
		new Color(229,245,224),
		new Color(199,233,192),
		new Color(161,217,155),
		new Color(116,196,118),
		new Color(65,171,93),
		new Color(35,139,69),
		new Color(0,109,44),
		new Color(0,68,27)
	};
	
	public static final Color Greys[] = new Color[]{
		new Color(255,255,255),
		new Color(240,240,240),
		new Color(217,217,217),
		new Color(189,189,189),
		new Color(150,150,150),
		new Color(115,115,115),
		new Color(82,82,82),
		new Color(37,37,37),
		new Color(0,0,0)
	};
	
	public static final Color Oranges[] = new Color[]{
		new Color(255,245,235),
		new Color(254,230,206),
		new Color(253,208,162),
		new Color(253,174,107),
		new Color(253,141,60),
		new Color(241,105,19),
		new Color(217,72,1),
		new Color(166,54,3),
		new Color(127,39,4)
	};
	
	public static final Color OrRd[] = new Color[]{
		new Color(255,247,236),
		new Color(254,232,200),
		new Color(253,212,158),
		new Color(253,187,132),
		new Color(252,141,89),
		new Color(239,101,72),
		new Color(215,48,31),
		new Color(179,0,0),
		new Color(127,0,0)
	};
	
	public static final Color PuBu[] = new Color[]{
		new Color(255,247,251),
		new Color(236,231,242),
		new Color(208,209,230),
		new Color(166,189,219),
		new Color(116,169,207),
		new Color(54,144,192),
		new Color(5,112,176),
		new Color(4,90,141),
		new Color(2,56,88)
	};
	
	public static final Color PuBuGn[] = new Color[]{
		new Color(255,247,251),
		new Color(236,226,240),
		new Color(208,209,230),
		new Color(166,189,219),
		new Color(103,169,207),
		new Color(54,144,192),
		new Color(2,129,138),
		new Color(1,108,89),
		new Color(1,70,54)
	};
	
	public static final Color PuRd[] = new Color[]{
		new Color(247,244,249),
		new Color(231,225,239),
		new Color(212,185,218),
		new Color(201,148,199),
		new Color(223,101,176),
		new Color(231,41,138),
		new Color(206,18,86),
		new Color(152,0,67),
		new Color(103,0,31)
	};
	
	public static final Color Purples[] = new Color[]{
		new Color(252,251,253),
		new Color(239,237,245),
		new Color(218,218,235),
		new Color(188,189,220),
		new Color(158,154,200),
		new Color(128,125,186),
		new Color(106,81,163),
		new Color(84,39,143),
		new Color(63,0,125)
	};
	
	public static final Color RdPu[] = new Color[]{
		new Color(255,247,243),
		new Color(253,224,221),
		new Color(252,197,192),
		new Color(250,159,181),
		new Color(247,104,161),
		new Color(221,52,151),
		new Color(174,1,126),
		new Color(122,1,119),
		new Color(73,0,106)
	};
	
	public static final Color Reds[] = new Color[]{
		new Color(255,245,240),
		new Color(254,224,210),
		new Color(252,187,161),
		new Color(252,146,114),
		new Color(251,106,74),
		new Color(239,59,44),
		new Color(203,24,29),
		new Color(165,15,21),
		new Color(103,0,13)
	};
	
	public static final Color YIGn[] = new Color[]{
		new Color(255,255,229),
		new Color(247,252,185),
		new Color(217,240,163),
		new Color(173,221,142),
		new Color(120,198,121),
		new Color(65,171,93),
		new Color(35,132,67),
		new Color(0,104,55),
		new Color(0,69,41)
	};
	
	public static final Color YIGnBu[] = new Color[]{
		new Color(255,255,217),
		new Color(237,248,177),
		new Color(199,233,180),
		new Color(127,205,187),
		new Color(65,182,196),
		new Color(29,145,192),
		new Color(34,94,168),
		new Color(37,52,148),
		new Color(8,29,88)
	};
	
	public static final Color YIOrBr[] = new Color[]{
		new Color(255,255,229),
		new Color(255,247,188),
		new Color(254,227,145),
		new Color(254,196,79),
		new Color(254,153,41),
		new Color(236,112,20),
		new Color(204,76,2),
		new Color(153,52,4),
		new Color(102,37,6)
	};
	
	public static final Color YIOrRd[] = new Color[]{
		new Color(255,255,204),
		new Color(255,237,160),
		new Color(254,217,118),
		new Color(254,178,76),
		new Color(253,141,60),
		new Color(252,78,42),
		new Color(227,26,28),
		new Color(189,0,38),
		new Color(128,0,38)
	};
	
	//Diverging
	public static final Color BrBG[] = new Color[]{
		new Color(84,48,5),
		new Color(140,81,10),
		new Color(191,129,45),
		new Color(223,194,125),
		new Color(246,232,195),
		new Color(245,245,245),
		new Color(199,234,229),
		new Color(128,205,193),
		new Color(53,151,143),
		new Color(1,102,94),
		new Color(0,60,48)
	};
	
	public static final Color PiYG[] = new Color[]{
		new Color(142,1,82),
		new Color(197,27,125),
		new Color(222,119,174),
		new Color(241,182,218),
		new Color(253,224,239),
		new Color(247,247,247),
		new Color(230,245,208),
		new Color(184,225,134),
		new Color(127,188,65),
		new Color(77,146,33),
		new Color(39,100,25)
	};
	
	public static final Color PRGn[] = new Color[]{
		new Color(64,0,75),
		new Color(118,42,131),
		new Color(153,112,171),
		new Color(194,165,207),
		new Color(231,212,232),
		new Color(247,247,247),
		new Color(217,240,211),
		new Color(166,219,160),
		new Color(90,174,97),
		new Color(27,120,55),
		new Color(0,68,27)
	};
	
	public static final Color PuOr[] = new Color[]{
		new Color(127,59,8),
		new Color(179,88,6),
		new Color(224,130,20),
		new Color(253,184,99),
		new Color(254,224,182),
		new Color(247,247,247),
		new Color(216,218,235),
		new Color(178,171,210),
		new Color(128,115,172),
		new Color(84,39,136),
		new Color(45,0,75),
	};	
	
	public static final Color RdBu[] = new Color[]{
		new Color(103,0,31),
		new Color(178,24,43),
		new Color(214,96,77),
		new Color(244,165,130),
		new Color(253,219,199),
		new Color(247,247,247),
		new Color(209,229,240),
		new Color(146,197,222),
		new Color(67,147,195),
		new Color(33,102,172),
		new Color(5,48,97)
	};
	
	public static final Color RdGy[] = new Color[]{
		new Color(103,0,31),
		new Color(178,24,43),
		new Color(214,96,77),
		new Color(244,165,130),
		new Color(253,219,199),
		new Color(255,255,255),
		new Color(224,224,224),
		new Color(186,186,186),
		new Color(135,135,135),
		new Color(77,77,77),
		new Color(26,26,26)
	};
	
	public static final Color RdYlBu[] = new Color[]{
		new Color(165,0,38),
		new Color(215,48,39),
		new Color(244,109,67),
		new Color(253,174,97),
		new Color(254,224,144),
		new Color(255,255,191),
		new Color(224,243,248),
		new Color(171,217,233),
		new Color(116,173,209),
		new Color(69,117,180),
		new Color(49,54,149)
	};
	
	public static final Color RdYlGn[] = new Color[]{
		new Color(165,0,38),
		new Color(215,48,39),
		new Color(244,109,67),
		new Color(253,174,97),
		new Color(254,224,139),
		new Color(255,255,191),
		new Color(217,239,139),
		new Color(166,217,106),
		new Color(102,189,99),
		new Color(26,152,80),
		new Color(0,104,55)
	};
	
	public static final Color Spectral[] = new Color[]{
		new Color(158,1,66),
		new Color(213,62,79),
		new Color(244,109,67),
		new Color(253,174,97),
		new Color(254,224,139),
		new Color(255,255,191),
		new Color(230,245,152),
		new Color(171,221,164),
		new Color(102,194,165),
		new Color(50,136,189),
		new Color(94,79,162)
	};
	
	public static final Map<String,Color[]> PALETTES=new HashMap<String,Color[]>();
	static
	{
		PALETTES.put("BLUE_BRIGHT_TO_DARK_256",BLUE_BRIGHT_TO_DARK_256_COLORS);
		PALETTES.put("WHITE_TO_BLACK_256",WHITE_TO_BLACK_256_COLORS);
		PALETTES.put("YlGnBu_9",YlGnBu_9_COLORS);				
	}	
	
	public static final Map<String,Color[]> COLORBREWER_QUALITATIVE_PALETTES=new HashMap<String,Color[]>();
	static
	{		
		COLORBREWER_QUALITATIVE_PALETTES.put("Accent",Accent);
		COLORBREWER_QUALITATIVE_PALETTES.put("Dark2",Dark2);
		COLORBREWER_QUALITATIVE_PALETTES.put("Paired",Paired);
		COLORBREWER_QUALITATIVE_PALETTES.put("Pastel1",Pastel1);
		COLORBREWER_QUALITATIVE_PALETTES.put("Pastel2",Pastel2);
		COLORBREWER_QUALITATIVE_PALETTES.put("Set1",Set1);
		COLORBREWER_QUALITATIVE_PALETTES.put("Set2",Set2);
		COLORBREWER_QUALITATIVE_PALETTES.put("Set3",Set3);
	}
	
	public static final Map<String,Color[]> COLORBREWER_SEQUENTIAL_PALETTES=new HashMap<String,Color[]>();
	static
	{		
		COLORBREWER_SEQUENTIAL_PALETTES.put("Blues",Blues);
		COLORBREWER_SEQUENTIAL_PALETTES.put("BuGn",BuGn);
		COLORBREWER_SEQUENTIAL_PALETTES.put("BuPu",BuPu);
		COLORBREWER_SEQUENTIAL_PALETTES.put("GnBu",GnBu);
		COLORBREWER_SEQUENTIAL_PALETTES.put("Greens",Greens);
		COLORBREWER_SEQUENTIAL_PALETTES.put("Greys",Greys);
		COLORBREWER_SEQUENTIAL_PALETTES.put("Oranges",Oranges);
		COLORBREWER_SEQUENTIAL_PALETTES.put("OrRd",OrRd);
		COLORBREWER_SEQUENTIAL_PALETTES.put("PuBu",PuBu);
		COLORBREWER_SEQUENTIAL_PALETTES.put("PuBuGn",PuBuGn);
		COLORBREWER_SEQUENTIAL_PALETTES.put("PuRd",PuRd);
		COLORBREWER_SEQUENTIAL_PALETTES.put("Purples",Purples);
		COLORBREWER_SEQUENTIAL_PALETTES.put("RdPu",RdPu);
		COLORBREWER_SEQUENTIAL_PALETTES.put("Reds",Reds);
		COLORBREWER_SEQUENTIAL_PALETTES.put("YIGn",YIGn);
		COLORBREWER_SEQUENTIAL_PALETTES.put("YIGnBu",YIGnBu);
		COLORBREWER_SEQUENTIAL_PALETTES.put("YIOrBr",YIOrBr);
		COLORBREWER_SEQUENTIAL_PALETTES.put("YIOrRd",YIOrRd);
	}
	
	public static final Map<String,Color[]> COLORBREWER_DIVERGING_PALETTES=new HashMap<String,Color[]>();
	static
	{
		COLORBREWER_DIVERGING_PALETTES.put("BrBG",BrBG);
		COLORBREWER_DIVERGING_PALETTES.put("PiYG",PiYG);
		COLORBREWER_DIVERGING_PALETTES.put("PRGn",PRGn);
		COLORBREWER_DIVERGING_PALETTES.put("PuOr",PuOr);
		COLORBREWER_DIVERGING_PALETTES.put("RdBu",RdBu);
		COLORBREWER_DIVERGING_PALETTES.put("RdGy",RdGy);
		COLORBREWER_DIVERGING_PALETTES.put("RdYlBu",RdYlBu);
		COLORBREWER_DIVERGING_PALETTES.put("RdYlGn",RdYlGn);
		COLORBREWER_DIVERGING_PALETTES.put("Spectral",Spectral);
	}
	
	public static Color[] COLORBREWER_ALL_QUALITATIVE;
	static
	{
		for (final Color[] array:COLORBREWER_QUALITATIVE_PALETTES.values())
		{
			if (COLORBREWER_ALL_QUALITATIVE==null) COLORBREWER_ALL_QUALITATIVE=array;
			else COLORBREWER_ALL_QUALITATIVE=ArraysUtil.concat(COLORBREWER_ALL_QUALITATIVE,array);
		}
	}
	
	public static Color[] COLORBREWER_ALL_QUALITATIVE_STRONG= new Color[]
	{
		// part of dark2
		new Color(27,158,119),
		new Color(217,95,2),
		new Color(117,112,179),
		new Color(231,41,138),
		new Color(102,166,30),
		new Color(230,171,2),
		new Color(166,118,29),
		new Color(102,102,102),
		
		// part of paired
		new Color(166,206,227),
		new Color(31,120,180),
		new Color(178,223,138),
		new Color(51,160,44),
		new Color(227,26,28),
		new Color(253,191,111),
		new Color(255,127,0),
		new Color(106,61,154),
		new Color(177,89,40),
		
		// part of set1
		new Color(228,26,28),
		new Color(55,126,184),
		new Color(77,175,74),
		new Color(152,78,163),
		new Color(255,127,0),
		new Color(166,86,40),
		new Color(247,129,191),
		new Color(153,153,153)
	};
		

	
	//
	// Constructors
	//
	
	private ColorHelper() {}
	
	
	//
	// Static methods
	//
	
	public static Color getRandomBrightColor()
	{
		final Color mix = Color.WHITE;
		final Random random = new Random();
		int red = random.nextInt(256);
	    int green = random.nextInt(256);
	    int blue = random.nextInt(256);

	    // mix the color
	    if (mix != null) {
	        red = (red + mix.getRed()) / 2;
	        green = (green + mix.getGreen()) / 2;
	        blue = (blue + mix.getBlue()) / 2;
	    }

	    return new Color(red, green, blue);
	}
	
	
	public static Color getColorForAString(final String s)
	{
		final int colorIdx=Math.abs(s.hashCode()%ColorHelper.COLORBREWER_ALL_QUALITATIVE_STRONG.length);
		final Color c=ColorHelper.COLORBREWER_ALL_QUALITATIVE_STRONG[colorIdx];
		//System.out.println(s+" "+c);
		return c;
	}

	
	//
	// Main method
	//	
	
	/**
	 * Main method.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        JXPanel panel = new JXPanel();
        panel.setPreferredSize(new Dimension(1000,800));
        
        PaletteColorComboBox combo =new PaletteColorComboBox(ColorHelper.COLORBREWER_QUALITATIVE_PALETTES);
        combo.setBorder(new TitledBorder("ColorBrewer Qualitative Color Palette"));
        panel.add(combo);
        
        combo =new PaletteColorComboBox(ColorHelper.COLORBREWER_SEQUENTIAL_PALETTES);
        combo.setBorder(new TitledBorder("ColorBrewer Sequential Color Palette"));
        panel.add(combo);
        
        combo =new PaletteColorComboBox(ColorHelper.COLORBREWER_DIVERGING_PALETTES);
        combo.setBorder(new TitledBorder("ColorBrewer Diverging Color Palette"));
        panel.add(combo);
        
        combo =new PaletteColorComboBox(ColorHelper.PALETTES);
        combo.setBorder(new TitledBorder("PALETTES"));
        panel.add(combo);
        
        JFrame frame = new JFrame("ColorHelper");
        LogoHelper.setLogo(frame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setPreferredSize(new Dimension(300,300));
	}
}
