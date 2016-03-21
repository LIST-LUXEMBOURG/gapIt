/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.graph;

import java.util.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * 
 * @author didry
 *
 */
public class GraphConnexity<V,E> {

	private Graph<V,E> graph;

	private int countConnex;
	private Map<Integer,Graph<V,E>> graphs;

	/** indexOfNode --> component **/
	private List<Integer> numeroCfc;


	/**
	 * 
	 * @param nSucc
	 */
	public GraphConnexity(final Graph<V,E> graph){
		this.graph = graph;
		
		this.graphs    = new HashMap<Integer, Graph<V,E>>();
		this.numeroCfc = new ArrayList<Integer>();
		this.countConnex = 0;
		
		//Compute connexity components
		cfc();
	}

	/**
	 * 
	 * @return
	 */
	private void cfc(){
		final int n = graph.getVertexCount();

		int nbCfc = 0;
		int rang  = 0; 

		
		final List<Integer> theta	  	  = new ArrayList<Integer>();
		final List<Integer> liste	 	  = new ArrayList<Integer>();   
		final List<Boolean> visite	 	  = new ArrayList<Boolean>(); 
		final List<Boolean> empile	 	  = new ArrayList<Boolean>();

		//-------------------------------------
		//Initialize arraylists
		//-------------------------------------
		for(int i = 0 ; i < n ; i++){
			theta.add(-1);
			numeroCfc.add(-1);
			visite.add(false);
			empile.add(false);
		}


		int indexS = -1;
		for(final V node : graph.getVertices()){
			indexS =  indexOfNode(node);
			if(!visite.get(indexS)){
				nbCfc  = cfcdesc(node,nbCfc,theta,rang,liste,visite,empile);
			}
		}

		computeGraphs();
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private int indexOfNode(final V node){
		return new ArrayList<V>(graph.getVertices()).indexOf(node);
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	private V getNodeByIndex(final int index){
		return new ArrayList<V>(graph.getVertices()).get(index);
	}

	/**
	 * 
	 * @param s
	 * @param nbCfc
	 * @param numeroCfc
	 * @param theta
	 * @param rang
	 * @param liste
	 * @param visite
	 * @param empile
	 * @return
	 */
	private int cfcdesc(V s,int nbCfc, List<Integer> theta, int rang,	List<Integer> liste,
			List<Boolean> visite, List<Boolean> empile){

		int top = 0;

		//Add the visited node to the liste of nodes
		int indexOfs = indexOfNode(s);
		liste.add(0,indexOfs);

		//------------------------------------
		//Initiliaze arraylists
		//------------------------------------
		empile.set(indexOfs,true);
		visite.set(indexOfs,true);
		rang++;

		numeroCfc.set(indexOfs,rang);
		theta.set(indexOfs,rang);

		//Succ. of the node "s"
		final Collection<V> lSucc = this.graph.getNeighbors(s);
		int k					  = -1;
		if(lSucc!=null){
			for(V succ : lSucc){
				k = indexOfNode(succ);

				//If not already visited
				if(!visite.get(k)){
					nbCfc = cfcdesc(succ,nbCfc,theta,rang,liste,visite,empile);
					//theta[s] = min(theta[s],theta[k]);
					if(theta.get(k) < theta.get(indexOfs)){
						theta.set(indexOfs,theta.get(k));
					}
					//theta[s] = min(theta[s],theta[k]);
				}else if(empile.get(k) && theta.get(k) < theta.get(indexOfs)){
					theta.set(indexOfs,theta.get(k));
				}
			}
		}

		//Compute numeroCfc
		if(theta.get(indexOfs)==numeroCfc.get(indexOfs)){
			do{
				top = liste.get(0);
				liste.remove(0);
				empile.set(top,false); 
				numeroCfc.set(top,nbCfc);
			}while(top != indexOfs);

			nbCfc++;
			this.countConnex++;
		}
		return nbCfc;
	}

	/**
	 * Compute sub-graph using num. of each component
	 * @param numeroCfc
	 */
	private void computeGraphs() {
		int index = 0;
		for(final Integer component : numeroCfc){
			//Add a graph at this component if none found
			if(this.graphs.get(component)==null){
				graphs.put(component,new DirectedSparseGraph<V,E>());
			}
			
			//Add the corresponding node & edges
			final Graph<V,E> subGraph = graphs.get(component);
			final V src = getNodeByIndex(index);
			subGraph.addVertex(src);
			for(final E edge : graph.getIncidentEdges(src)){
				if(graph instanceof UndirectedSparseGraph){
					subGraph.addEdge(edge, graph.getEndpoints(edge));
				}else{
					subGraph.addEdge(edge, graph.getSource(edge), graph.getDest(edge), EdgeType.DIRECTED);
				}
			}
			index++;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getCountConnex() {
		return countConnex;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public Graph<V,E> getSubGraph(V node){
		return graphs.get(numeroCfc.get(indexOfNode(node)));
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<Integer, Graph<V, E>> getGraphs() {
		return graphs;
	}
	

}
