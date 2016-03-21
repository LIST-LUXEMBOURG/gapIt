/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models.history;

import java.awt.Color;
import java.io.Serializable;
import java.util.*;

import lu.lippmann.cdb.graph.renderer.CShape;
import lu.lippmann.cdb.models.*;


/**
 * Graph operation.
 * 
 * @author Yoann DIDRY, Olivier PARISOT
 */
public final class GraphOperation implements Serializable {

	//
	// Static fields
	//

	/** Serial version UID. */
	private static final long serialVersionUID=14528864422L;
	/** */
	private static final Random RANDOM = new Random();


	//
	// Instance fields
	//

	/** */
	private CUser user;
	/** */
	private final Long id;
	/** */
	private Long idGroup;
	/** */
	private Operation operation;
	/** */
	private ArrayList<Object> parameters;


	/** shared untitled label count **/
	private int untitledVertexCount;
	private int untitledEdgeCount;

	//
	// Constructors
	//


	/**
	 * Constructor.
	 */
	public GraphOperation(final int vcount,final int ecount,final CUser user,final Operation operation,final ArrayList<Object> parameters) {
		this.user=user;
		this.untitledVertexCount = vcount;
		this.untitledEdgeCount   = ecount;
		this.operation = operation;
		this.parameters = parameters;
		this.id = RANDOM.nextLong();
	}

	/**
	 * Constructor.
	 */
	public GraphOperation() 
	{
		this(0,0,null,null,null);
	}

	//
	// Instance methods
	//

	/**
	 * @param user the user to set
	 */
	public void setUser(final CUser user) {
		this.user = user;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(final Operation operation) {
		this.operation = operation;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(final ArrayList<Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * 
	 * @return the user
	 */
	public CUser getUser() {
		return user;
	}

	/**
	 * 
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return the parameters
	 */
	public ArrayList<Object> getParameters() {
		return parameters;
	}

	/**
	 * set the id group
	 * @param idGroup
	 */
	public void setIdGroup(Long idGroup){
		this.idGroup = idGroup;
	}

	/**
	 *@return the idGroup
	 */
	public Long getIdGroup(){
		return idGroup;
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String res = "";
		switch(operation){
		case NODE_ADDED:
			res = "+Node:"+((CNode)parameters.get(0)).getName();
			break;
		case EDGE_ADDED:
			final CEdge tmpEdge11 = ((CEdge)parameters.get(0));
			res = "+Arc:"+tmpEdge11.getName()+"("+tmpEdge11.getExpression()+")";
			break;
		case NODE_REMOVED:
			res = "-Node:"+ ((CNode)parameters.get(0)).getName();
			break;
		case EDGE_REMOVED:
			final CEdge tmpEdge0 = ((CEdge)parameters.get(0));
			res = "-Arc:"+ tmpEdge0.getName()+"("+tmpEdge0.getExpression()+")";
			break;
		case NODE_DATA_UPDATED:
			final String  tmpLabel1  = (String)parameters.get(1);
			final String  tmpLabel2  = (String)parameters.get(2);
			final CShape  tmpShape1 = (CShape)parameters.get(3);
			final CShape  tmpShape2 = (CShape)parameters.get(4);
			final Color   tmpColor1 = (Color)parameters.get(5);
			final Color   tmpColor2 = (Color)parameters.get(6);
			final Object  tmpTags1 = parameters.get(7);
			final Object  tmpTags2 = parameters.get(8);
			if(!tmpLabel1.equals(tmpLabel2)){
				res = "~Node label:"+tmpLabel1+"->"+tmpLabel2;
			}else if(!tmpShape1.equals(tmpShape2)){
				res = "~Node '"+tmpLabel2+"' shape:"+tmpShape1+"->"+tmpShape2;
			}else if(!tmpColor1.equals(tmpColor2)){
				res = "~Node '"+tmpLabel2+"' color";
			}else if(!tmpTags1.equals(tmpTags2)){
				res = "~Node '"+tmpLabel2+"' tags";
			}else{
				res = "~Node '"+tmpLabel2+"' unk change";
			}
			break;
		case EDGE_DATA_UPDATED:
			final String  tmpELabel1  = (String)parameters.get(1);
			final String  tmpELabel2  = (String)parameters.get(2);
			final String  tmpExpression1  = (String)parameters.get(3);
			final String  tmpExpression2  = (String)parameters.get(4);			
			final Object  tmpETags1 = parameters.get(5);
			final Object  tmpETags2 = parameters.get(6);
			if(!tmpELabel1.equals(tmpELabel2)){
				res = "~Arc label:"+tmpELabel1+"->"+tmpELabel2;
			}else if(!tmpExpression1.equals(tmpExpression2)){
				res = "~Arc '"+tmpELabel2+"' expression:"+tmpExpression1+"->"+tmpExpression2;
			}else if(!tmpETags1.equals(tmpETags2)){
				res = "~Arc '"+tmpELabel2+"' tags";
			}else{
				res = "~Arc '"+tmpELabel2+"' unk change";
			}
			break;
		case NODE_MOVED:
			res = ((CNode)parameters.get(0)).getName()+ " moved ";
			break;
		case LAYOUT_CHANGED:
			res = "Graph reorganized";
			break;
		case VARIABLE_CHANGED:
			final CVariable  tmpVar1 = (CVariable)parameters.get(0);
			final CVariable  tmpVar2 = (CVariable)parameters.get(1);
			if(!tmpVar1.getType().equals(tmpVar2.getType())){
				res = "~Variable '"+tmpVar1.getKey()+"' type changed : " + tmpVar1.getType() + "->" + tmpVar2.getType();
			}else if(!tmpVar1.getDescription().equals(tmpVar2.getDescription())){
				res = "~Variable '"+tmpVar1.getKey()+"' description changed : " + tmpVar1.getDescription() + "->" + tmpVar2.getDescription();
			}
			else if(tmpVar1.getValues()!=null || tmpVar2.getValues() != null && !tmpVar1.getValues().equals(tmpVar2.getValues())){
				res = "~Variable '"+tmpVar1.getKey()+"' values changed : " + tmpVar1.getValues().size() + "->" + tmpVar2.getValues().size();
			}
			else{
				res = "~Variable '"+tmpVar1.getKey()+"' changed";
			}
			break;
		default:
			break;
		}
		return "<html>"+res+"</html>";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GraphOperation) {
			final GraphOperation newObj = (GraphOperation) obj;
			return (newObj.getId().equals(this.id));
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
