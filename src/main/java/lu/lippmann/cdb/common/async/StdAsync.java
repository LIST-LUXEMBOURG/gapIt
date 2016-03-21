/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 * 
 * @author
 * 
 * @param <E>
 */
public class StdAsync<E> {

	//
	// Static fields
	//

	/** Logger. */
	private static final Logger LOGGER = Logger.getAnonymousLogger();
	/** Threadpool -> don't touch this!! */
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private Future<E> submit;

	//
	// Constructor
	//

	/**
	 * Constructor.
	 * 
	 * @param callable
	 * @param callback
	 */
	public StdAsync(final Callable<E> callable, final AsyncCallback<E> callback) {
		submit = EXECUTOR_SERVICE.submit(callable);
		EXECUTOR_SERVICE.submit(new StdAsyncCallback<E>(submit, callback));
	}

	//
	// Static methods
	//

	/**
	 * Terminates the executor service
	 */
	public static void shutdownAll() {
		EXECUTOR_SERVICE.shutdown();
	}

	public void stop() {
		submit.cancel(true);
	}
	
	//
	// Inner classes
	//

	/**
	 * this class make the link beetween the event and your callback
	 * 
	 * @author wax
	 * 
	 * @param <E>
	 */
	private static class StdAsyncCallback<E> implements Runnable {

		private final AsyncCallback<E> callBackToLaunch;
		private final Future<E> future;

		/**
		 * Constructor.
		 */
		public StdAsyncCallback(final Future<E> future,
				final AsyncCallback<E> cb) {
			callBackToLaunch = cb;
			this.future = future;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				try {
					final E result = future.get();
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							callBackToLaunch.onSuccess(result);
						}
					});
				} catch (final CancellationException e) {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							//this is normal :), we can cancel a job
							LOGGER.log(Level.INFO,"A job has been canceled");
						}
					});
				} catch (final InterruptedException e) {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							callBackToLaunch.onFailure(e);
						}
					});
				} catch (final ExecutionException e) {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							callBackToLaunch.onFailure(e);
						}
					});
				}
			} catch (final Exception t) {
				LOGGER.log(Level.SEVERE, "Error executing Async", t);
			}

		}
	}

}
