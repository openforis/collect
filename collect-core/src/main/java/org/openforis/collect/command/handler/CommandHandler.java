package org.openforis.collect.command.handler;

import org.openforis.collect.command.Command;

public interface CommandHandler<R, C extends Command<R>> {

	R execute(C command);
	
}