/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.models;

import java.io.Serializable;

/**
 * 
 * @author didry
 *
 */
public class CRelation implements Serializable {

	private static final long serialVersionUID = -131550693286939831L;

	private CEdge edge;

	private CNode source;
	private CNode dest;

	/**
	 * 
	 */
	public CRelation(){
		super();
	}

	/**
	 * 
	 * @param e
	 * @param source
	 * @param dest
	 */
	public CRelation(CEdge e, CNode s, CNode d){
		this.edge 		 = e;
		this.source 	 = s;
		this.dest		 = d;
	}

	/**
	 * @return the edge
	 */
	public CEdge getEdge() {
		return edge;
	}

	/**
	 * @param edge the edge to set
	 */
	public void setEdge(CEdge edge) {
		this.edge = edge;
	}

	/**
	 * @return the source
	 */
	public CNode getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(CNode source) {
		this.source = source;
	}

	/**
	 * @return the dest
	 */
	public CNode getDest() {
		return dest;
	}

	/**
	 * @param dest the dest to set
	 */
	public void setDest(CNode dest) {
		this.dest = dest;
	}

}
