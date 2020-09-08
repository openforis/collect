package org.openforis.collect.command;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.command.handler.CommandHandler;
import org.openforis.collect.event.EventListener;

public class RegistryCommandDispatcher implements CommandDispatcher {

	private final Map<Class<? extends Command>, CommandHandler<?>> handlers = 
			new HashMap<Class<? extends Command>, CommandHandler<?>>();

	public <R, C extends Command> RegistryCommandDispatcher register(
			Class<C> commandType,
			CommandHandler<C> handler) {
		handlers.put(commandType, handler);
		return this;
	}

	@Override
	public void submit(Command command, EventListener eventListener) {
		@SuppressWarnings("unchecked")
		CommandHandler<Command> handler = (CommandHandler<Command>) handlers.get(command.getClass());
	 	//Error handling if missing
		handler.execute(command, eventListener);
	}

}
