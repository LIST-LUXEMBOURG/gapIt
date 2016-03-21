/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.history;

import java.util.List;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.models.history.*;

import org.bushe.swing.event.annotation.EventSubscriber;

import com.google.inject.Inject;


/**
 * HistoryPresenter.
 * 
 * @author the ACORA team
 */
public class HistoryPresenter implements Presenter<HistoryView> 
{
	//
	// Instance fields
	//

	private final HistoryView view;

	@Inject
	private ApplicationContext context;

	private final EventPublisher eventPublisher;


	//
	// Constructors
	//

	/**
	 * Constructor.	 
	 */
	@Inject
	public HistoryPresenter(final HistoryView view,final EventPublisher eventPublisher) 
	{
		this.view=view;
		this.eventPublisher=eventPublisher;
		bindComponents();
		eventPublisher.markAsEventListener(this);
	}


	//
	// Instance methods
	//

	private void bindComponents() {
		view.setOnClick(new Listener<List<GraphOperation>>() {

			@Override
			public void onAction(List<GraphOperation> parameter) {
				final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();

				boolean moved = false;
				
				//Single element
				if(parameter.size() == 1){
						final GraphOperation o = parameter.get(0);
						int idx = graph.getOperations().indexOf(o);
						moved  = graph.getOperationIndex()!=idx+1;
						if(moved){
							graph.moveAt(o); 
						}
				}else{
				//Group elements
					
					//if not a group -> has moved
					final Long idGroup = graph.getOperations().get(graph.getOperationIndex()-1).getIdGroup();
					for(final GraphOperation o : parameter){
						moved |= o.getIdGroup().longValue() != idGroup.longValue();
						graph.moveAt(o); // if at least one element changes -> OK
					}
				}
 
				if(moved){
					//Publish a event with flag historicalChange = true
					eventPublisher.publish(new GraphStructureChangedEvent(true));

					eventPublisher.publish(new GraphRepaintedEvent());
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {

	}

	@EventSubscriber(eventClass = GraphStructureChangedEvent.class)
	public  void refreshHistory(GraphStructureChangedEvent evt){
		//Don't call if it's a histrorical change !
		if(!evt.isHistoricalChange()){
			if(context.getCadralGraph()!=null){
				final GraphWithOperations graph = (GraphWithOperations)context.getCadralGraph().getInternalGraph();
				view.setOperations(graph.getOperations());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HistoryView getDisplay() {
		return view;
	}

}
