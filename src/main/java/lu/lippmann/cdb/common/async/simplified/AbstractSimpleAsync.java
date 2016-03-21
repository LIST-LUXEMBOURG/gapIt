/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.async.simplified;

import java.awt.event.*;
import java.util.concurrent.Callable;

import lu.lippmann.cdb.common.async.*;
import lu.lippmann.cdb.main.MainViewLoadingFrame;


/**
 * AbstractSimpleAsync.
 * 
 * @author the ACORA team
 */
public abstract class AbstractSimpleAsync<T> implements SimpleAsync<T> {

	private MainViewLoadingFrame loadingFrame;


	/**
	 * local class to wrapp simplified Async Call
	 * 
	 * @author wax
	 * 
	 * @param <T>
	 */
	private static class AbstractSimpleAsyncCallback<T> implements AsyncCallback<T> {

		private AbstractSimpleAsync<T> abstractAsyncObj;

		/**
		 * 
		 * @param asyncObj
		 */
		public AbstractSimpleAsyncCallback(AbstractSimpleAsync<T> asyncObj) {
			this.abstractAsyncObj = asyncObj;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(T result) {
			abstractAsyncObj.postSuccess();
			abstractAsyncObj.onSuccess(result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable caught) {
			abstractAsyncObj.postFailure();
			abstractAsyncObj.onFailure(caught);
		}

	}

	/**
	 * store if the async call will propagate on the bus
	 */
	private final boolean propagateOnBus;

	/**
	 * Background events that will be used with asynchronous calls
	 * (subscribe/publish)
	 */
	private final AbstractBackgroundEvents<?, ?> backgroundEvents;

	/**
	 * callable 
	 */
	private Callable<T> callable;

	/**
	 * asyncObj
	 */
	private StdAsync<T> asyncObj = null;

	
	/**
	 * Is task canceled ?
	 */
	private boolean canceled;

	/**
	 * Is loading frame ?
	 */
	private boolean useLoadingFrame;

	/**
	 * Default constructor (provided success/failure)
	 */
	public AbstractSimpleAsync(boolean useLoadingFrame) {
		this(useLoadingFrame,true);
	}

	/**
	 * 
	 * @param propagateOnBus
	 *            propagation of async call on event bus
	 */
	public AbstractSimpleAsync(boolean useLoadingFrame,boolean propagateOnBus) {
		this.useLoadingFrame  = useLoadingFrame;
		this.propagateOnBus   = propagateOnBus;
		this.backgroundEvents = new MainBackgroundEvents();
	}

	/**
	 * 
	 * @param propagateOnBus
	 *            propagation of async call on event bus
	 */
	public AbstractSimpleAsync(AbstractBackgroundEvents<?, ?> backgroundEvents) {
		this.propagateOnBus = true;
		this.backgroundEvents = backgroundEvents;
	}

	/**
	 * will start the async call
	 */
	public void start() {
		canceled = false;
		if(useLoadingFrame){
			initBusyView();
		}
		invokeSync(new AbstractSimpleAsyncCallback<T>(this),propagateOnBus);
	}

	/**
	 * 
	 */
	private void initBusyView() {
		if(loadingFrame==null){
			loadingFrame = new MainViewLoadingFrame();
			loadingFrame.setVisible(true);
			loadingFrame.pack();
			loadingFrame.repaint();
			loadingFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					stop();
				}
			});
		}
	}

	/**
	 * Protected method to build Sync Using provided callback.
	 */
	protected void invokeSync(AsyncCallback<T> callback,boolean propagateOnBus) {
		/**
		 * Callable object
		 */
		callable = new Callable<T>() {
			@Override
			public T call() throws Exception {
				return execute();
			}
		};

		if (callback == null) {
			throw new IllegalArgumentException("Please provide a non-null callback !");
		}

		// Build Async object
		if (propagateOnBus) {
			asyncObj = new AcoraAsync<T>(callable, callback, backgroundEvents);
		} else {
			asyncObj = new StdAsync<T>(callable, callback);
		}

	}


	/**
	 * 
	 */
	public void postSuccess() {
		//System.out.println("SUCCESS !");
		if(loadingFrame!=null){
			loadingFrame.setVisible(false);
		}
		loadingFrame = null;
	}

	/**
	 * 
	 */
	public void postFailure() {
		//System.out.println("FAILURE !");
		if(loadingFrame!=null){
			loadingFrame.setVisible(false);
		}
		loadingFrame = null;
	}


	/**
	 * 
	 */
	public void stop() {
		if (asyncObj!=null) {
			if(loadingFrame!=null){
				loadingFrame.setVisible(false);
				loadingFrame = null;
			}
			asyncObj.stop();
			canceled = true;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCanceled() {
		return canceled;
	}
	
}
