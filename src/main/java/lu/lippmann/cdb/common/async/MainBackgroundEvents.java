/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async;

/**
 * Main background events for asynchronous calls.<br>
 * These events are the most used.
 * 
 * @author
 * 
 */
public class MainBackgroundEvents extends AbstractBackgroundEvents<MainBackgroundEvents.OnProcessIn, MainBackgroundEvents.OnCompleteProcessIn> {

	/**
	 * 
	 * @author
	 *
	 */
	public class OnProcessIn implements AbstractBackgroundEvents.OnProcessIn {	}

	/**
	 * 
	 * @author
	 *
	 */
	public class OnCompleteProcessIn implements	AbstractBackgroundEvents.OnCompleteProcessIn {	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OnProcessIn getOnProcessIn() {
		return new OnProcessIn();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OnCompleteProcessIn getOnCompleteProcessIn() {
		return new OnCompleteProcessIn();
	}

}
