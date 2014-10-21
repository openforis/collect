/**
 * 
 */
package org.openforis.collect.api.event;

import java.util.Date;

import org.openforis.collect.api.command.Command;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class Event {

	public final Date timestamp;
	public final Command trigger;
	
	public Event(Command trigger) {
		this.trigger = trigger;
		this.timestamp = new Date();
	}
	
}
