/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.panes.editor;

import lu.lippmann.cdb.common.mvp.Display;
import lu.lippmann.cdb.models.CNode;

/**
 * 
 * @author
 *
 */
public interface NodeEditorView extends Display {
	void setNode(CNode node);
	void focusOnField();
}
