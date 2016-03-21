/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.variables;

import java.util.Set;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.models.CVariable;


/**
 * 
 * 
 * @author
 */
public interface VariablesEditorView extends Display 
{
	void reInit();
	
	void setDefinedAndUsedCVariables(Set<CVariable> v);

	void setDefinedButNotUsedCVariables(Set<CVariable> v);
	
	void setUsedButNotDefinedCVariables(Set<CVariable> v);
	
	void setOnAskCVariableUpdateListener(Listener<CVariable> listener);
}
