/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.models.CGraphElement;


/**
 * ShowTagsEditorCommand.
 *
 * @author Olivier PARISOT
 */
public final class ShowTagsEditorCommand  implements AbstractCommand 
{
	//
	// Instance fields
	//

	/** */
	private final CGraphElement cGraphElement;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public ShowTagsEditorCommand(final CGraphElement cGraphElement) 
	{
		this.cGraphElement=cGraphElement;
	}

	
	//
	// Instance methods
	//
	
	public CGraphElement getCGraphElement() 
	{
		return cGraphElement;
	}}
