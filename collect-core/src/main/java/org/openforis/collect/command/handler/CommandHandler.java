package org.openforis.collect.command.handler;

import org.openforis.collect.command.Command;
import org.openforis.collect.event.EventListener;

public interface CommandHandler<C extends Command> {

	void execute(C command, EventListener eventListener);
	
}