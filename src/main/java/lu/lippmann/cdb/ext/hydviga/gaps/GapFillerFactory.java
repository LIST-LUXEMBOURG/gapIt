/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.ext.hydviga.gaps;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.M5P;


/**
 * TimeSeriesGapFillerRunner.
 * 
 * @author Olivier PARISOT
 */
public final class GapFillerFactory 
{
	//
	// Inner enums
	//
	
	public static enum Algo
	{
		Interpolation,
		EM_WITH_DISCR_TIME,
		EM,
		REG_WITH_DISCR_TIME,
		REG,
		M5P_WITH_DISCR_TIME, 
		M5P,
		ZeroR,
		ANN_WITH_DISCR_TIME, 
		ANN, 
		NEARESTNEIGHBOUR_WITH_DISCR_TIME,
		NEARESTNEIGHBOUR;
	}
	
	
	//
	// Static enums
	//
	
	public static GapFiller getGapFiller(final Algo algo) throws Exception
	{
		final GapFiller tsgp;
		if (algo==Algo.EM_WITH_DISCR_TIME) tsgp=new GapFillerEM(true);
		else if (algo==Algo.EM) tsgp=new GapFillerEM(false);
		else if (algo==Algo.Interpolation) tsgp=new GapFillerInterpolation(false);
		else if (algo==Algo.ZeroR) tsgp=new GapFillerClassifier(false,new ZeroR());		
		else if (algo==Algo.REG_WITH_DISCR_TIME) tsgp=new GapFillerRegressions(true);
		else if (algo==Algo.REG) tsgp=new GapFillerRegressions(false);
		else if (algo==Algo.M5P_WITH_DISCR_TIME) tsgp=new GapFillerClassifier(true,new M5P());
		else if (algo==Algo.M5P) tsgp=new GapFillerClassifier(false,new M5P());
		else if (algo==Algo.ANN_WITH_DISCR_TIME) tsgp=new GapFillerClassifier(true,new MultilayerPerceptron());
		else if (algo==Algo.ANN) tsgp=new GapFillerClassifier(false,new MultilayerPerceptron());		
		else if (algo==Algo.NEARESTNEIGHBOUR_WITH_DISCR_TIME) tsgp=new GapFillerClassifier(true,new IBk());
		else if (algo==Algo.NEARESTNEIGHBOUR) tsgp=new GapFillerClassifier(false,new IBk());		
		else throw new Exception("Algo not managed -> "+algo);		
		return tsgp;
	}
	
	public static GapFiller getGapFiller(final String algoname,final boolean useDiscretizedTime) throws Exception
	{
		final GapFiller tsgp;
		if (algoname.equals("EM")) tsgp=new GapFillerEM(useDiscretizedTime);
		else if (algoname.equals("Interpolation")) tsgp=new GapFillerInterpolation(useDiscretizedTime);
		else if (algoname.equals("ZeroR")) tsgp=new GapFillerClassifier(useDiscretizedTime,new ZeroR());
		else if (algoname.equals("REG")) tsgp=new GapFillerRegressions(useDiscretizedTime);
		else if (algoname.equals("M5P")) tsgp=new GapFillerClassifier(useDiscretizedTime,new M5P());
		else if (algoname.equals("ANN")) tsgp=new GapFillerClassifier(useDiscretizedTime,new MultilayerPerceptron());
		else if (algoname.equals("NEARESTNEIGHBOUR")) tsgp=new GapFillerClassifier(useDiscretizedTime,new IBk());		
		else throw new Exception("Algo name not managed -> "+algoname);
		return tsgp;
	}
}
