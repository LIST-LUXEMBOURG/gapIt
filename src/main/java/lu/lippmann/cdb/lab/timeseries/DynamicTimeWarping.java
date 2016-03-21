/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.timeseries;


/**
 * DynamicTimeWarping.
 * 
 * @author the WP1 team
 */
public final class DynamicTimeWarping 
{
	//
	// Instance fields
	//
		
	private final double[] seq1;
	private final double[] seq2;
	private int[][] warpingPath;
	
	private final int n;
	private final int m;
	private int K;
	
	private double warpingDistance;
	
	
	//
	// Contructors
	//
	
	/**
	 * Constructor.
	 */
	public DynamicTimeWarping(final double[] sample,final double[] templete) 
	{
		seq1 = sample;
		seq2 = templete;
		
		n = seq1.length;
		if (n==0) throw new IllegalArgumentException("first serie is empty!");
		m = seq2.length;
		if (m==0) throw new IllegalArgumentException("second serie is empty!");
		K = 1;
		
		warpingPath = new int[n + m][2];	// max(n, m) <= K < n + m
		warpingDistance = 0.0;
		
		this.compute();
	}
	
	
	//
	// Instance methods
	//
	
	private void compute() 
	{
		double accumulatedDistance = 0.0;
		
		double[][] d = new double[n][m];	// local distances
		double[][] D = new double[n][m];	// global distances
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				d[i][j] = distanceBetween(seq1[i], seq2[j]);
			}
		}
		
		D[0][0] = d[0][0];
		
		for (int i = 1; i < n; i++) {
			D[i][0] = d[i][0] + D[i - 1][0];
		}

		for (int j = 1; j < m; j++) {
			D[0][j] = d[0][j] + D[0][j - 1];
		}
		
		for (int i = 1; i < n; i++) {
			for (int j = 1; j < m; j++) {
				accumulatedDistance = Math.min(Math.min(D[i-1][j], D[i-1][j-1]), D[i][j-1]);
				accumulatedDistance += d[i][j];
				D[i][j] = accumulatedDistance;
			}
		}
		accumulatedDistance = D[n - 1][m - 1];

		int i = n - 1;
		int j = m - 1;
		int minIndex = 1;
	
		warpingPath[K - 1][0] = i;
		warpingPath[K - 1][1] = j;
		
		while ((i + j) != 0) {
			if (i == 0) {
				j -= 1;
			} else if (j == 0) {
				i -= 1;
			} else {	// i != 0 && j != 0
				double[] array = { D[i - 1][j], D[i][j - 1], D[i - 1][j - 1] };
				minIndex = getIndexOfMinimum(array);

				if (minIndex == 0) {
					i -= 1;
				} else if (minIndex == 1) {
					j -= 1;
				} else if (minIndex == 2) {
					i -= 1;
					j -= 1;
				}
			} // end else
			K++;
			warpingPath[K - 1][0] = i;
			warpingPath[K - 1][1] = j;
		} // end while
		warpingDistance = accumulatedDistance / K;
		
		this.reversePath(warpingPath);
	}
	
	/**
	 * Changes the order of the warping path (increasing order)
	 * 
	 * @param path	the warping path in reverse order
	 */
	protected void reversePath(int[][] path) 
	{
		int[][] newPath = new int[K][2];
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < 2; j++) {
				newPath[i][j] = path[K - i - 1][j];
			}
		}
		warpingPath = newPath;
	}
	
	/**
	 * Returns the warping distance
	 * 
	 * @return
	 */
	public double getDistance() 
	{
		return warpingDistance;
	}
	
	/**
	 *	{@inheritDoc}
	 */
	@Override
	public String toString() 
	{
		String retVal = "Warping Distance: " + warpingDistance + "\n";
		retVal += "Warping Path: {";
		for (int i = 0; i < K; i++) {
			retVal += "(" + warpingPath[i][0] + ", " +warpingPath[i][1] + ")";
			retVal += (i == K - 1) ? "}" : ", ";
			
		}
		return retVal;
	}
	
	
	//
	// Static methods
	//
	
	/**
	 * Computes a distance between two points
	 * 
	 * @param p1	the point 1
	 * @param p2	the point 2
	 * @return		the distance between two points
	 */
	private static double distanceBetween(final double p1,final double p2) 
	{
		return (p1 - p2) * (p1 - p2);
	}

	/**
	 * Finds the index of the minimum element from the given array
	 * 
	 * @param array		the array containing numeric values
	 * @return				the min value among elements
	 */
	private static int getIndexOfMinimum(final double[] array) 
	{
		int index = 0;
		double val = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] < val) {
				val = array[i];
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * Main method.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) 
	{
		//double[] n2 = {1.5d, 3.9d, 4.1d, 3.3d};
		//double[] n1 = {1.5d, 3.9d, 4.1d, 3.3d};
		//double[] n1 = {2.1d, 2.45d, 3.673d, 4.32d, 2.05d, 1.93d, 5.67d, 6.01d};
		
		double[] n2 = {325.6,332.4,329.6,334.7,334.0,338.8,344.9,343.2,345.2,345.9,329.5,348.4,336.4,333.8,326.9,336.3,343.0,343.8,344.2,335.8,341.3,344.4,343.8,343.6,347.9,353.7,355.2,357.4,354.8,356.8,359.2,360.8,357.2,358.7,342.2,338.8,344.0,345.3,351.2,355.5,350.0,357.2,360.1,361.4,354.9,354.7,349.1,345.3,353.2,342.1,342.0,336.8,337.1,336.0,342.6,339.3,341.8,348.1,353.2,347.7,350.6,346.4,351.1,344.3,337.0,341.2,338.1,339.9,334.1,330.5,335.0,334.8,333.3,326.1,333.1,343.5,355.0,350.3,353.6,352.2,346.2,346.8,349.7,348.0,348.3,348.4,349.7,347.9,348.9,349.0,346.1,345.7,339.2,332.0,336.5,342.1,339.6,330.0,335.5,333.4,336.0,334.8,341.1,348.9,346.5,343.2,345.7,338.2,331.8,333.2,330.6,327.2,330.0,329.8,326.9,329.0,317.4,316.7,325.2,318.9,331.4,327.6,333.6,336.0,334.7,336.0,343.0,349.0,354.7,353.3,356.3,353.5,358.3,361.0,361.2,365.4,378.0,396.1,387.0,388.3,390.4,400.0,400.6,391.6,387.6,397.8,397.6,391.0,389.4,380.4,361.7,361.3,371.2};
		double[] n1 = {330.3,332.5,334.3,335.2,336.4,343.2,345.0,344.4,346.6,348.5,344.8,348.6,338.3,334.9,337.4,341.4,345.6,344.7,344.4,340.0,345.6,345.2,344.2,346.7,353.2,355.5,359.0,360.0,357.8,359.5,360.0,364.9,360.3,359.5,345.4,344.6,345.2,348.4,355.0,355.7,354.4,359.8,360.3,361.7,357.4,354.8,349.8,352.3,356.5,347.8,343.0,339.6,338.2,339.7,342.6,340.2,346.0,352.1,354.3,351.0,350.9,349.8,351.6,344.6,342.2,343.9,340.4,340.2,335.7,333.7,336.1,336.0,333.6,332.2,338.0,345.8,355.1,353.8,355.0,352.4,349.8,354.0,350.5,349.9,351.8,351.0,350.0,349.2,349.7,350.0,347.1,346.2,341.2,336.1,341.0,342.4,341.0,336.0,335.9,338.6,336.9,337.6,347.8,352.1,348.0,345.3,347.0,338.2,334.8,333.7,331.7,328.3,333.2,330.3,328.7,329.2,317.7,325.8,328.9,331.7,333.2,333.9,336.7,336.4,336.1,343.5,349.8,354.1,358.0,360.0,359.8,357.7,360.0,361.6,365.0,374.6,378.6,396.3,390.1,395.0,400.0,404.5,402.6,397.0,395.2,399.5,397.9,393.6,391.3,383.5,367.8,374.6,374.6};

		
		DynamicTimeWarping dtw = new DynamicTimeWarping(n1, n2);
		System.out.println(dtw);
	}
}
