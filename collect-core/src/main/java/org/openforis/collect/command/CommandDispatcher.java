package org.openforis.collect.command;

import java.util.List;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;

public interface CommandDispatcher {

	<C extends Command> void submit(C command, EventListener eventListener);

	<C extends Command> List<RecordEvent> submitSync(C command);
}
