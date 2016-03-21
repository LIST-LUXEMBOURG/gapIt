/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.datasetview.tabs;

import java.awt.Component;
import java.util.concurrent.*;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.*;

import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.weka.WekaDataStatsUtil;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.error.ErrorInfo;
import weka.core.Instances;


/**
 * AbstractTabView.
 * 
 * @author the WP1 team
 */
public abstract class AbstractTabView implements TabView
{
	//
	// Static fields
	//

	/** */
	private static final boolean UPDATE_SLOWTABS_ONDEMAND=true;
	/** */
	private static final boolean UPDATE_SLOWTABS_ONBACKGROUND=!UPDATE_SLOWTABS_ONDEMAND;



	//
	// Instance fields
	//

	/** */
	private final ExecutorService executorService;
	/** */
	private Runnable updateToExecuteWhenTabIsSelected;

	/** */
	private Listener<DataChange> selectionListener;
	/** */
	private final JXErrorPane errorComponent;
	/** */
	private final JXPanel busyComponent;

	/** */
	private JTabbedPane parentTabbedPane;
	/** */
	private int posInParentTabbedPane;


	//
	// Constructor
	//

	public AbstractTabView()
	{
		this.errorComponent=new JXErrorPane();
		this.errorComponent.setVisible(false);
		this.busyComponent=new JXPanel();
		final JXBusyLabel comp=new JXBusyLabel();
		comp.setBusy(true);
		this.busyComponent.add(comp);
		this.busyComponent.setVisible(false);

		if (isSlow()) this.executorService=Executors.newSingleThreadExecutor();
		else this.executorService=null;
	}


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLocation(final JTabbedPane jtp,final int pos)
	{
		this.parentTabbedPane=jtp;
		this.posInParentTabbedPane=pos;

		if (UPDATE_SLOWTABS_ONDEMAND&&isSlow())
		{
			this.parentTabbedPane.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					if (isTabSelected())
					{
						if (updateToExecuteWhenTabIsSelected!=null) executorService.execute(updateToExecuteWhenTabIsSelected);
						updateToExecuteWhenTabIsSelected=null;
					}
				}
			});
		}
	}

	private boolean isTabSelected()
	{
		return (parentTabbedPane.getSelectedIndex()==posInParentTabbedPane);
	}

	/**
	 * {@inheritDoc}
	 * False by default.
	 */
	@Override
	public boolean isSlow()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * True by default.
	 */
	@Override
	public boolean needsClassAttribute()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 * False by default.
	 */
	@Override
	public boolean needsDateAttribute()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setDataChangeListener(final Listener<DataChange> selectionListener) 
	{
		this.selectionListener=selectionListener;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void pushDataChange(final DataChange c) 
	{
		selectionListener.onAction(c);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void update(final Instances dataset) 
	{
		if (this.parentTabbedPane!=null)
		{
			boolean enabled=true;
			if (needsClassAttribute()) enabled&=(dataset.classIndex()!=-1);
			if (needsDateAttribute()) enabled&=(WekaDataStatsUtil.getFirstDateAttributeIdx(dataset)!=-1);
			
			this.parentTabbedPane.setEnabledAt(this.posInParentTabbedPane,enabled);
			if (!enabled&&isTabSelected()) this.parentTabbedPane.setSelectedIndex(0);
			if (!enabled) return;
		}

		if (isSlow())
		{
			final Runnable r=new Runnable()
			{
				@Override
				public void run() 
				{
					processUpdate(new Instances(dataset)); // duplication to avoid strange problem
				}
			};
			if (UPDATE_SLOWTABS_ONBACKGROUND||isTabSelected())
			{
				this.executorService.execute(r);				
			}
			else
			{
				this.updateToExecuteWhenTabIsSelected=r;
			}
		}
		else
		{
			processUpdate(dataset);	
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Component getErrorComponent() 
	{
		return this.errorComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component getBusyComponent() 
	{		
		return this.busyComponent;
	}


	public abstract void update0(Instances dataSet) throws Exception;


	private final void processUpdate(final Instances dataset) 
	{
		busyComponent.setVisible(true);
		getComponent().setVisible(false);
		try
		{
			update0(dataset);

			getComponent().setVisible(true);
			errorComponent.setVisible(false);
		}
		catch(final Exception e)
		{

			e.printStackTrace();
			//final String msg="Impossible to show "+getName()+" view!";
			//final ErrorInfo info=new ErrorInfo(msg,msg,null,"category",e,Level.ALL,null);
			//JXErrorPane.showDialog(null, info);
			getComponent().setVisible(true);
			
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run() 
				{
					errorComponent.setVisible(true);
					final String msg="Impossible to show "+getName()+" view!";
					final ErrorInfo info=new ErrorInfo(msg,msg,null,"category",e,Level.ALL,null);
					errorComponent.setErrorInfo(info);				
				}
			});
			
		}		
		busyComponent.setVisible(false);
	}
}
