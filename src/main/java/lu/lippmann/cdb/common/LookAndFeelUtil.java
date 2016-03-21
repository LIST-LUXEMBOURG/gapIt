/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import javax.swing.*;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;


/**
 * LookAndFeelUtil.
 * 
 * @author Olivier PARISOT
 */
public class LookAndFeelUtil 
{
	private LookAndFeelUtil() {}
	
	public static final void init()
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		//SubstanceLookAndFeel.setSkin(new ModerateSkin());
		try 
		{
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
}
