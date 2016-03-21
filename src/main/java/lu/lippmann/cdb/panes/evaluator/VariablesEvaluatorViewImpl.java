/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.evaluator;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.common.guice.util.AutoBind;
import lu.lippmann.cdb.common.mvp.Listener;
import lu.lippmann.cdb.models.*;

import org.jdesktop.swingx.*;

import weka.experiment.Stats;


/**
 * VariablesEvaluatorViewImpl.
 * 
 * @author the ACORA/WP1 team
 */
@AutoBind
public class VariablesEvaluatorViewImpl extends JPanel implements VariablesEvaluatorView 
{
	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=1931691L;


	//
	// Instance fields
	//

	private Map<CVariable,List<String>> variablesAndPossibleValues;
	private Map<CVariable, String> defaultValuesMap;

	private Map<CVariable,CInput> variablesValuesMap = new HashMap<CVariable,CInput>();

	private Listener<Void> evaluationListener;


	private Map<CVariable, Stats> variablesTooltip;


	private JScrollPane scrollPane = null;


	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException 
	 */
	@Override
	public void reInit(final Map<CVariable,String> previousVarValues) throws IllegalStateException
	{
		initComponents(previousVarValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPossibleValuesMap(final Map<CVariable,List<String>> variablesAndPossibleValues)
	{
		this.variablesAndPossibleValues=variablesAndPossibleValues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultValuesMap(final Map<CVariable,String> defaultValuesMap) 
	{
		this.defaultValuesMap=defaultValuesMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVariablesTooltip(Map<CVariable, Stats> statsForTooltipMap) {
		this.variablesTooltip = statsForTooltipMap;
	}

	/**
	 * 
	 * @param previousVarValues
	 * @throws IllegalStateException 
	 */
	private void initComponents(final Map<CVariable,String> previousVarValues) throws IllegalStateException 
	{
		
		variablesValuesMap.clear();
			
		removeAll();

		setLayout(new BorderLayout());

		if (variablesAndPossibleValues!=null&&!variablesAndPossibleValues.isEmpty())
		{

			//If no default value, we set an empty string
			final Set<CVariable> keySet=variablesAndPossibleValues.keySet();
			if(defaultValuesMap==null)
			{
				defaultValuesMap = new HashMap<CVariable, String>();
				for(final CVariable var : keySet)
				{
					defaultValuesMap.put(var, "");	
				}
			}
			//--------------------------------------------

			final JPanel intern=new JPanel();			

			intern.setLayout(new GridLayout(keySet.size(),0));
			for (final CVariable cv:keySet)
			{
				intern.add(new JLabel(cv.getKey()));
				switch (cv.getType())
				{
				  case UNKNOWN:
				  case NUMERIC:
					String varValue;
					if(previousVarValues.containsKey(cv))
					{
						//First time before structure changed : we take the previously evaluation, if any
						if(previousVarValues.get(cv).equals("") && variablesValuesMap.containsKey(cv))
						{
							varValue = variablesValuesMap.get(cv).getValue().toString();	
						}else
						{
							varValue = previousVarValues.get(cv);	
						}

					}else{
						varValue = defaultValuesMap.get(cv);
					}
					final JTextField textField=new JTextField(varValue);
					variablesValuesMap.put(cv,new CInput(cv,textField.getText()));
					intern.add(textField);
					textField.addFocusListener(new FocusAdapter() 
					{
						@Override
						public void focusLost(FocusEvent e) 
						{
							super.focusLost(e);
							variablesValuesMap.put(cv,new CInput(cv,textField.getText()));
						}
					});

					//Set tooltip using weka statistics (if provided !)
					if(variablesTooltip!=null){
						final Stats wekaStats = variablesTooltip.get(cv);
						final String toolTip="<html>Range: ["+wekaStats.min+","+wekaStats.max+"]<br/>"+
								"Average: "+FormatterUtil.DECIMAL_FORMAT.format(wekaStats.mean)
								+"</html>";
						textField.setToolTipText(toolTip);
					}
					break;
				  case BOOLEAN:
					final JCheckBox t = new JCheckBox("True");
					final JCheckBox f = new JCheckBox("False");
					t.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							variablesValuesMap.put(cv,new CInput(cv,"True"));
							f.setSelected(false);
						}
					});
					f.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							variablesValuesMap.put(cv,new CInput(cv,"False"));
							t.setSelected(false);
						}
					});
					
					final JPanel p = new JPanel();
					p.add(t);
					p.add(f);
					intern.add(p);
					break;
				  case ENUMERATION:
					//Search why it's called with a wrong map (and VariableDefinitionHasChangedEvent twice)!
					List<String> possibleValues =  variablesAndPossibleValues.get(cv);
					if(possibleValues==null) possibleValues = cv.getValues();
					//----------------------------------------
					final JComboBox combo=new JComboBox(possibleValues.toArray());
					combo.setSelectedItem(defaultValuesMap.get(cv));					
					variablesValuesMap.put(cv,new CInput(cv,combo.getSelectedItem().toString()));
					combo.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							variablesValuesMap.put(cv,new CInput(cv,combo.getSelectedItem().toString()));							
						}
					});
					intern.add(combo);
					break;
				  default:
					break;
				}
			}
		
			add(new JScrollPane(intern),BorderLayout.CENTER);

			final JButton evalBtn=new JButton("Evaluate");
			evalBtn.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(final ActionEvent e) 
				{
					evaluationListener.onAction(null);
				}
			});
			add(evalBtn,BorderLayout.SOUTH);

			/*final int paneHeight=Math.min(30*cadralVariables.size(),200);
			setPreferredSize(new Dimension(150,paneHeight));*/
		}
		else
		{
			add(new JXLabel("Nothing to evaluate!"));
		}

		updateUI();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component asComponent() 
	{
		final JXTaskPane panel=new JXTaskPane();
		//panel.setLayout(new GridLayout(1,1));
		if (scrollPane == null) {
			scrollPane = new JScrollPane(this);
		}
		//scrollPane.setPreferredSize(new Dimension(150,200));
		panel.add(scrollPane);
		panel.setTitle("Evaluation");
		panel.setCollapsed(true);
		return panel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<CVariable,CInput> getVariablesValuesMap() 
	{
		return variablesValuesMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEvaluationListener(final Listener<Void> listener) 
	{
		this.evaluationListener=listener;		
	}


}

