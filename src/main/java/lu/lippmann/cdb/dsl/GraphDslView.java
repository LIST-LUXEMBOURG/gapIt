/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import java.util.*;

import lu.lippmann.cdb.common.mvp.*;
import lu.lippmann.cdb.models.CVariable;


/**
 * Graph DSL view.
 * 
 * @author Olivier PARISOT
 */
public interface GraphDslView extends Display 
{	
	void reInit();

	boolean isVisible();

	void setVisible(boolean b);
	
	void setOnDslStringChangedListener(Listener<String> listener);

	void setDslString(String dslString);

	void setDslFormat(String dslFormat);
	
	void setDslKeywords(String[] dslKeywords);

	void setVariables(Set<CVariable> variables);

	void updateLinesWithError(Map<Integer,String> linesWithError);

	void setCommentMarker(String commentMarker);
}
