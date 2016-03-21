/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.util.List;

import lu.lippmann.cdb.common.async.simplified.AbstractSimpleAsync;
import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.*;

import org.bushe.swing.event.annotation.EventSubscriber;
import com.google.inject.Inject;
import edu.uci.ics.jung.graph.Graph;


/**
 * Graph DSL presenter.
 * 
 * @author Olivier PARISOT
 */
public class GraphDslPresenter implements Presenter<GraphDslView> {

	//
	// Instance fields
	//

	@Inject 
	private ApplicationContext applicationContext;

	@Inject
	private GraphDsl cgraphDsl;

	@Inject
	private GraphDslView view;

	/** */
	private EventPublisher eventPublisher;
	
	/** */
	private GraphView graphView;
	
	//
	// Constructors
	//

	/**
	 * Constructor.	 
	 */
	@Inject
	public GraphDslPresenter(final EventPublisher eventPublisher) 
	{
		this.eventPublisher    = eventPublisher;
		eventPublisher.markAsEventListener(this);
	}


	//
	// Instance methods
	//

	/**
	 * 
	 * @param graphView
	 */
	public void setGraphView(GraphView graphView) {
		this.graphView = graphView;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {

		view.setOnDslStringChangedListener(new Listener<String>()
				{
			@Override
			public void onAction(final String parameter) 
			{	
				final AbstractSimpleAsync<?> async = new AbstractSimpleAsync<Boolean>(false) 
						{
					@Override
					public Boolean execute() throws Exception 
					{
						final GraphDslParsingResult gpr=cgraphDsl.getGraphDslParsingResult(parameter);
						eventPublisher.publish(new GraphDslParsingErrorEvent(gpr.getLinesWithError()));
						if (gpr.getLinesWithError().isEmpty())
						{
							final Graph<CNode,CEdge> iGraph=applicationContext.getCadralGraph().getInternalGraph();
							final List<GraphOperation> l=GraphUtil.diff(applicationContext.getUser(),iGraph,gpr.getGraph());
							if (!l.isEmpty())
							{
								GraphUtil.applyOperationsToGraph((GraphWithOperations)iGraph,l);
								return Boolean.TRUE;
							}
						}						
						return Boolean.FALSE;
					}

					@Override
					public void onSuccess(Boolean result) 
					{
						if (result)
						{
							eventPublisher.publish(new GraphReloadedEvent());
							
							graphView.selectAll(true);
							graphView.reorganize();
							graphView.selectAll(false);
							
							graphView.autoFit();
						}
					}

					@Override
					public void onFailure(Throwable caught) 
					{
						eventPublisher.publish(new ErrorOccuredEvent("Error when using DSL!",caught));
					}

						};
						async.start();
			}
				});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GraphDslView getDisplay() 
	{
		return view;
	}

	@EventSubscriber(eventClass = AbstractEvent.class)
	public void onEvent(final AbstractEvent o) 
	{
		refreshView();
	}

	private void refreshView() 
	{
		if (view.isVisible())
		{			
			final Graph<CNode,CEdge> graph = applicationContext.getCadralGraph().getInternalGraph();
			view.setVariables(GraphUtil.getUsedVariablesInGraph(graph));				
			view.setDslString(cgraphDsl.getDslString(graph));
			view.reInit();			
		}
	}

	@EventSubscriber(eventClass=GraphDslParsingErrorEvent.class)
	public void onGraphDslParsingErrorEvent(final GraphDslParsingErrorEvent e) 
	{
		view.updateLinesWithError(e.getLinesWithError());
	}

	public boolean getViewVisible(){
		return view.isVisible();
	}

	public void setViewVisible(final boolean visible)
	{		
		view.setVisible(visible);
		if (visible)
		{
			view.setDslFormat(cgraphDsl.getDslFormat());
			view.setDslKeywords(cgraphDsl.getKeywords());
			view.setCommentMarker(cgraphDsl.getCommentMarker());
			refreshView();
		}
	}

}
