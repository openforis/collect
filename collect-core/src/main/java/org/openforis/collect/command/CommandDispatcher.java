package org.openforis.collect.command;

import org.openforis.collect.event.EventListener;

public interface CommandDispatcher {

	void submit(Command command, EventListener eventListener);
	
}
