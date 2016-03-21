/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.context;

import lu.lippmann.cdb.models.*;


/**
 * ApplicationContext: component containing global values for the whole application.
 * 
 * @author the ACORA team.
 */
public class ApplicationContext 
{
	//
	// Instance fields
	//
	
	/** */
	private CGraph cadralGraph;
	/** */
	private CUser user;
	
	
	//
	// Instance methods
	//

	public CGraph getCadralGraph() 
	{
		return cadralGraph;
	}

	public void setCadralGraph(final CGraph graph) 
	{
		this.cadralGraph = graph;
	}

	public void setUser(final CUser user) 
	{
		this.user=user;		
	}

	public final CUser getUser() 
	{
		return user;
	}

}
