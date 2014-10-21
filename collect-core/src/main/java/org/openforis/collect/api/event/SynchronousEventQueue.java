/**
 * 
 */
package org.openforis.collect.api.event;

import java.util.Arrays;
import java.util.List;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class SynchronousEventQueue implements EventQueue {
	
	private final List<EventHandler> handlers;
	
	public SynchronousEventQueue(EventHandler... handlers) {
		this.handlers = Arrays.asList(handlers);
	}
	
	@Override
	public void publish(List<Event> events) {
		for (EventHandler handler : handlers) {
			handler.handle(events);
		}
	}

}
