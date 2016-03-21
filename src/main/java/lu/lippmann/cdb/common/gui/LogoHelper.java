/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;


/**
 * LogoHelper.
 *
 *
 * @author Olivier PARISOT
 */
public final class LogoHelper 
{
	/**
	 * Private constructor to avoid instanciation.
	 */
	private LogoHelper() {}
		
	/**
	 * Set logo.	 
	 */
	public static void setLogo(final Window w)
	{
		/*final List<Image> images=new LinkedList<Image>();
		try 
		{
			final ClassLoader cl=ClassLoader.getSystemClassLoader();
			images.add(ImageIO.read(cl.getResource("cadral-logo-tiny.png")));
			images.add(ImageIO.read(cl.getResource("cadral-logo-small.png")));
			images.add(ImageIO.read(cl.getResource("cadral-logo.png")));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		w.setIconImages(images);*/
	}
}
