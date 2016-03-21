/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.evaluator;

import java.util.*;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.models.*;
import weka.experiment.Stats;


/**
 * VariablesEvaluatorView.
 * 
 * @author the ACORA/WP1 team
 */
public interface VariablesEvaluatorView extends Display 
{
	void reInit(Map<CVariable,String> varValues) throws IllegalStateException;

	void setPossibleValuesMap(Map<CVariable,List<String>> vars);

	void setDefaultValuesMap(Map<CVariable, String> defaultValuesMap);
	
	void setVariablesTooltip(Map<CVariable, Stats> statsForTooltipMap);
	
	Map<CVariable,CInput> getVariablesValuesMap();

	void addEvaluationListener(Listener<Void> listener);

	
}
