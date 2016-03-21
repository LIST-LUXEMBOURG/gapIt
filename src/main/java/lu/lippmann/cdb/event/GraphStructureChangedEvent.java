/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import lu.lippmann.cdb.models.history.Operation;

/**
 * 
 * @author didry
 *
 */
public class GraphStructureChangedEvent implements AbstractEvent {

	/**
	 * Single change
	 */
	private Operation operation;
	
	/**
	 * Global change
	 */
	private boolean global;
	private boolean historicalChange;
	
	/**
	 * 
	 * @param operation
	 */
	private GraphStructureChangedEvent(final Operation o,final boolean global,final boolean historicalChange) {
		this.operation = o;
		this.global = global;
		this.historicalChange = historicalChange;
	}
	
	
	/**
	 * 
	 * @param operation
	 */
	public GraphStructureChangedEvent() {
		this(null,true,false);
	}
	
	
	/**
	 * 
	 * @param operation
	 */
	public GraphStructureChangedEvent(final Operation o) {
		this(o,false,false);
	}
	
	/**
	 * 
	 * @param operation
	 */
	public GraphStructureChangedEvent(boolean h) {
		this(null,false,h);
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * @return the global
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * @param global the global to set
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}


	/**
	 * @return the historicalChange
	 */
	public boolean isHistoricalChange() {
		return historicalChange;
	}


	/**
	 * @param historicalChange the historicalChange to set
	 */
	public void setHistoricalChange(boolean historicalChange) {
		this.historicalChange = historicalChange;
	}
	


}
