/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.variables;

import java.awt.Component;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.models.CVariable;


/**
 * 
 *
 *
 * @author 
 *
 */
public interface CVariableEditorView extends Display 
{
	void setCVariable(CVariable cv);
	
	void setAlreadyInRepository(boolean alreadyInRepository);
	
	void setOnCVariableUpdateListener(Listener<CVariable> var);

	void setVisible(boolean b);

	void reInit(Component parent);

	boolean hasToBeDeletedInRepository();
	
	boolean hasToBeSavedInRepository();

	

	
}
