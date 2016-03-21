/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import lu.lippmann.cdb.models.history.GraphWithOperations;
import weka.core.Instances;

/**
 * 
 * @author Didry
 *
 */
public class GraphLinkedWithDataSetEvent implements AbstractEvent {

	private GraphWithOperations graph;
	private Instances dataSet;
	
	/**
	 * 
	 * @param graph
	 * @param dataSet
	 */
	public GraphLinkedWithDataSetEvent(final GraphWithOperations graph,final Instances dataSet) {
		super();
		this.graph = graph;
		this.dataSet = dataSet;
	}
	/**
	 * @return the graph
	 */
	public GraphWithOperations getGraph() {
		return graph;
	}
	/**
	 * @return the dataSet
	 */
	public Instances getDataSet() {
		return dataSet;
	}
	
	
	
}
