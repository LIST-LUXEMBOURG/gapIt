/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph.mouse;

import java.awt.event.MouseEvent;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;


/**
 * CadralTaggingGraphMousePlugin.
 * 
 * @author Olivier PARISOT
 */
public final class CadralTaggingGraphMousePlugin extends PickingGraphMousePlugin<CNode,CEdge> {

	//
	// Instance fields
	//
	
	/** */
	private CGraphElement selectedCGraphElement;

	/** */
	private final CommandDispatcher commandDispatcher;

	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public CadralTaggingGraphMousePlugin(final CommandDispatcher commandDispatcher)
	{
		this.commandDispatcher=commandDispatcher;
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(final MouseEvent e) 
	{
		if (vertex != null) 
		{ 
			selectedCGraphElement = vertex;
		} 
		else if (edge != null)   
		{ 
			selectedCGraphElement = edge; 
		} 
		else 
		{
			selectedCGraphElement = null;
		}
		super.mouseReleased(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(final MouseEvent e) 
	{
		final CGraphElement cge=selectedCGraphElement;
		if (cge!=null)
		{
			commandDispatcher.dispatch(new ShowTagsEditorCommand(cge));
		}
		super.mouseClicked(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent arg0) 
	{
		System.out.println("Not allowed to move the nodes in this mode");
	}
	
}
