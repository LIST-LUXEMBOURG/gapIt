/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import lu.lippmann.cdb.models.CLayoutTransition;

/**
 * 
 * @author didry
 *
 */
public class LayoutChangedEvent implements AbstractEvent
{
	
	private int idSource;
	private CLayoutTransition transition;
	
	/**
	 * 
	 * @param map
	 */
	public LayoutChangedEvent(int idSource,CLayoutTransition transition){
		this.idSource   = idSource;
		this.transition = transition;
	}

	/**
	 * @return the map
	 */
	public CLayoutTransition getTransition() {
		return transition;
	}

	/**
	 * @param map the map to set
	 */
	public void setTransition(CLayoutTransition transition) {
		this.transition = transition;
	}

	/**
	 * 
	 * @return
	 */
	public int getIdSource() {
		return idSource;
	}

	/**
	 * 
	 * @param idSource
	 */
	public void setIdSource(int idSource) {
		this.idSource = idSource;
	}

	
	
}

