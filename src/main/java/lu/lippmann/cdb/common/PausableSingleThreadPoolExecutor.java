/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;


/**
 * PausableThreadPoolExecutor.
 * 
 * @author the WP1 team
 */
public final class PausableSingleThreadPoolExecutor extends ThreadPoolExecutor 
{
	//
	// Instance fields
	//
	
	/** */
	private boolean isPaused;
	/** */
	private boolean oneStep;
	/** */
	private final ReentrantLock pauseLock = new ReentrantLock();
	/** */
	private final Condition unpaused = pauseLock.newCondition();

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public PausableSingleThreadPoolExecutor() 
	{
		super(1, 1, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
	}

	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) 
	{
		super.beforeExecute(t, r);
		pauseLock.lock();
		try 
		{
			while (isPaused && !oneStep)
				unpaused.await();
			
			oneStep = false;
		} 
		catch (InterruptedException ie) 
		{
			t.interrupt();
		} 
		finally 
		{
			pauseLock.unlock();
		}
	}

	public void pause() 
	{
		pauseLock.lock();
		try 
		{
			isPaused = true;
		} 
		finally 
		{
			pauseLock.unlock();
		}
	}
	
	public void step() 
	{
		pauseLock.lock();
		try 
		{
			oneStep = true;
			unpaused.signalAll();
		} 
		finally 
		{
			pauseLock.unlock();
		}
	}

	public void resume() 
	{
		pauseLock.lock();
		try 
		{
			isPaused = false;
			unpaused.signalAll();
		} 
		finally 
		{
			pauseLock.unlock();
		}
	}
}
