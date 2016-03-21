/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async;

import java.util.*;
import java.util.concurrent.Callable;

import org.bushe.swing.event.EventBus;

/**
 * 
 * @author
 * 
 * @param <E>
 */
public class AcoraAsync<E> extends StdAsync<E> {

	/**
	 * the main purpose of this class is to publish event on the bus this class
	 * will wrapp AsyncCallback passed in argument and will publish event on the
	 * bus to handle event if an async callback has been launched and if all
	 * async callback has been finished.
	 * 
	 * @author wax
	 * 
	 * @param <E>
	 */
	private static class AsyncCallbackDecorator<E> implements AsyncCallback<E> {

		private final AsyncCallback<E> wrappedCallback;

		private static final Map<String, Set<Integer>> ASYNC_GROUP_IN_BACKGROUND = new HashMap<String, Set<Integer>>();

		/**
		 * Use to ensure that identifiers are only accessed by one thread at a
		 * time.
		 */
		private static final Object LOCK = new Object();

		/**
		 * unique identifier to know which async are running in BG
		 */
		private final int uid;

		/**
		 * Background event used to manage busy property with many kinds of
		 * asynchronous calls
		 */
		private final AbstractBackgroundEvents<?, ?> backgroundEvents;

		public AsyncCallbackDecorator(final AsyncCallback<E> callback,
				AbstractBackgroundEvents<?, ?> backgroundEvents) {

			this.backgroundEvents = backgroundEvents;

			// generate a uniq identifier
			final Random r = new Random();
			uid = r.nextInt(Integer.MAX_VALUE);

			this.wrappedCallback = callback;

			// BackgroundEvents class name is used as a key
			final String eventClassName = backgroundEvents.getClass().getName();
			synchronized (LOCK) {
				final Set<Integer> asyncInBackground;
				final Set<Integer> temp = ASYNC_GROUP_IN_BACKGROUND.get(eventClassName);
				if (temp != null) {
					asyncInBackground = temp;
				} else {
					asyncInBackground = new TreeSet<Integer>();
					ASYNC_GROUP_IN_BACKGROUND.put(eventClassName,
							asyncInBackground);
				}
				// send event on the bus what there's process in BG
				if (asyncInBackground.isEmpty()) {
					EventBus.publish(backgroundEvents.getOnProcessIn());
				}
				asyncInBackground.add(uid);
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(final E result) {
			wrappedCallback.onSuccess(result);
			handleEventOnFinish();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(final Throwable caught) {
			wrappedCallback.onFailure(caught);
			handleEventOnFinish();
		}

		/**
		 * 
		 */
		private void handleEventOnFinish() {
			// BackgroundEvents class name is used as a key
			final String eventClassName = backgroundEvents.getClass().getName();
			synchronized (LOCK) {
				final Set<Integer> asyncInBackground = ASYNC_GROUP_IN_BACKGROUND
						.get(eventClassName);
				// If no more process running for a kind of background event we
				// send the OnCompleteProcessIn event.
				asyncInBackground.remove(uid);
				if (asyncInBackground.isEmpty()) {
					EventBus.publish(backgroundEvents.getOnCompleteProcessIn());
				}
			}
		}

	}

	/**
	 * 
	 * @param callable
	 * @param callback
	 */
	public AcoraAsync(final Callable<E> callable,final AsyncCallback<E> callback,AbstractBackgroundEvents<?, ?> backgroundEvents) {
		super(callable, new AsyncCallbackDecorator<E>(callback,	backgroundEvents));
	}

}
