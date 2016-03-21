/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.variables;

import java.util.*;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;
import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import lu.lippmann.cdb.repositories.CVariablesRepository;

import org.bushe.swing.event.annotation.EventSubscriber;

import com.google.inject.Inject;


/**
 * VariablesEditorPresenter.
 * 
 * @author the ACORA/WP1 team
 */
public final class VariablesEditorPresenter implements Presenter<VariablesEditorView> 
{
	//
	// Instance fields
	//
	
	@Inject 
	private CVariablesRepository repo;
	
	@Inject 
	private ApplicationContext applicationContext;
		
	private final EventPublisher eventPublisher;
	
	private final VariablesEditorView view;
	
	private final CVariableEditorView editorView;

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	@Inject
	public VariablesEditorPresenter(final VariablesEditorView view,final CVariableEditorView editorView,final EventPublisher eventPublisher) 
	{
		this.view=view;
		this.editorView=editorView;
		this.eventPublisher=eventPublisher;
		
		eventPublisher.markAsEventListener(this);
		bindListeners();
	}

	
	//
	// Instance methods
	//
	
	private void bindListeners() 
	{
		view.setOnAskCVariableUpdateListener(new Listener<CVariable>()
		{
			@Override
			public void onAction(final CVariable parameter) 
			{
				editorView.setCVariable(parameter);
				editorView.setAlreadyInRepository(repo.contains(parameter));
				editorView.reInit(getDisplay().asComponent().getParent().getParent()); // FIXME: beurk...
				editorView.setVisible(true);
			}
		});
		editorView.setOnCVariableUpdateListener(new Listener<CVariable>()
		{
			@Override
			public void onAction(final CVariable parameter) 
			{
				if (parameter!=null)
				{
					if(editorView.hasToBeSavedInRepository()){
						repo.addOrUpdateCadralVariable(parameter);
					}else if(editorView.hasToBeDeletedInRepository()){
						repo.removeCadralVariable(parameter);
					}
					//Update variable of the graph
					((GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph())
					.addVariable(parameter);
					
					eventPublisher.publish(new VariableDefinitionHasChangedEvent());
				}
				editorView.setVisible(false);
				
			}
		});	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VariablesEditorView getDisplay() 
	{
		return view;
	}	
	
	
	/**
	 * If we change the variable def. -> update
	 * @param o
	 */
	@EventSubscriber(eventClass = VariableDefinitionHasChangedEvent.class)
	public void onVariableDefinitionHasChangedEvent(final VariableDefinitionHasChangedEvent o) 
	{
		refreshView();
	}
	
	/**
	 * 
	 */
	private void refreshView() 
	{
		final CGraph cg = applicationContext.getCadralGraph();
		if (cg==null) return;
		final GraphWithOperations gwo = (GraphWithOperations)cg.getInternalGraph();
		
		final Set<CVariable> cadralVarInRepo       = repo.getCadralVariables();
		final Set<CVariable> cadralVarUsedInGraph  = gwo.getVariables();
		
		final Set<CVariable> definedAndUsedCadralVariables=new HashSet<CVariable>();
		definedAndUsedCadralVariables.addAll(cadralVarInRepo);
		definedAndUsedCadralVariables.retainAll(cadralVarUsedInGraph);
		view.setDefinedAndUsedCVariables(definedAndUsedCadralVariables);
		
		final Set<CVariable> usedButNotDefinedCadralVariables=new HashSet<CVariable>();
		usedButNotDefinedCadralVariables.addAll(cadralVarUsedInGraph);
		usedButNotDefinedCadralVariables.removeAll(cadralVarInRepo);
		view.setUsedButNotDefinedCVariables(usedButNotDefinedCadralVariables);

		final Set<CVariable> definedButNotUsedCadralVariables=new HashSet<CVariable>();
		definedButNotUsedCadralVariables.addAll(cadralVarInRepo);
		definedButNotUsedCadralVariables.removeAll(cadralVarUsedInGraph);
		view.setDefinedButNotUsedCVariables(definedButNotUsedCadralVariables);

		view.reInit();
	}

}
