/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.guice.util;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.*;

/**
 * 
 * @author
 */
public final class ClassEnumerator {
	//
	// Constructors
	//

	/**
	 * Private constructor to avoid instantiation.
	 */
	private ClassEnumerator() {
	}

	//
	// Static methods
	//

	/**
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Set<Class<?>> getClasses(final String packageName)
			throws IOException, ClassNotFoundException {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		return getClasses(loader, packageName);
	}

	/**
	 * 
	 * @param loader
	 * @param packageName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> getClasses(final ClassLoader loader,
			final String packageName) throws IOException,
			ClassNotFoundException {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		final String path = packageName.replace('.', '/');
		final Enumeration<URL> resources = loader.getResources(path);
		if (resources != null) {
			while (resources.hasMoreElements()) {
				String filePath = resources.nextElement().getFile();
				// WINDOWS HACK
				if (filePath.indexOf("%20") > 0) {
					filePath = filePath.replaceAll("%20", " ");
				}
				System.out.println("#### ClassEnumerator.getClasses() in '"
						+ filePath + "'");
				if (filePath != null) {
					if ((filePath.indexOf('!') > 0)
							&& (filePath.indexOf(".jar") > 0)) {
						String jarPath = filePath.substring(0,
								filePath.indexOf('!')).substring(
								filePath.indexOf(':') + 1);
						// WINDOWS HACK
						if (jarPath.indexOf(':') >= 0) {
							jarPath = jarPath.substring(1);
						}
						classes.addAll(getFromJARFile(jarPath, path));
					} else {
						classes.addAll(getFromDirectory(filePath, packageName));
					}
				}
			}
		}
		return classes;
	}

	/**
	 * 
	 * @param dirName
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static Set<Class<?>> getFromDirectory(final String dirName,
			final String packageName) throws ClassNotFoundException {
		// System.out.println("getFromDir -> "+dirName);
		final File directory = new File(dirName);
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		if (directory.exists() && directory.isDirectory()) {
			for (final String file : directory.list()) {
				if (file.endsWith(".class")) {
					if (file.indexOf('$') < 0) {
						// System.out.println(file);
						final String name = packageName + '.'
								+ stripFilenameExtension(file);
						// if (name.indexOf("Impl")>0)
						{
							// System.out.println("adding -> "+name);
							classes.add(Class.forName(name));
						}
					}

				} else {
					// System.out.println(dirName+"\\"+file);
					classes.addAll(getFromDirectory(dirName + '\\' + file,
							packageName + "." + file));
				}
			}
		}
		return classes;
	}

	/**
	 * 
	 * @param jarFileName
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Set<Class<?>> getFromJARFile(final String jarFileName,
			final String packageName) throws ClassNotFoundException, IOException {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		JarInputStream jarFile = null;
		try {
			jarFile = new JarInputStream(new FileInputStream(jarFileName));
			JarEntry jarEntry;
			do {
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry != null) {
					String className = jarEntry.getName();
					if (className.endsWith(".class")) {
						className = stripFilenameExtension(className);
						if (className.startsWith(packageName)) {
							classes.add(Class.forName(className.replace('/',
									'.')));
						}
					}
				}
			} while (jarEntry != null);

		} catch (IOException e) {
			throw new IOException(e);
		} finally {
			// Close jar input stream
			if (jarFile != null) {
				jarFile.close();
			}
		}
		return classes;
	}

	/**
	 * @param className
	 * @return
	 */
	private static String stripFilenameExtension(final String className) {
		final int i = className.lastIndexOf('.');
		return className.substring(0, i);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            command line arguements
	 */
	public static void main(final String[] args) {
		try {
			System.out.println("size -> "
					+ getClasses("lu.lippmann.agimo").size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
