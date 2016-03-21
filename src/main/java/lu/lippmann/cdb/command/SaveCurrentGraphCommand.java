/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import lu.lippmann.cdb.models.history.GraphWithOperations;

/**
 * 
 * @author didry
 *
 */
public class SaveCurrentGraphCommand implements AbstractCommand {

	private GraphWithOperations gwo;

	
	/**
	 * 
	 * @param gwo
	 */
	public SaveCurrentGraphCommand(GraphWithOperations gwo) {
		this.gwo = gwo;
	}

	/**
	 * @return the gwo
	 */
	public GraphWithOperations getGwo() {
		return gwo;
	}

	/**
	 * @param gwo the gwo to set
	 */
	public void setGwo(GraphWithOperations gwo) {
		this.gwo = gwo;
	}
	
	
	
}
