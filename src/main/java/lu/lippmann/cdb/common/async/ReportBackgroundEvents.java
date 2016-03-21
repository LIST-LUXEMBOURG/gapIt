/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async;

/**
 * Background events for asynchronous calls.<br>
 * These events are only used as default by "Reports" widgets.<br>
 * However, "Reports" that aren't displayed into a modal frame should use
 * MainBackgroundEvents.<br>
 * 
 * @author
 * 
 */
public class ReportBackgroundEvents extends AbstractBackgroundEvents<ReportBackgroundEvents.OnProcessIn, ReportBackgroundEvents.OnCompleteProcessIn> {

	/**
	 * 
	 * @author
	 *
	 */
	public class OnProcessIn implements AbstractBackgroundEvents.OnProcessIn {
	}

	/**
	 * 
	 * @author
	 *
	 */
	public class OnCompleteProcessIn implements
			AbstractBackgroundEvents.OnCompleteProcessIn {
	}

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
