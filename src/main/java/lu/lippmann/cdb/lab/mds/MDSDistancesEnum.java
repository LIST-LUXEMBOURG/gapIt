/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.lab.mds;



/**
 * MDSDistancesEnum: lists all distances which are available to compute MDS view.
 * 
 * @author the WP1 team
 */
public enum MDSDistancesEnum 
{
	
	EUCLIDEAN,
	DT,
	MANHATTAN,
	MINKOWSKI,
	CHEBYSHEV;
	
	
	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}
	
	public String[] getParameters() {
		return parameters;
	}

	private String[] parameters;
	
}
