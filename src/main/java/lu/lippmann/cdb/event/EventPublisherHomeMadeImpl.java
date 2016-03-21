/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import java.lang.reflect.Method;
import java.util.*;

import lu.lippmann.cdb.common.guice.util.ClassEnumerator;


/**
 * EventPublisherHomeMadeImpl.
 *
 * @author Olivier PARISOT
 */
public class EventPublisherHomeMadeImpl implements EventPublisher
{
	//
	// Instance fields
	//
	
	/** listener.class -> List<Method> */
	private final Map<Class<?>,List<Method>> mapMethods=new HashMap<Class<?>,List<Method>>();
	/** event.class -> List<listener> */
	private final Map<Class<? extends AbstractEvent>,List<Object>> mapObjects=new HashMap<Class<? extends AbstractEvent>,List<Object>>();
	
	
	//
	// Constructor
	//
	
	/**
	 * Constructor.
	 */
	public EventPublisherHomeMadeImpl()
	{
		try 
		{
			for (final Class<?> c:ClassEnumerator.getClasses("lu.lippmann")) 
			{
				for (final Method m:c.getMethods())
				{
					final EventListener annotation=m.getAnnotation(EventListener.class);
					if (annotation!=null)
					{
						//final Class<? extends AbstractEvent> eventClass=annotation.eventClass();
						if (!mapMethods.containsKey(c))
						{
							mapMethods.put(c,new ArrayList<Method>());
						}						
						mapMethods.get(c).add(m);
						/*if (!mapObjects.containsKey(annotation.eventClass()))
						{
							mapObjects.put(annotation.eventClass(),new ArrayList<Object>());
						}*/
					}
				}
			}
			System.out.println("mapMethods -> "+mapMethods);
			System.out.println("mapObjects -> "+mapObjects);			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 

	}
	
	
	//
	// Instance methods
	//
	
	//private List<>
	
	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void publish(final AbstractEvent event)
	{
		for (final Object obj:mapObjects.get(event.getClass()))
		{
			for (final Method m:mapMethods.get(obj.getClass()))
			{
				try 
				{
					m.invoke(obj,event);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				} 
			}
		}
		/*if (!mapMethods.containsKey(event.getClass()))
		{
			System.out.println("WARNING: no listener for "+event.getClass());
		}
		else
		{
			for (final Method m:mapMethods.get(event.getClass()))
			{			
				for (final Object o:mapObjects.get(event.getClass()))
				{
					try 
					{
						m.invoke(o,event);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					} 
				}
			}
		}*/
	}

	/**		 
	 * {@inheritDoc}
	 */
	@Override
	public void markAsEventListener(final Object obj) 
	{
		/*if (mapObjects.containsKey(obj.getClass()))
		{
			System.out.println("markAsEventListener "+obj.getClass());
			mapObjects.get(obj.getClass()).add(obj);;
		}*/
	}
}
