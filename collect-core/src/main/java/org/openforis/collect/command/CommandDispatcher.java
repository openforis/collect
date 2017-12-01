package org.openforis.collect.command;

public interface CommandDispatcher {

	<R> R submit(Command<R> command);
	
}
