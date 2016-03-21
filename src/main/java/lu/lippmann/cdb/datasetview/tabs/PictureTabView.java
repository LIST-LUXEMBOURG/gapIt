/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import javax.swing.*;

import lu.lippmann.cdb.context.ResourceLoader;

import org.jdesktop.swingx.JXPanel;
import weka.core.Instances;


/**
 * PictureTabView.
 *
 * @author the WP1 team
 */
public class PictureTabView extends AbstractTabView
{
	//
	// Instance fields
	//

	/** */
	private final JXPanel jxp;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public PictureTabView(final String url)
	{
		super();
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	
		
		final ImageIcon icon=new ImageIcon(url); 
		final JLabel label=new JLabel() 
		{
			/** */
			private static final long serialVersionUID=1234L;

			@Override
		    public void paintComponent(final Graphics g)
		    {
		        super.paintComponent(g);
		        
		        final double scalex = (double) getWidth() / icon.getIconWidth();
		        final double scaley = (double) getHeight() / icon.getIconHeight();
		        final double ratio=Math.min(scalex, scaley);
			    
		        final int preferredWidth=(int)(ratio*icon.getIconWidth());
		        final int preferredHeight=(int)(ratio*icon.getIconHeight());
		        		        
		        g.setColor(Color.WHITE);
		        g.fillRect(0,0,getWidth(),getHeight());
		        
		        g.drawImage(icon.getImage(),(getWidth()-preferredWidth)/2,(getHeight()-preferredHeight)/2,preferredWidth,preferredHeight,this);
		        
		        g.drawRect((getWidth()-preferredWidth)/2,(getHeight()-preferredHeight)/2,preferredWidth,preferredHeight);
		    }			
		}; 
		label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		
		this.jxp.add(label,BorderLayout.CENTER);
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * True by default.
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() 
	{
		return "Picture";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getComponent() 
	{			
		return jxp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update0(final Instances dataSet) throws Exception {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() 
	{
		return ResourceLoader.getAndCacheIcon("menu/stats.png"); // TODO: change it
	}
	
	
}
