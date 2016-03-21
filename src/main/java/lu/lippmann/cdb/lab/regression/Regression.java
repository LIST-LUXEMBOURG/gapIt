/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.regression;

import java.io.File;

import lu.lippmann.cdb.common.FormatterUtil;
import lu.lippmann.cdb.weka.*;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import weka.core.Instances;


/**
 * Regression.
 * 
 * @author the WP1 team
 */
public final class Regression 
{
	//
	// Instance fields
	//

	/** */
	private final Instances newds;
	/** */
	private final double r2;
	/** */
	private final double[] coe;
	/** */
	private final double[] estims;

	
	//
	// Cosntructors
	//
	
	/**
	 * Constructor.
	 */
	public Regression(final Instances ds,final int idx) throws Exception
	{
		this.newds=WekaDataProcessingUtil.buildDataSetSortedByAttribute(ds,idx);
		
		//System.out.println("Regression -> "+newds.toSummaryString());
		
		final int N = this.newds.numInstances();
		final int M = this.newds.numAttributes();

		final double[][] x = new double[N][M-1];
		final double[]   y = new double[N];
		for(int i = 0 ; i < N ; i++)
		{
			y[i] = this.newds.instance(i).value(0);
		}
		for(int i = 0 ; i < N ; i++)
		{
			for(int j = 1 ; j < M ;j++)
			{
				x[i][j-1] = this.newds.instance(i).value(j);
			}
		}

		final OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		//reg.setNoIntercept(true);
		reg.newSampleData(y,x);
		
		this.r2 = reg.calculateRSquared();
		//this.r2=-1d;
		
		this.coe = reg.estimateRegressionParameters();

		this.estims=calculateEstimations(x,y,coe);
	}
	
	
	//
	// Instance methods
	//
	
	public double getR2() 
	{
		return r2;
	}

	public double[] getCoe() 
	{
		return coe;
	}

	public double getCoef(final String attrName) 
	{		
		return coe[this.newds.attribute(attrName).index()];
	}
	
	public String getCoeDesc()
	{
		final StringBuilder sb=new StringBuilder();
		sb.append(coe[0]).append("+\n");
		for(int i = 1 ; i < this.newds.numAttributes() ; i++)
		{
			sb.append(this.newds.attribute(i).name()).append("*").append(coe[i]).append("+\n");
		}
		sb.setLength(sb.length()-2);
		return sb.toString();
	}

	public double[] getEstims() 
	{
		return estims;
	}
	
	
	//
	// Static methods
	//
	
	private static double[] calculateEstimations(double[][] coords,double[] y,double[] coe) 
	{
		if (coe==null) return null;

		final double[] estims=new double[coords.length];
		for(int i = 0 ; i < coords.length ; i++)
		{
			estims[i]=calculateEstimation(coords[i], coe);
		}
		return estims;

	}

	private static double calculateEstimation(double[] coords, double[] coe) 
	{
		double result = coe[0];
		for(int i = 1; i < coe.length; ++i) result += coe[i] * coords[i-1]; // 1
		return result;
	}
	
	/**
	 * Main method.
	 * @param args comamnd line arguments	 
	 */
	public static void main(String[] args) throws Exception 
	{

		final Instances ds = WekaDataAccessUtil.loadInstancesFromARFFOrCSVFile(new File("./samples/csv/bank.csv"));
		
		final int idx=0;
		System.out.println("Regression for attribute "+ds.attribute(idx).name());
		final Regression r=new Regression(ds,idx);
		System.out.println(r.getR2());
		System.out.println(FormatterUtil.buildStringFromArrayOfDoubles(r.getCoe()));
		System.out.println(r.getCoeDesc());
		System.out.println(FormatterUtil.buildStringFromArrayOfDoubles(r.getEstims()));
	}



}
