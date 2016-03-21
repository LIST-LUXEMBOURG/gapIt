/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.guice.util;

import com.google.inject.AbstractModule;


/**
 * AbstractAutoBindingModule.
 * 
 * @author Olivier PARISOT
 */
public abstract class AbstractAutoBindingModule extends AbstractModule {

	//
	// Instance fields
	//

	/** */
	private final String[] packagesNames;

	
	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public AbstractAutoBindingModule(final String... packagesNames) 
	{
		this.packagesNames=packagesNames;
	}

	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void configure() 
	{
		for (final String packageName:packagesNames) 
		{
			try 
			{
				for (final Class<?> c:ClassEnumerator.getClasses(packageName)) 
				{
					if (!c.isInterface()) 
					{
						if (c.getAnnotation(AutoBind.class)!=null) 
						{
							for (final Class<?> i:c.getInterfaces()) 
							{
								doBind(i, c);
							}
						}
					}
				}
			} 
			catch (final Exception e) 
			{
				throw new RuntimeException(e);// TODO
			}
		}
	}

	/**
	 * Called when a class annotated with autoBind is found, that has a value of
	 * an interface implemented by this class.
	 */
	protected abstract void doBind(Class<?> interfaceToBindTo, Class<?> matchingClass);

}
