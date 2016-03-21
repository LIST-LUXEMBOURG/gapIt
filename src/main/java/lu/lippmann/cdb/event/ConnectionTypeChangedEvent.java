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
public class ConnectionTypeChangedEvent implements AbstractEvent {

	public enum GraphConnectionStatus {
		LOCAL, SHARED , CONNECTED
	}
	
	private GraphConnectionStatus status;

	
	/**
	 * 
	 * @param status
	 */
	public ConnectionTypeChangedEvent(GraphConnectionStatus status) {
		this.status = status;
	}



	/**
	 * @return the status
	 */
	public String getStatus() {
		return status.name();
	}
	
	
	
	
	
}
