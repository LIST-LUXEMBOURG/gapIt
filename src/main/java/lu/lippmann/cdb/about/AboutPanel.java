/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.about;

import java.awt.*;
import org.jdesktop.swingx.*;


/**
 * AboutPanel.
 * 
 * @author Olivier PARISOT
 */
public final class AboutPanel extends JXPanel 
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=1526993234673L;

	
	//
	// Constructors
	//
	
	public AboutPanel(final boolean light)
	{
		super();
		
		setPreferredSize(new Dimension(1280,900));
	}
}
