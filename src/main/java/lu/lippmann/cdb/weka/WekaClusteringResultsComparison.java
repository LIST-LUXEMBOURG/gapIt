/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.weka;

import java.util.*;
import weka.core.*;
import lu.lippmann.cdb.common.*;


/**
 * WekaClusteringResultsComparison.
 * Mainly based on 'http://i11www.iti.uni-karlsruhe.de/extra/publications/ww-cco-06.pdf'.
 * 
 * @author the WP1 team
 */
public final class WekaClusteringResultsComparison 
{
	//
	// Instance fields
	//
	
	/** Pair counting confusion matrix (flat: inBoth, inFirst, inSecond, inNone). */
	private final long[] pairconfuse=new long[4];

	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public WekaClusteringResultsComparison(final WekaClusteringResult cr1,final WekaClusteringResult cr2) 
	{
		final boolean[][] b1=computePairsProximityMatrix(cr1.getAss());
		final boolean[][] b2=computePairsProximityMatrix(cr2.getAss());
						
		this.pairconfuse[0]=countByPairsProximity(b1,b2,CCMode.IN_BOTH);
		this.pairconfuse[1]=countByPairsProximity(b1,b2,CCMode.IN_FIRST);
		this.pairconfuse[2]=countByPairsProximity(b1,b2,CCMode.IN_SECOND);
		this.pairconfuse[3]=countByPairsProximity(b1,b2,CCMode.IN_NONE);
	}
	
	
	//
	// Instance methods
	//
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb=new StringBuilder();
		sb.append("TOTAL: ").append(MathsUtil.sum(this.pairconfuse)).append('\n');
		sb.append("IN BOTH: ").append(this.pairconfuse[0]).append('\n');
		sb.append("IN FIRST: ").append(this.pairconfuse[1]).append('\n');
		sb.append("IN SECOND: ").append(this.pairconfuse[2]).append('\n');
		sb.append("IN NONE: ").append(this.pairconfuse[3]).append('\n');		
		sb.append("\n\n");
		sb.append("Measures based on counting pairs: \n");		
		sb.append("fowlkesMallows: ").append(this.fowlkesMallows()).append('\n');
		sb.append("jaccard: ").append(this.jaccard()).append('\n');
		sb.append("mirkin: ").append(this.mirkin()).append('\n');
		sb.append("partitionDifference: ").append(this.partitionDifference()).append('\n');
		sb.append("adjustedRandIndex: ").append(this.adjustedRandIndex()).append('\n');
		sb.append("randIndex: ").append(this.randIndex()).append('\n');
		sb.append("precision: ").append(this.precision()).append('\n');
		sb.append("recall: ").append(this.recall()).append('\n');
		sb.append("\n\n");
		sb.append("Measures based on set overlaps: \n");
		sb.append("f1Measure: ").append(this.f1Measure()).append('\n');
		return sb.toString();
	}
	
	/**
	 * Get the pair-counting F-Measure.
	 * 
	 * @param beta
	 *            Beta value.
	 * @return F-Measure
	 */
	public double fMeasure(final double beta) 
	{
		final double beta2 = beta * beta;
		double fmeasure = ((1 + beta2) * pairconfuse[0])
				/ ((1 + beta2) * pairconfuse[0] + beta2 * pairconfuse[1] + pairconfuse[2]);
		return fmeasure;
	}

	/**
	 * Get the pair-counting F1-Measure.
	 * 
	 * @return F1-Measure
	 */
	public double f1Measure() 
	{
		return fMeasure(1.0);
	}

	/**
	 * Computes the pair-counting precision.
	 * 
	 * @return pair-counting precision
	 */
	public double precision() 
	{
		return ((double) pairconfuse[0]) / (pairconfuse[0] + pairconfuse[2]);
	}

	/**
	 * Computes the pair-counting recall.
	 * 
	 * @return pair-counting recall
	 */
	public double recall() 
	{
		return ((double) pairconfuse[0]) / (pairconfuse[0] + pairconfuse[1]);
	}

	/**
	 * Computes the pair-counting Fowlkes-mallows (flat only, non-hierarchical!)
	 * 
	 * <p>
	 * Fowlkes, E.B. and Mallows, C.L.<br />
	 * A method for comparing two hierarchical clusterings<br />
	 * In: Journal of the American Statistical Association, Vol. 78 Issue 383
	 * </p>
	 * 
	 * @return pair-counting Fowlkes-mallows
	 */
	public double fowlkesMallows() 
	{
		return Math.sqrt(precision() * recall());
	}

	/**
	 * Computes the Rand index (RI).
	 * 
	 * <p>
	 * Rand, W. M.<br />
	 * Objective Criteria for the Evaluation of Clustering Methods<br />
	 * Journal of the American Statistical Association, Vol. 66 Issue 336
	 * </p>
	 * 
	 * @return The Rand index (RI).
	 */
	public double randIndex() 
	{
		final double sum = pairconfuse[0] + pairconfuse[1] + pairconfuse[2]
				+ pairconfuse[3];
		return (pairconfuse[0] + pairconfuse[3]) / sum;
	}

	/**
	 * Computes the adjusted Rand index (ARI).
	 * 
	 * @return The adjusted Rand index (ARI).
	 */
	public double adjustedRandIndex() 
	{
		final double nom = pairconfuse[0] * pairconfuse[3] - pairconfuse[1]
				* pairconfuse[2];
		final long d1 = (pairconfuse[0] + pairconfuse[1])
				* (pairconfuse[1] + pairconfuse[3]);
		final long d2 = (pairconfuse[0] + pairconfuse[2])
				* (pairconfuse[2] + pairconfuse[3]);
		return 2 * nom / (d1 + d2);
	}

	/**
	 * Computes the Jaccard index.
	 * 
	 * @return The Jaccard index
	 */
	public double jaccard() 
	{
		final double sum = pairconfuse[0] + pairconfuse[1] + pairconfuse[2];
		return pairconfuse[0] / sum;
	}

	/**
	 * Computes the Mirkin index.
	 * 
	 * @return The Mirkin index
	 */
	public long mirkin() 
	{
		return 2 * (pairconfuse[1] + pairconfuse[2]);
	}
	
	/**
	 * Computes the Partition Difference.
	 * Commonly used according to 'http://i11www.iti.uni-karlsruhe.de/extra/publications/ww-cco-06.pdf'.
	 * Used as 'distance' between clustering results.	 
	 * 
	 * @return the Partition Difference
	 */
	public long partitionDifference() 
	{
		return pairconfuse[3];
	}
	
	//
	// Static methods
	//
	
	/**
	 * Matrix describing items which are in the same cluster.
	 */
	static boolean[][] computePairsProximityMatrix(final double[] ass)
	{
		final boolean[][] r=new boolean[ass.length][ass.length];
		for (int i=0;i<ass.length;i++)
		{
			for (int j=0;j<ass.length;j++) // TODO: optimize this loop?
			{
				r[i][j]=((int)ass[i]==(int)ass[j]);
			}
		}
		return r;
	}
	
	private static long countByPairsProximity(final boolean[][] pairsProximityMatrix1,final boolean[][] pairsProximityMatrix2,final CCMode mode)
	{
		if (pairsProximityMatrix1.length!=pairsProximityMatrix2.length) throw new IllegalArgumentException();
		if (pairsProximityMatrix1.length!=pairsProximityMatrix1[0].length) throw new IllegalArgumentException();
		
		final int l=pairsProximityMatrix1.length;
		
		long c=0;
		for (int i=0;i<l;i++)
		{
			for (int j=0;j<l;j++) // TODO: optimize this loop?
			{
				final boolean inFirst=pairsProximityMatrix1[i][j];
				final boolean inSecond=pairsProximityMatrix2[i][j];
				if (mode==CCMode.IN_BOTH)
				{
					if (inFirst&&inSecond) c++;
				}
				else if (mode==CCMode.IN_FIRST)
				{
					if (inFirst&&!inSecond) c++;
				}
				else if (mode==CCMode.IN_SECOND)
				{
					if (!inFirst&&inSecond) c++;
				}
				else if (mode==CCMode.IN_NONE)
				{
					if (!inFirst&&!inSecond) c++;
				}
				else throw new IllegalStateException();
			}
		}
		return c;
	}
	
	/**
	 * Build the diff of two clustering where there is a good correspondance between clusters labels.
	 */
	public static Instances buildDiff(final WekaClusteringResult cr1,final WekaClusteringResult cr2) 
	{
		if (cr1.getAss().length!=cr2.getAss().length) throw new IllegalArgumentException();
		
		final Map<Instance,Boolean> inTheSameCluster=new HashMap<Instance,Boolean>();
		for (int i=0;i<cr1.getAss().length;i++)
		{
			inTheSameCluster.put(cr1.getInstance(i),cr1.getAss()[i]==cr2.getAss()[i]);
		}
	
		final Instances newds=new Instances(inTheSameCluster.keySet().iterator().next().dataset(),0);
		newds.insertAttributeAt(new Attribute("status",Arrays.asList("NOT_SAME","SAME")),newds.numAttributes());
		newds.setClassIndex(newds.numAttributes()-1);		
		
		for (Map.Entry<Instance,Boolean> entry:inTheSameCluster.entrySet())
		{
			final double[] array=ArraysUtil.concat(entry.getKey().toDoubleArray(),new double[]{entry.getValue()?1d:0d});
			Instance newinst=new DenseInstance(1d,array);
			newinst.setDataset(newds);
			newds.add(newinst);
		}
			
		return newds;
	}
	
	//
	// Inner classes
	// 
	
	private enum CCMode 
	{
		IN_BOTH,IN_FIRST,IN_SECOND,IN_NONE;
	}
}
