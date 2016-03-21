/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dsl;

import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.graph.Graph;


/**
 * Abstract definition of a cgraph DSL (Domain-Specific Language).
 *
 * @author Olivier PARISOT
 */
public interface GraphDsl 
{
	String getDslString(Graph<CNode,CEdge> cgraph);
	
	GraphDslParsingResult getGraphDslParsingResult(String dslStringFormat);
	
	String[] getKeywords();
	
	String getCommentMarker();
	
	String getDslFormat();
}
