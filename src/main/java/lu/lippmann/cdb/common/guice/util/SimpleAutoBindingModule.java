/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.common.guice.util;

/**
 * 
 * @author
 *
 */
public class SimpleAutoBindingModule extends AbstractAutoBindingModule 
{
	//
	// Constructors
	//

	/**
	 * Constructor.
	 */
	public SimpleAutoBindingModule(final String... packageName) 
	{
		super(packageName);
	}
	

	//
	// Instance methods
	//

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doBind(final Class interfaceToBindTo,final Class matchingClass) 
	{
		bind(interfaceToBindTo).to(matchingClass);
		System.out.println("Binded: "+matchingClass+"->"+interfaceToBindTo);
	}

}
