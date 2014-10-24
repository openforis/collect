/**
 * 
 */
package org.openforis.collect.api.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.api.RecordProvider;
import org.openforis.collect.api.event.Event;
import org.openforis.collect.api.event.EventHandler;
import org.openforis.collect.api.event.SynchronousEventQueue;
import org.openforis.collect.model.RecordUpdater;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class SynchronousCommandQueue implements CommandQueue {

	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends Command>, CommandHandler> handlers = new HashMap<Class<? extends Command>, CommandHandler>();
	private final SynchronousEventQueue eventQueue;
	
	public SynchronousCommandQueue(RecordProvider recordProvider, EventHandler... eventHandlers) {
		RecordUpdater recordUpdater = new RecordUpdater();
		handlers.put(UpdateAttributeValueCommand.class, new UpdateAttributeValueCommand.Handler(recordProvider, recordUpdater));
		eventQueue = new SynchronousEventQueue(eventHandlers);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void submit(Command command) {
		CommandHandler handler = handlers.get(command.getClass());
		List<Event> events = handler.handle(command);
		eventQueue.publish(events);
	}

	
	
}
