/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingx.*;

import lu.lippmann.cdb.common.mvp.Display;


/**
 * WestPaneView.
 * 
 * @author the ACORA team
 */
public final class WestPaneView extends JPanel implements Display 
{
	//
	// Static fields
	//
	
	/** Serial version UID. */
	private static final long serialVersionUID=1787279L;
	
	
	//
	// Instance fields
	//
	
	/** */
	private final JXTaskPaneContainer container;
	
	
	//
	// Constructors.
	//
	
	/**
	 * Constructor.
	 */
	public WestPaneView(final List<Component> views) 
	{
		this.setLayout(new BorderLayout());
		this.container=new JXTaskPaneContainer();
		for (final Component c:views) container.add(c);
		add(container,BorderLayout.CENTER);
		//setTaskPaneCollapsed(true);		
	}

	
	//
	// Instance methods
	//

	/* could be usefull 
	private void setTaskPaneCollapsed(final boolean b)
	{
		for (final Component c:this.container.getComponents())
		{
			if (c instanceof JXTaskPane)
			{
				final JXTaskPane jtp=(JXTaskPane)c;			
				jtp.setCollapsed(b);
				jtp.setEnabled(!b);
			}			
		}
		revalidate();
	} */
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		return this;
	}
	
}
