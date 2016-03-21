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
import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;


/**
 * 
 * @author didry
 *
 * @param <V>
 * @param <E>
 */
public interface GenericGraphView<V,E> extends Display {
	
	/**
	 * 
	 * @author
	 *
	 */
	public enum ViewMode 
	{
		Add,Edit,Tag;//, Lens;

		public static List<String> valuesAsStringsList() 
		{
			final ViewMode[] values=values();
			final List<String> l=new ArrayList<String>(values.length);
			for (ViewMode vm:values) l.add(vm.name());
			return l;
		}
	};

	void init();
	
	ViewMode getViewMode();
	void setViewMode(ViewMode mode);

	void setCGraph(GenericCGraph<V,E> cgraph);
	Graph<V,E> getGraph();

	void highlightPath(List<E> path);
	
	void selectAll(boolean doSelect);

	void reorganize();

	void deleteSelected();

	void repaint();

	VisualizationViewer<V,E> getVisualisationViewer();

	int getUniqueId();

	void fitGraphToSubPanel(double panelWidth, double panelHeigth, double ratioPanel);
	
	void setBackground(Color color);

	void addMetaInfo(String title, String content);
	void addMetaInfoComponent(Component c);
	
	void resetLayout();

	void setShape(CShape cs);

	void setColor(Color co);

	void updateEdgeShapeRenderer(Map<E, Float> map);
	void updateVertexShapeTransformer(Map<V, Map<Object, Integer>> map);

	void resetVertexAndEdgeShape();

	void autoFit();
	void clearMetaInfo();

	
	
}
