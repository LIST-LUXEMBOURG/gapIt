/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import org.ejml.simple.SimpleMatrix;

/**
 * 
 * @author didry
 *
 */
public final class ProjectionStressUtil {
	
	/**
	 * 
	 */
	private ProjectionStressUtil()
	{
		throw new IllegalStateException("Can't instantiate !");
	}
	
	/**
	 * 
	 */
	public static double getKruskalStressFromProjection(final SimpleMatrix initialDistances,final SimpleMatrix newCoordinates)
	{
		double sum1=0,sum2=0;
		final SimpleMatrix oldDistances = MatrixUtil.rescaleDistanceMatrix(initialDistances);
		final SimpleMatrix newDistances =  MatrixUtil.rescaleDistanceMatrix(MatrixUtil.dist(newCoordinates));
		//System.out.println("old="+initialDistances.getNumElements()+" new="+newDistances.getNumElements());
		final int N = initialDistances.numRows();
		for(int i = 0 ; i < N ; i++){
			for(int j = i+1 ; j < N ; j++){
				sum1+=Math.pow(oldDistances.get(i,j)-newDistances.get(i,j),2);
				sum2+=Math.pow(oldDistances.get(i,j),2);
			}
		}
		return Math.sqrt(sum1/sum2);
	}

}
