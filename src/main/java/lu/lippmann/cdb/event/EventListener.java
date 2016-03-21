/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.event;

import java.lang.annotation.*;


/**
 * Event listener.
 *
 * @author Olivier PARISOT
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener 
{
	Class<? extends AbstractEvent> eventClass() default AbstractEvent.class;	
}
