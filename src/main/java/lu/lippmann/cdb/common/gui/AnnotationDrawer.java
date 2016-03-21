/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;
import java.awt.geom.*;
import org.jfree.ui.Drawable;

/**
 * AnnotationDrawer.
 * 
 * @author Olivier PARISOT
 */
public final class AnnotationDrawer implements Drawable 
{
	//
	// Instance fields
	//
	
    /** The outline paint. */
    private final Paint outlinePaint;

    /** The outline stroke. */
    private final Stroke outlineStroke;

    
    //
    // Constructors
    //
    
    /**
	 * Constructor
     */
    public AnnotationDrawer(final Paint outlinePaint) 
    {
    	this(outlinePaint,new BasicStroke());
    }
    
    /**
	 * Constructor
     */
    public AnnotationDrawer(final Paint outlinePaint,final Stroke outlineStroke) 
    {
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
    }

    
    //
    // Instance methods
    //
    
    /**
     * Draws the circle.
     * 
     * @param g2  the graphics device.
     * @param area  the area in which to draw.
     */
    @Override
    public void draw(final Graphics2D g2,final Rectangle2D area) 
    {
        final Ellipse2D ellipse = new Ellipse2D.Double(area.getX(), area.getY(),area.getWidth(), area.getHeight());
        if (this.outlinePaint != null && this.outlineStroke != null) 
        {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.outlineStroke);
            g2.draw(ellipse);
        }
    }
}
