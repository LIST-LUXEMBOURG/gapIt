/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;
import java.util.*;

import lu.lippmann.cdb.models.history.*;

/**
 * 
 * @author didry
 *
 */
public class GraphTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7516699894878982454L;

	private Long graphId;
	
	private List<GraphOperation> operations;

	private int untitledVertexCount;
	private int untitledEdgeCount;
	
	/** used variable of the graph **/
	private Set<CVariable> variables;
	
	/**
	 * 
	 */
	public GraphTO(){
		this.graphId = 0l;
		this.operations = new ArrayList<GraphOperation>();
		this.untitledEdgeCount = 0;
		this.untitledVertexCount = 0;
		this.variables  = new HashSet<CVariable>();

	}
	
	/**
	 * 
	 * @param operations
	 * @param untitledVertexCount
	 * @param untitledEdgeCount
	 */
	public GraphTO(Long graphId,List<GraphOperation> operations, int untitledVertexCount,int untitledEdgeCount,Set<CVariable> variables) {
		super();
		this.graphId = graphId;
		this.operations = operations;
		this.untitledVertexCount = untitledVertexCount;
		this.untitledEdgeCount = untitledEdgeCount;
		this.variables = variables;
	}

	/**
	 * @return the operations
	 */
	public List<GraphOperation> getOperations() {
		return operations;
	}

	/**
	 * @param operations the operations to set
	 */
	public void setOperations(List<GraphOperation> operations) {
		this.operations = operations;
	}

	/**
	 * @return the untitledVertexCount
	 */
	public int getUntitledVertexCount() {
		return untitledVertexCount;
	}

	/**
	 * @param untitledVertexCount the untitledVertexCount to set
	 */
	public void setUntitledVertexCount(int untitledVertexCount) {
		this.untitledVertexCount = untitledVertexCount;
	}

	/**
	 * @return the untitledEdgeCount
	 */
	public int getUntitledEdgeCount() {
		return untitledEdgeCount;
	}

	/**
	 * @param untitledEdgeCount the untitledEdgeCount to set
	 */
	public void setUntitledEdgeCount(int untitledEdgeCount) {
		this.untitledEdgeCount = untitledEdgeCount;
	}

	
	
	/**
	 * @return the graphId
	 */
	public Long getGraphId() {
		return graphId;
	}

	/**
	 * @param graphId the graphId to set
	 */
	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}


	/**
	 * @return the variables
	 */
	public Set<CVariable> getVariables() {
		return variables;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(Set<CVariable> variables) {
		this.variables = variables;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "GraphTO [graphId=" + graphId + ", operations=" + operations
				+ ", untitledVertexCount=" + untitledVertexCount
				+ ", untitledEdgeCount=" + untitledEdgeCount + "]";
	}

	
	

}
