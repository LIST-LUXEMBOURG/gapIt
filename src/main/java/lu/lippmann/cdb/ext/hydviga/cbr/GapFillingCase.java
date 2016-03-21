/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.cbr;

import lu.lippmann.cdb.ext.hydviga.gaps.GapFillerFactory.Algo;
import weka.core.Attribute;


/**
 * GapFillingCase.
 * 
 * @author the HYDVIGA team
 */
public final class GapFillingCase 
{
	//
	// Instance fields
	//
	
	final String season;
	final int year;
	
	final int gapSize;
	final int gapPosition;
	final Attribute attr;
	final double x;
	final double y;
	
	final boolean isDuringRising;
	
	final boolean hasUpstream;
	final boolean hasDownstream;
		
	final Algo algo;
	
	final double mae;
	final double rmse;
	final double rsr;
	final double pbias;
	final double nashSutcliffe;
	final double indexOfAgreement;
	
	final boolean useMostSimilar;
	final boolean useNearest;
	final boolean useDownstream;
	final boolean useUpstream;
	
	boolean wasTheBestSolution;
	
	final String flow;
	
	
	//
	// Constructors
	//
	
	/**
	 * Constructor.
	 */
	public GapFillingCase(final String season,final int year,final Algo algo,final int gapSize,final int gapPosition,final Attribute attr,final double x,final double y,final boolean hasDownstream,final boolean hasUpstream,final double mae,final double rmse,final double rsr,final double pbias,final double ns,final double ioa,final boolean useMostSimilar,final boolean useNearest,final boolean useDownstream,final boolean useUpstream,final boolean isDuringRising,final String flow)
	{
		this.season=season;
		this.year=year;
		this.gapSize=gapSize;
		this.attr=attr;
		this.x=x;
		this.y=y;		
		this.gapPosition=gapPosition;
		this.hasUpstream=hasUpstream;
		this.hasDownstream=hasDownstream;
		this.isDuringRising=isDuringRising;
		
		this.algo=algo;
		
		if (mae>rmse) throw new IllegalArgumentException("mae(="+mae+") should be <= to rmse(="+rmse+")");
		this.mae=mae;
		this.rmse=rmse;
		
		this.rsr=rsr;
		this.pbias=pbias;
		this.nashSutcliffe=ns;
		this.indexOfAgreement=ioa;
		
		this.useMostSimilar=useMostSimilar;
		this.useNearest=useNearest;
		this.useDownstream=useDownstream;
		this.useUpstream=useUpstream;
		
		this.flow=flow;
	}

	
	//
	// Instance methods
	//
	
	public void setWasTheBestSolution(final boolean wasTheBestSolution)
	{
		this.wasTheBestSolution=wasTheBestSolution;
	}
	
	public double getRMSE()
	{
		return this.rmse;
	}

	public double getRSR()
	{
		return this.rsr;
	}

	public double getNashSutcliffe() 
	{		
		return this.nashSutcliffe;
	}
	
}
