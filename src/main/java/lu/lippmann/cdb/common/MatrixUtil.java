/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common;

import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;


/**
 * 
 * @author didry
 *
 */
public final class MatrixUtil {


	private MatrixUtil(){
		throw new UnsupportedOperationException();
	}

	/**
	 * Compute the pairwise distance
	 * @param D
	 * @return
	 */
	public static SimpleMatrix dist(final SimpleMatrix D){
		int weight[] = new int[D.numCols()];
		Arrays.fill(weight,1);
		return dist(D,weight);                     
	}
	
	public static SimpleMatrix dist(final SimpleMatrix D, int weight[]){
		final int N = D.numRows();
		final int M = D.numCols();
		final SimpleMatrix res = new SimpleMatrix(N,N);
		for(int i = 0 ; i < N ; i++){
			for(int j = i+1; j < N ; j++){
				double dist = 0;
				for(int k = 0 ; k < M ; k++){
					dist+=Math.pow((D.get(i,k)-D.get(j,k))*weight[k],2);
				}
				dist=Math.sqrt(dist);
				res.set(i,j,dist);
				res.set(j,i,dist);
			}
		}
		return res;
	}

	/**
	 * Please don't remove :-)
	 * @param oldMatrix
	 * @return
	 * @throws Exception
	 */
	public static SimpleMatrix centerReduceMatrix(final SimpleMatrix oldMatrix) {
		final int N = oldMatrix.numRows();
		final int M = oldMatrix.numCols();
		final SimpleMatrix res = new SimpleMatrix(N,M);
		for(int i=0; i<N; i++){
			for(int j=0; j < M; j++){									
				double mean = computeMean(oldMatrix, j);
				double sd   = computeStandardDev(oldMatrix, mean, j);
				res.set(i,j,(oldMatrix.get(i,j)-mean)/sd);
			}

		}
		return res;
	}

	/**
	 * 
	 * @param oldMatrix
	 * @return
	 */
	public static SimpleMatrix rescaleDistanceMatrix(final SimpleMatrix oldMatrix){
		final int N = oldMatrix.numRows();
		final int M = oldMatrix.numCols();
		final SimpleMatrix res = new SimpleMatrix(N,M);
		double min=Double.MAX_VALUE,max=Double.MIN_NORMAL;
		for(int i=0; i<N; i++){
			for(int j=0; j <M; j++){	
				if(oldMatrix.get(i,j)<min){
					min=oldMatrix.get(i,j);
				}
				if(oldMatrix.get(i,j)>max){
					max=oldMatrix.get(i,j);
				}
			}
		}
		if(Math.abs(max-min)<Math.pow(10,-6)){
			throw new IllegalStateException("Matrix contains only duplicate !");
		}
		for(int i=0; i<N; i++){
			for(int j=0; j < M; j++){
				double oldValue=oldMatrix.get(i,j);
				res.set(i,j,(oldValue-min)/(max-min));
			}
		}
		return res;
	}

	/*
	public static SymetricMatrix normalizeSymetricMatrix(final SymetricMatrix oldMatrix){
		double min=Double.MAX_VALUE,max=Double.MIN_NORMAL;
		final SymetricMatrix res = new SymetricMatrix(oldMatrix.getMatrixSize());
		double[] data = oldMatrix.getData();
		int N = data.length;

		//Get min & max
		for(int i=0; i< N ; i++){
			if(data[i]<min){
				min=data[i];
			}
			if(data[i]>max){
				max=data[i];
			}
		}

		if(Math.abs(max-min)<Math.pow(10,-6)){
			throw new IllegalStateException("Matrix contains only duplicate !");
		}
		for(int i=0; i<N; i++){
			for(int j = i+1 ; j < N ; j++){
				double oldValue=oldMatrix.get(i, j);
				res.set(i,j,(oldValue-min)/(max-min));
			}
		}
		return res;
	}
	*/

	/**
	 * 
	 * @param matrix
	 * @param columnIndex
	 * @return
	 */
	public static double computeMean(final SimpleMatrix matrix, int columnIndex){
		double mean = 0;
		for(int i=0; i<matrix.numRows(); i++){
			mean += matrix.get(i, columnIndex);
		}
		mean = mean/matrix.numRows();
		return mean;
	}

	/**
	 * For example {1,2,3} have a mean of 3 and standard deviation of sqrt[(1-2)ï¿½+(2-2)ï¿½+(3-2)ï¿½] 
	 * @param matrix
	 * @param mean
	 * @param columnIndex
	 * @return
	 */
	public static double computeStandardDev(final SimpleMatrix matrix, double mean, int columnIndex){
		double var = 0;
		for(int i=0; i<matrix.numRows(); i++){
			var += Math.pow((matrix.get(i,columnIndex) - mean), 2);
		}
		return Math.sqrt(var);
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final SimpleMatrix D = new SimpleMatrix(new double[][]{{1,2,3},{4,5,6},{7,8,9}});
		System.out.println(MatrixUtil.dist(D));
	}

}
