/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async;

/**
 * Skeleton class of background events (on process and on complete process).<br>
 * Each group that manage independently the busy property of widget must extend
 * this class.<br>
 * <br>
 * If a widget subscribe to one of the AbstractBackgroundEvents events,<br>
 * it will be notify by all the subclasses of AbstractBackgroundEvents.
 * 
 * @author
 * 
 * @param <T>
 * @param <U>
 */
public abstract class AbstractBackgroundEvents<T extends AbstractBackgroundEvents.OnProcessIn, U extends AbstractBackgroundEvents.OnCompleteProcessIn> {

	/**
	 * 
	 * @author
	 *
	 */
	public interface OnProcessIn extends BackgroundEvent {
	}

	/**
	 * 
	 * @author
	 *
	 */
	public interface OnCompleteProcessIn extends BackgroundEvent {
	}

	/**
	 * 
	 * @return
	 */
	public abstract T getOnProcessIn();

	/**
	 * 
	 * @return
	 */
	public abstract U getOnCompleteProcessIn();

}
