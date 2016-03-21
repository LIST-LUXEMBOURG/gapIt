/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.awt.*;
import java.util.*;
import java.util.List;

import lu.lippmann.cdb.common.mvp.Display;
import lu.lippmann.cdb.graph.GenericGraphView.ViewMode;
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;


/**
 * 
 * 
 * @author
 *
 */
public interface GraphView extends Display {

	void init();
	
	ViewMode getViewMode();
	void setViewMode(ViewMode mode);

	void setCGraph(CGraph cgraph);
	Graph<CNode,CEdge> getGraph();

	void highlightPath(List<CEdge> path);
	
	void selectAll(boolean doSelect);

	void reorganize();

	void deleteSelected();

	void repaint();

	VisualizationViewer<CNode,CEdge> getVisualisationViewer();

	int getUniqueId();

	void fitGraphToSubPanel(double panelWidth, double panelHeigth, double ratioPanel);
	
	void setBackground(Color color);

	void addMetaInfo(String title, String content);
	void addMetaInfoComponent(Component c);
	
	void resetLayout();

	void setShape(CShape cs);

	void setColor(Color co);

	void updateEdgeShapeRenderer(Map<CEdge, Float> map);
	void updateVertexShapeTransformer(Map<CNode, Map<Object, Integer>> map);

	void resetVertexAndEdgeShape();

	void autoFit();
	void clearMetaInfo();

	void hideSharedLabel();

	
	
}
