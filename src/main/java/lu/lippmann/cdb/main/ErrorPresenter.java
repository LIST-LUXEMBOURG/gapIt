/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.main;

import java.util.logging.Level;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.event.*;

import org.bushe.swing.event.annotation.*;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import com.google.inject.Inject;


/**
 * Error presenter.
 * 
 * @author Olivier PARISOT
 */
public final class ErrorPresenter implements Presenter<Display>
{
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	@Inject
	public ErrorPresenter(final EventPublisher eventPublisher) 
	{
		eventPublisher.markAsEventListener(this);
	}
	
	
	//
	// Instance methods
	//

	@EventSubscriber(eventClass=ErrorOccuredEvent.class)
	public void onErrorEvent(final ErrorOccuredEvent event) 
	{
		final String title=event.getMsg();
		String msg=event.getMsg();
		if (event.getException()!=null) msg+="\nReason: "+event.getException().getMessage()+"";
		final ErrorInfo info=new ErrorInfo(title,msg,null,"category",event.getException(),Level.ALL,null);		
		JXErrorPane.showDialog(null,info);
	}

	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public Display getDisplay() 
	{
		return null;
	}

	/**	 
	 * {@inheritDoc}
	 */
	@Override
	public void init() {}
	
}
