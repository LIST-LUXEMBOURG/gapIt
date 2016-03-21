/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.context;

import java.awt.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;


/**
 * ResourceLoader: component which allow to load images and icons, by using a cache.
 * 
 * @author the ACORA team.
 */
public final class ResourceLoader 
{
	//
	// Static fields
	//
	
	/** */
	private static final Map<String, Object> CACHE = new WeakHashMap<String, Object>();

	
	//
	// Constructors
	//
	
	/**
	 * Private constructor to avoid instantiation.
	 */
	private ResourceLoader() {
		throw new UnsupportedOperationException();
	}

	
	//
	// Static methods
	//
	
	/**
	 * 
	 * @param imageName
	 * @return
	 */
	public static Icon getAndCacheIcon(String imageName) {
		final Image image = getAndCacheImage(imageName);
		return new ImageIcon(image);
	}

	/**
	 * 
	 * @param imageName
	 * @return
	 */
	public static Image getAndCacheImage(String imageName) {
		checkFileName(imageName);
		Image ret = (Image) CACHE.get("Image://" + imageName);
		if (ret != null) {
			return ret;
		}

		final URL u = getClassLoader().getResource(imageName);
		ret = Toolkit.getDefaultToolkit().getImage(u);
		CACHE.put("Image://" + imageName, ret);
		return ret;
	}

	/**
	 * 
	 * @return
	 */
	private static ClassLoader getClassLoader() {
		return ClassLoader.getSystemClassLoader();
		// return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static URL getAndCacheUrl(String fileName) {
		checkFileName(fileName);
		URL ret = (URL) CACHE.get("Url://" + fileName);
		if (ret != null) {
			return ret;
		}
		ret = getClassLoader().getResource(fileName);
		CACHE.put("Url://" + fileName, ret);
		return ret;
	}

	/**
	 * 
	 * @param imageName
	 * @return
	 */
	public static Image getImage(String imageName) {
		checkFileName(imageName);
		final URL u = getClassLoader().getResource(imageName);
		return Toolkit.getDefaultToolkit().getImage(u);
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static URL getUrl(String fileName) {
		checkFileName(fileName);
		return getClassLoader().getResource(fileName);
	}

	/**
	 * Performs checks for developers
	 * 
	 * @param fileName
	 */
	private static void checkFileName(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException(
					"You must provide a non null filename");
		}
		if (fileName.startsWith(".") || fileName.contains("..")) {
			throw new IllegalArgumentException(
					"Relative path like './' not allowed (jar packaging)");
		}
	}

}
