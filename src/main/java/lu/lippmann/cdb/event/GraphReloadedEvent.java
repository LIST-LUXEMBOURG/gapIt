/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

/**
 * 
 * @author didry
 *
 */
public class GraphReloadedEvent implements AbstractEvent {
	
	/** */
	private boolean reloadVariables;
	
	/**
	 * 
	 */
	public GraphReloadedEvent(){
		this.reloadVariables = true;
	}
	
	/**
	 * 
	 * @param r
	 */
	public GraphReloadedEvent(boolean r){
		this.reloadVariables = r;
	}

	/**
	 * @return the reloadVariables
	 */
	public boolean isReloadVariables() {
		return reloadVariables;
	}

	/**
	 * @param reloadVariables the reloadVariables to set
	 */
	public void setReloadVariables(boolean reloadVariables) {
		this.reloadVariables = reloadVariables;
	}
	
	
	
	
}
