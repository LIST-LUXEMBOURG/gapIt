/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import lu.lippmann.cdb.common.mvp.Listener;

import org.jfree.chart.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.plot.PiePlot;


/**
 * Drag and droppable PieChartPanel.
 *
 *
 * @author Yoann DIDRY, Olivier PARISOT
 *
 */
public final class DragAndDroppablePieChartPanel extends ChartPanel 
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=145687945L;

	
	//
	// Instance fields
	//

	/** */
	private PieSectionEntity source = null;
	/** */
	private double tx,ty;
	/** */
	private boolean released = false;
	/** */
	private final Listener<Integer[]> listener;
	
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public DragAndDroppablePieChartPanel(final JFreeChart chart,final boolean useBuffer,final Listener<Integer[]> listener) 
	{
		super(chart, useBuffer);
		this.listener=listener;
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		if(!released && source!=null)
		{
			final PiePlot plot = ((PiePlot)getChart().getPlot());
			final Comparable<?> key = plot.getDataset().getKey(source.getSectionIndex());
			final Color color       = (Color)plot.getSectionPaint(key);
			g2d.translate(tx, ty);
			g2d.setColor(color);
			g2d.fill(source.getArea());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) 
	{
		super.mousePressed(e);
		final ChartEntity entity = getEntityForPoint(e.getX(),e.getY());
		if(entity instanceof PieSectionEntity)
		{
			source = (PieSectionEntity)entity;
		}
		released = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		if (source!=null)
		{
			final ChartRenderingInfo info = getChartRenderingInfo();   
			final Rectangle2D dataArea = info.getPlotInfo().getPlotArea();   
			final double xx = dataArea.getCenterX();
			final double yy = dataArea.getCenterY();
			tx = e.getX()-xx;
			ty = e.getY()-yy;
			repaint();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) 
	{
		super.mouseReleased(e);
		final ChartEntity entity = getEntityForPoint(e.getX(),e.getY());
		if(entity instanceof PieSectionEntity)
		{
			final PieSectionEntity target = ((PieSectionEntity) entity);
			if (source!=null)
			{
				if (target.getSectionIndex()!=source.getSectionIndex())
				{
					listener.onAction(new Integer[]{source.getSectionIndex(),target.getSectionIndex()});
				}
			}
		}
		released = true;
		repaint();
	}
}
