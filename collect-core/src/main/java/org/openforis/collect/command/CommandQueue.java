package org.openforis.collect.command;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandQueue {
	
	private ConcurrentLinkedQueue<Command> queue;
	
	public CommandQueue() {
		queue = new ConcurrentLinkedQueue<Command>();
	}
	
	public void add(Command command) {
		queue.add(command);
	}

	public Command poll() {
		return queue.poll();
	}
}
