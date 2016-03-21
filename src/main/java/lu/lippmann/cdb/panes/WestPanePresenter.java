/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JPanel;

import lu.lippmann.cdb.App;
import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.GraphReloadedEvent;
import lu.lippmann.cdb.graph.*;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.panes.editor.*;
import lu.lippmann.cdb.panes.evaluator.VariablesEvaluatorPresenter;
import lu.lippmann.cdb.panes.history.HistoryPresenter;
import lu.lippmann.cdb.panes.variables.VariablesEditorPresenter;

import org.bushe.swing.event.annotation.EventSubscriber;

import com.google.inject.Inject;


/**
 * WestPanePresenter.
 * 
 * @author the ACORA team
 */
public class WestPanePresenter implements Presenter<WestPaneView>{

	//
	// Instance fields
	// 
	
	@Inject
	private ApplicationContext applicationContext;
	
	@Inject
	private NodeEditorView nodeView;

	@Inject
	private EdgeEditorView edgeView;
	
	/** */
	private final WestPaneView view;	
	/** */
	private final ViewModePaneView viewModePaneView;
	/** */
	private final DefaultColorsAndShapesPaneView defaultColorsAndShapePaneView;
	
	private final JPanel contextEditorPanel;
	
	/** Keep it to avoid gc. */
	private HistoryPresenter historyPresenter;
	/** Keep it to avoid gc. */
	private final VariablesEditorPresenter cadralVariablesPresenter;
	/** Keep it to avoid gc. */
	private final VariablesEvaluatorPresenter variablesEvaluatorPresenter;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	@Inject
	public WestPanePresenter(final VariablesEditorPresenter cadralVariablesPresenter,final VariablesEvaluatorPresenter variablesEvaluatorPresenter,final HistoryPresenter historyPresenter,final CommandDispatcher commandDispatcher)
	{
		commandDispatcher.markAsCommandHandler(this);
		
		this.contextEditorPanel=new JPanel();
		contextEditorPanel.setVisible(false);
		
		this.cadralVariablesPresenter=cadralVariablesPresenter;
		this.variablesEvaluatorPresenter=variablesEvaluatorPresenter;
		this.historyPresenter=historyPresenter;
		
		cadralVariablesPresenter.init();
		variablesEvaluatorPresenter.init();
		historyPresenter.init();		
		
		this.viewModePaneView=new ViewModePaneView();
		this.defaultColorsAndShapePaneView=new DefaultColorsAndShapesPaneView();
		
		final java.util.List<Component> l=new ArrayList<Component>();
		l.add(viewModePaneView);
		l.add(defaultColorsAndShapePaneView);
		l.add(cadralVariablesPresenter.getDisplay().asComponent());
		l.add(historyPresenter.getDisplay().asComponent());
		l.add(variablesEvaluatorPresenter.getDisplay().asComponent());
		l.add(contextEditorPanel);
				
		
		this.view=new WestPaneView(l);
		this.view.setVisible(App.BOOT_WITH_NEW_GRAPH);
		
		viewModePaneView.setOnSelectView(new Listener<ViewMode>() 
		{
			@Override
			public void onAction(final ViewMode parameter) 
			{
				commandDispatcher.dispatch(new SetViewModeCommand(parameter));
				commandDispatcher.dispatch(new SwitchDefaultPanelVisibilityCommand(parameter));
			}
		});		
		
		defaultColorsAndShapePaneView.setOnShapeSelectListener(new Listener<CShape>() 
		{
			@Override
			public void onAction(final CShape parameter) 
			{
				commandDispatcher.dispatch(new SetShapeCommand(parameter));
			}
		});

		defaultColorsAndShapePaneView.setOnColorSelectListener(new Listener<Color>() 
		{
			@Override
			public void onAction(final Color parameter) 
			{
				commandDispatcher.dispatch(new SetColorCommand(parameter));
			}
		});
	}
		
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {}

	@EventSubscriber(eventClass = GraphReloadedEvent.class)
	public void onAskReload(GraphReloadedEvent evt) 
	{
		this.view.setVisible(applicationContext.getCadralGraph()!=null);
	}
	
	@EventSubscriber(eventClass=SwitchDefaultPanelVisibilityCommand.class)
	public void onChangeMode(SwitchDefaultPanelVisibilityCommand evt)
	{
		this.defaultColorsAndShapePaneView.setVisible(evt.getMode().equals(ViewMode.Add));
		if(!evt.getMode().equals(ViewMode.Edit)){
			this.contextEditorPanel.setVisible(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WestPaneView getDisplay() 
	{		
		return view;
	}

	//@CommandHandler(commandClass=SelectNodeCommand.class)
	@EventSubscriber(eventClass=SelectNodeCommand.class)
	public void handle(final SelectNodeCommand cmd) 
	{
		nodeView.setNode(cmd.getNode());
		setContextualDisplay(nodeView);
		if(cmd.getClickCount()==2)
		{
			nodeView.focusOnField();
		}
	}

	//@CommandHandler(commandClass=SelectEdgeCommand.class)
	@EventSubscriber(eventClass=SelectEdgeCommand.class)
	public void handle(final SelectEdgeCommand cmd) 
	{
		edgeView.setEdge(cmd.getEdge());
		setContextualDisplay(edgeView);
		if(cmd.getClickCount()==2)
		{
			edgeView.focusOnField();
		}
	}
	
	public void setContextualDisplay(Display d) 
	{
		contextEditorPanel.setVisible(true);
		contextEditorPanel.setLayout(new GridLayout(1, 1));
		contextEditorPanel.removeAll();
		contextEditorPanel.add(d.asComponent());
		((JPanel) view.asComponent()).updateUI();
		//view.asComponent().repaint();
	}
	
	/**
	 * 
	 * @return
	 */
	public ViewModePaneView getViewModePaneView() {
		return viewModePaneView;
	}


	
	
}
