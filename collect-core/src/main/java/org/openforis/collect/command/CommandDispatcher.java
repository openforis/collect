package org.openforis.collect.command;

import java.util.List;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.utils.ExceptionHandler;

public interface CommandDispatcher {

	<C extends Command> void submit(C command, EventListener eventListener, ExceptionHandler exceptionHandler);

	<C extends Command> List<RecordEvent> submitSync(C command) throws Exception;
}
