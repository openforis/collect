package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.Command;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;

public interface CommandHandler<C extends Command> {

	void execute(C command, EventListener eventListener);
	
	List<RecordEvent> executeSync(C command);
}