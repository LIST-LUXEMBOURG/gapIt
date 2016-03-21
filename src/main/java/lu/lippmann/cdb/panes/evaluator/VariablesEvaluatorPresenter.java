/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.evaluator;

import java.util.*;

import javax.swing.JOptionPane;

import lu.lippmann.cdb.command.*;
import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.context.ApplicationContext;
import lu.lippmann.cdb.event.*;

import lu.lippmann.cdb.models.*;
import lu.lippmann.cdb.models.CVariable.CadralType;
import lu.lippmann.cdb.models.history.GraphWithOperations;
import lu.lippmann.cdb.repositories.CVariablesRepository;

import org.bushe.swing.event.annotation.EventSubscriber;

import com.google.inject.Inject;

import edu.uci.ics.jung.graph.Graph;


/**
 * VariablesEvaluatorPresenter.
 * 
 * @author the ACORA/WP1 team
 */
public class VariablesEvaluatorPresenter implements Presenter<VariablesEvaluatorView> 
{ 
	private final VariablesEvaluatorView view;

	@Inject 
	private ApplicationContext applicationContext;

	private final CommandDispatcher commandDispatcher;

	@Inject 
	private CVariablesRepository varRepo; 

	private Map<CVariable,String> currentEvaluationMap;

	/**
	 * Constructor.
	 */
	@Inject
	public VariablesEvaluatorPresenter(final VariablesEvaluatorView view,final CommandDispatcher commandDispatcher,final EventPublisher eventPublisher) 
	{
		this.view=view;
		this.commandDispatcher=commandDispatcher;
		this.currentEvaluationMap = new HashMap<CVariable, String>();
		eventPublisher.markAsEventListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() 
	{
		this.currentEvaluationMap.clear();

		this.view.addEvaluationListener(new Listener<Void>()
				{
			@Override
			public void onAction(Void parameter) 
			{
				final Graph<CNode,CEdge> g = applicationContext.getCadralGraph().getInternalGraph();
				final List<CInput> inputs = new ArrayList<CInput>();
				boolean hasError = false;
				/*for(CVariable var : getDefinedAndUsedVariables())
				{
					final CInput in = view.getVariablesValuesMap().get(var);
					if(in==null||in.getValue().toString().trim().equals("")){
						//Don't use : view.asComponent(), otherwise it creates two panel !!!
						JOptionPane.showMessageDialog(null, "Please fill the variable value : " + var.getKey() + " first ! ");
						hasError = true;
					}else{
						inputs.add(new CInput(var,in.getValue()));
					}
				}*/
				if(!hasError)
				{
					/*List<CEdge> res = null;
					try 
					{
						res = new GraphEvaluator(inputs).evaluate(g);
					}catch(RuntimeException e)
					{
						commandDispatcher.dispatch(new UnHighlightCommand());
						JOptionPane.showMessageDialog(null,e.getMessage());
					}
					finally 
					{
						if(res!=null && !res.isEmpty())
						{
							commandDispatcher.dispatch(new HighlightCommand(res));
						}
					}*/
				}				
			}
				});
	}

	/**
	 * Update current evaluation map
	 */
	private void updateEvaluationMap()
	{
		/*final Set<CVariable> s = ((GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph()).getVariables();		
		if(s!= null && !s.isEmpty()) updateVariablesTypeThatAreInRepo(s);

		this.currentEvaluationMap.clear();

		for (final CVariable cvar:s){
			if(view.getVariablesValuesMap().get(cvar)==null)
			{
				this.currentEvaluationMap.put(cvar,"");	
			}
			else
			{
				this.currentEvaluationMap.put(cvar,view.getVariablesValuesMap().get(cvar).getValue().toString());
			}
		}*/
	}


	/**
	 * return used variables in graph contained in repository
	 * @return
	 */
	private Set<CVariable> getDefinedAndUsedVariables()
	{
		final GraphWithOperations gwo = (GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph();
		return gwo.getVariables();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VariablesEvaluatorView getDisplay() {
		return view;
	}


	/**
	 * If we update the variable definition -> refresh view
	 * @param evt
	 */
	@EventSubscriber(eventClass = VariableDefinitionHasChangedEvent.class)
	public void onVariableChanged(final VariableDefinitionHasChangedEvent evt) 
	{
		System.out.println("Variable definition changed !");
		refreshView();
	}

	private void refreshView(){
		if (applicationContext.getCadralGraph()==null) return;
		final GraphWithOperations gwo = (GraphWithOperations)applicationContext.getCadralGraph().getInternalGraph();
		final Map<CVariable,String> defValMap = new HashMap<CVariable, String>();
		final Map<CVariable,List<String>> possValMap = new HashMap<CVariable, List<String>>();
		for(final CVariable var : gwo.getVariables())
		{
			if(var.getType().equals(CadralType.ENUMERATION) && !var.getValues().isEmpty())
			{
				defValMap.put(var,var.getValues().get(0)); 	//First value per default
				possValMap.put(var,var.getValues());		//All values
			}			
			else
			{
				currentEvaluationMap.put(var,"");
				defValMap.put(var,"");
				possValMap.put(var,null);
			}

		}
		view.setPossibleValuesMap(possValMap);
		view.setDefaultValuesMap(defValMap);

		try {
			final Map<CVariable,String> previousEvaluationMap     = new HashMap<CVariable, String>(this.currentEvaluationMap);
			updateEvaluationMap();

			//FIXME : change when values are added to enumeration list
			//if(hasMapChanged(previousEvaluationMap,this.currentEvaluationMap)){
			//if(true){
				view.reInit(previousEvaluationMap);
			//}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	/**
	 * Update variable found in graph with repository informations
	 * @param s
	 */
	private void updateVariablesTypeThatAreInRepo(Set<CVariable> vars) {
		final List<CVariable> repoVars = new ArrayList<CVariable>(varRepo.getCadralVariables());
		for(final CVariable var : vars){
			final int idx = repoVars.indexOf(var);
			if(idx!=-1){
				final CVariable repoVar = repoVars.get(idx);
				var.setDescription(repoVar.getDescription()); //useless for now
				var.setType(repoVar.getType());				  //to dynamically changed view
				if(repoVar.getValues()==null){
					var.setValues(new ArrayList<String>());
				}else{
					var.setValues(repoVar.getValues());
				}
			}
		}


	}

}


