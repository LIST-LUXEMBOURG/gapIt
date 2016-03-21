/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt.ui;

import java.awt.event.*;
import java.util.List;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.dt.DecisionTree;
import lu.lippmann.cdb.event.EventPublisher;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;

import org.apache.commons.lang.StringEscapeUtils;

import edu.uci.ics.jung.graph.Graph;


/**
 * DecisionTreeToGraphViewHelper.
 *
 * @author Olivier PARISOT
 */
public final class DecisionTreeToGraphViewHelper 
{
	//
	// Constructors
	//
	/**
	 * Private constructor.
	 */
	private DecisionTreeToGraphViewHelper() { }	


	//
	// Static methods
	//

	/**
	 * 
	 */
	public static GraphView buildGraphView(final DecisionTree gdt,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher)
	{
		return buildGraphView(gdt.getGraphWithOperations(),eventPublisher,commandDispatcher);
	}

	public static GraphView buildGraphView(final CGraph cgraph,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher)
	{
		final GraphViewImpl graphView=new GraphViewImpl(eventPublisher,commandDispatcher);
		graphView.init();
		graphView.setViewMode(ViewMode.Edit);				
		graphView.setCGraph(cgraph);

		//Tricky way to autofit ...
		graphView.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if(graphView != null){
					graphView.autoFit();
				}
			}
		});
		
		return graphView;
	}
	
	public static GraphView buildGraphView(final GraphWithOperations gwo,final EventPublisher eventPublisher,final CommandDispatcher commandDispatcher)
	{
		return buildGraphView(GraphUtil.buildNewCGraphReorganizedLayout(gwo),eventPublisher,commandDispatcher);
	}
	
	public static GraphView buildSimpleGraphView(final GraphWithOperations gwo,final EventPublisher eventPublisher)
	{
		final GraphView graphView=new GraphViewImpl(eventPublisher,new CommandDispatcherFakeImpl());
		graphView.init();
		graphView.setViewMode(ViewMode.Edit);				
		final CGraph cgraph=GraphUtil.buildNewCGraphWithFRLayout(gwo);		
		graphView.setCGraph(cgraph);

		graphView.selectAll(true);
		graphView.reorganize();
		graphView.selectAll(false);

		return graphView;
	}
	
	public static <V,E> GenericGraphView<V,E> buildSimpleGenericGraphView(final Graph<V,E> graph)
	{
		final GenericGraphView<V,E> graphView=new GenericGraphViewImpl<V,E>();
		graphView.init();
		graphView.setViewMode(ViewMode.Edit);				
		final GenericCGraph<V,E> cgraph=GraphUtil.buildNewCGenericGraphWithFRLayout(graph);		
		graphView.setCGraph(cgraph);

		graphView.selectAll(true);
		graphView.reorganize();
		graphView.selectAll(false);

		return graphView;
	}
	
	public static <V,E> GenericGraphView<V,E> buildSimpleGenericGraphView(final GenericCGraph<V,E> cgraph)
	{
		final GenericGraphView<V,E> graphView=new GenericGraphViewImpl<V,E>();
		graphView.init();
		graphView.setViewMode(ViewMode.Edit);				
		graphView.setCGraph(cgraph);

		return graphView;
	}

	// FIXME: to move ...
	public static String buildHTML(final String title,final List<String> l)
	{
		final StringBuilder sb=new StringBuilder("<html>");
		sb.append("<b>").append(title).append("</b><br/><br/>");
		if(l!=null){
			for (final String s:l)
			{
				sb.append(StringEscapeUtils.escapeHtml(s)).append("<br/>");
			}
			sb.setLength(sb.length()-1);
			sb.append("</html>");
		}
		return sb.toString();
	}	

}
