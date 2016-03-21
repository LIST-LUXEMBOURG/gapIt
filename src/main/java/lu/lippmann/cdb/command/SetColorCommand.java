/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import java.awt.Color;


/**
 * SetColorCommand.
 * 
 * @author the ACORA team
 */
public class SetColorCommand implements AbstractCommand 
{

	private final Color color;
	
	public SetColorCommand(final Color color) 
	{
		this.color=color;
	}

	public Color getColor() 
	{	
		return color;
	}

}
