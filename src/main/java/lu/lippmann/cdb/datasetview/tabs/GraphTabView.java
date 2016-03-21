/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.*;
import java.util.Collection;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import lu.lippmann.cdb.command.CommandDispatcher;
import lu.lippmann.cdb.context.ResourceLoader;
import lu.lippmann.cdb.dt.ui.DecisionTreeToGraphViewHelper;
import lu.lippmann.cdb.event.EventPublisher;
import lu.lippmann.cdb.graph.GraphView;
import lu.lippmann.cdb.models.CGraph;

import org.jdesktop.swingx.JXPanel;
import weka.core.Instances;


/**
 * GraphTabView.
 *
 * @author the WP1 team
 */
public class GraphTabView extends AbstractTabView
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
	public GraphTabView(final Collection<CGraph> graphs,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher)
	{
		super();
		this.jxp=new JXPanel();
		this.jxp.setLayout(new BorderLayout());	
		
		final JXPanel graphsPanel=new JXPanel();
		graphsPanel.setLayout(new GridLayout(1,graphs.size()));		
		for (final CGraph graph:graphs)
		{
			final GraphView graphView=DecisionTreeToGraphViewHelper.buildGraphView(graph,eventPublisher,commandDispatcher);			
			graphView.selectAll(true);
			graphView.reorganize();
			graphView.selectAll(false);			
			((JComponent)graphView).setBorder(new TitledBorder(""));
			graphsPanel.add(graphView.asComponent());
		}
		
		this.jxp.add(graphsPanel,BorderLayout.CENTER);	
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
		return "Graph";
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
