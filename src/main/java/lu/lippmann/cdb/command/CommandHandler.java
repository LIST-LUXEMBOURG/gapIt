/**
 * Copyright© 2014-2016 LIST (Luxembourg Institute of Science and Technology), all right reserved.
 * Authorship : Olivier PARISOT, Yoanne DIDRY
 * Licensed under GNU General Public License version 3
 */
package lu.lippmann.cdb.command;

import java.lang.annotation.*;


/**
 * CommandHandler.
 *
 * @author Olivier PARISOT
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler 
{
	Class<? extends AbstractCommand> commandClass() default AbstractCommand.class;	
}
