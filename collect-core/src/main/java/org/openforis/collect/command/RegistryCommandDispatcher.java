package org.openforis.collect.command;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.command.handler.CommandHandler;

public class RegistryCommandDispatcher implements CommandDispatcher {

	private final Map<Class<? extends Command<?>>, CommandHandler<?, ?>> handlers = 
			new HashMap<Class<? extends Command<?>>, CommandHandler<?, ?>>();

	public <R, C extends Command<R>> RegistryCommandDispatcher register(
			Class<C> commandType,
			CommandHandler<R, C> handler) {
		handlers.put(commandType, handler);
		return this;
	}

	@Override
	public <R> R submit(Command<R> command) {
		@SuppressWarnings("unchecked")
		CommandHandler<R, Command<R>> handler = (CommandHandler<R, Command<R>>) handlers.get(command.getClass());
	 	//Error handling if missing
		return handler.execute(command);
	}

}
