/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.dt;

import lu.lippmann.cdb.graph.GraphUtil;
import lu.lippmann.cdb.models.CNode;
import lu.lippmann.cdb.models.history.GraphWithOperations;


/**
 * DecisionTree.
 * 
 * @author Olivier PARISOT
 */
public final class DecisionTree
{
	//
	// Instance variables
	//

	/** */
	private final GraphWithOperations gwo;
	/** */
	private final double errorRate;
	

	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public DecisionTree(final GraphWithOperations gwo,final double errorRate)
	{
		this.gwo=gwo;
		this.errorRate=errorRate;
	}

	/**
	 * Dummy Constructor.
	 */
	public DecisionTree(final String nodeName)
	{
		//If not classifier is provided, return a decision with a single node and no error rate
		this.errorRate=0.0d;
		
		//Decision Tree with a single node
		this.gwo=new GraphWithOperations();
		this.gwo.addVertex(new CNode(nodeName));
	}


	//
	// Instance methods
	//

	public double getErrorRate() 
	{
		return errorRate;
	}
	
	public double getMinPathLength() 
	{
		return GraphUtil.computeMinPathLength(getGraphWithOperations());
	}

	public double getMaxPathLength() 
	{
		return GraphUtil.computeMaxPathLength(getGraphWithOperations());
	}

	public int getDepth()
	{
		return GraphUtil.computeMaxPathLength(getGraphWithOperations())-1;
	}
	
	public int getSize() 
	{
		return gwo.getVertexCount();
	}

	public GraphWithOperations getGraphWithOperations() 
	{
		return gwo;
	}	
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb=new StringBuilder();
		sb.append("----");
		sb.append(" graph-size=").append(getSize());
		sb.append(" error-rate=").append(errorRate);
		sb.append(" minPathLength=").append(getMinPathLength());			
		sb.append(" maxPathLength=").append(getMaxPathLength());
		//sb.append(" graph={").append(getGraphWithOperations().getEdges()).append(' ').append(getGraphWithOperations().getVertices()).append('}');
		sb.append("----");
		return sb.toString();
	}
}