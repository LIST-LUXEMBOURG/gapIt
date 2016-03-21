/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;
import weka.core.*;


/**
 * WekaClusteringResult.
 *
 * @author the WP1 team
 */
public final class WekaClusteringResult
{
	//
	// Instance fields
	//
	
	/** */
	private final double[] ass;
	/***/
	private final List<Instances> clustersList;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public WekaClusteringResult(final double[] ass,final List<Instances> clustersList)
	{
		this.ass=ass;
		this.clustersList=clustersList;
	}

	/**
	 * Constructor.
	 */
	public WekaClusteringResult(final WekaClusteringResult cr)
	{
		this.ass=new double[cr.ass.length];
		System.arraycopy(cr.ass,0,this.ass,0,cr.ass.length);
		this.clustersList=new ArrayList<Instances>(cr.clustersList.size());
		for (int i=0;i<cr.clustersList.size();i++) this.clustersList.add(new Instances(cr.clustersList.get(i))); 
	}
	
	
	//
	// Instance methods
	//
	
	public double[] getAss() 
	{
		return this.ass;
	}

	public List<Instances> getClustersList() 
	{
		return this.clustersList;
	}
	
	public void applyMove(final Move pm) throws Exception
	{	
		if (this.ass[pm.idx]!=pm.sourceCluster) throw new Exception("Impossible move");

		final int relIdx=computeRelativeIdxInCluster(this.ass,pm.targetCluster,pm.idx);
		final int oldrelIdx=computeRelativeIdxInCluster(this.ass,pm.sourceCluster,pm.idx);		
		final Instance instToMove=this.clustersList.get(pm.sourceCluster).instance(oldrelIdx);
		
		if (relIdx>=0) this.clustersList.get(pm.targetCluster).add(relIdx,new DenseInstance(1.0d,instToMove.toDoubleArray()));
		else this.clustersList.get(pm.targetCluster).add(new DenseInstance(1.0d,instToMove.toDoubleArray()));
		this.clustersList.get(pm.sourceCluster).remove(pm.relativeCurrentClusterIdx);		
		this.ass[pm.idx]=pm.targetCluster;				
	}

	public Instance getInstance(final int idx)
	{
		final int cluster=(int)this.ass[idx];
		final int relIdx=computeRelativeIdxInCluster(this.ass,cluster,idx);
		return this.clustersList.get(cluster).instance(relIdx);
	}
	
	
	//
	// Static methods
	//
	
	public static int computeRelativeIdxInCluster(final double[] ass,final int c,final int assIdx)
	{
		int idx=-1;
		for (int i=0;i<=assIdx;i++)
		{
			if (ass[i]==c) idx++;
		}		
		return idx;
	}

	
	//
	// Static classes
	//
	
	/**
	 * Move.	 	 
	 */
	public static class Move
	{
		private final int idx;
		private final int sourceCluster;
		private final int relativeCurrentClusterIdx;
		private final int targetCluster;
		private double impact;
		
		public Move(final int idx,final int sourceCluster,final int relativeCurrentClusterIdx,final int targetCluster,final double impact) 
		{			
			this.idx=idx;
			this.sourceCluster=sourceCluster;
			this.relativeCurrentClusterIdx=relativeCurrentClusterIdx;
			this.targetCluster=targetCluster;
			this.impact=impact;
		}

		@Override
		public String toString()
		{
			return "{"+idx+",src="+sourceCluster+",trgt="+targetCluster+",impct="+impact+"}";
		}

		public double getImpact() 
		{
			return impact;
		}
	}
}