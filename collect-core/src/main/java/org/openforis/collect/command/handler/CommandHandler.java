package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.Command;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.utils.ExceptionHandler;

public interface CommandHandler<C extends Command> {

	void execute(C command, EventListener eventListener, ExceptionHandler exceptionHandler);

	List<RecordEvent> executeSync(C command) throws Exception;
}