package org.openforis.collect.command;

import org.openforis.rmb.MessageQueue;

/**
 * 
 * @author S. Ricci
 *
 */
public class CommandBrokerCommandQueue implements CommandQueue {
	
	private boolean enabled = false;
	private MessageQueue<Object> queue;
	
	public CommandBrokerCommandQueue(MessageQueue<Object> queue) {
		super();
		this.queue = queue;
	}

	@Override
	public void publish(Command command) {
		queue.publish(command);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
