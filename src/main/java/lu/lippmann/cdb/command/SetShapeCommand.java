/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.graph.renderer.CShape;


/**
 * SetShapeCommand.
 * 
 * @author the ACORA team
 */
public final class SetShapeCommand implements AbstractCommand 
{

	private final CShape shape;
	
	public SetShapeCommand(CShape shape) 
	{
		this.shape=shape;
	}

	public CShape getShape() 
	{		
		return shape;
	}

}
