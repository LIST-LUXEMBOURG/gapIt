/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async.simplified;

/**
 * 
 * @author didry
 * 
 * @param <T>
 */
public interface SimpleAsync<T> {

	/**
	 * Execute method
	 * 
	 * @return
	 * @throws FacadeException
	 */
	T execute() throws Exception;
	
	/**
	 * Cancel execution
	 */
	//void cancel();

	/**
	 * Method to be executed on success
	 * 
	 * @param result
	 */
	void onSuccess(T result);

	/**
	 * Method to be executed on failure
	 * 
	 * @param caught
	 */
	void onFailure(Throwable caught);

}
