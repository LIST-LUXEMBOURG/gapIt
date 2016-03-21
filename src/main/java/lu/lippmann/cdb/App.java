/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb;

import lu.lippmann.cdb.common.FileUtil;
import lu.lippmann.cdb.context.BusLogger;


/**
 * Main class for the application.
 * 
 * @author the ACORA team
 */
public final class App 
{
	//
	// Static fields
	//

	/** */
	public static final boolean BOOT_WITH_NEW_GRAPH=false;

	/** Avoid gc */
	private static BusLogger BUS_LOGGER;

	
	//
	// Constructors
	//

	/**
	 * Private constructor.
	 */
	private App(){}


	//
	// Static methods
	//



	/**
	 * 
	 * @return
	 */
	public static String getBuildId() {
		try {
			final StringBuilder res = FileUtil.getFileContent("build.txt");
			return res.toString();
		} catch (Exception e) {
			return "<i>Version : Unknown</i>";
		}
	}

}