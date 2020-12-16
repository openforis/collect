package org.openforis.collect.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.utils.ExceptionHandler;

public class RegistryCommandDispatcher implements CommandDispatcher {

	private final Map<Class<? extends Command>, CommandHandler<?>> handlers = new HashMap<Class<? extends Command>, CommandHandler<?>>();

	public <R, C extends Command> RegistryCommandDispatcher register(Class<C> commandType, CommandHandler<C> handler) {
		handlers.put(commandType, handler);
		return this;
	}

	@Override
	public <C extends Command> void submit(C command, EventListener eventListener, ExceptionHandler exceptionHandler) {
		@SuppressWarnings("unchecked")
		CommandHandler<C> handler = (CommandHandler<C>) handlers.get(command.getClass());
		// Error handling if missing
		handler.execute(command, eventListener, exceptionHandler);
	}

	@Override
	public <C extends Command> List<RecordEvent> submitSync(C command) throws Exception {
		@SuppressWarnings("unchecked")
		CommandHandler<C> handler = (CommandHandler<C>) handlers.get(command.getClass());
		return handler.executeSync(command);
	}
}
