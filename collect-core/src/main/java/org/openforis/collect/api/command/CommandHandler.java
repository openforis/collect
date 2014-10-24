/**
 * 
 */
package org.openforis.collect.api.command;

import java.util.List;

import org.openforis.collect.api.event.Event;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
interface CommandHandler<T extends Command> {

	List<Event> handle(T command);
	
}
