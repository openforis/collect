package org.openforis.collect.event;

import org.openforis.collect.relational.event.InitializeRDBEvent;
import org.openforis.rmb.MessageQueue;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class EventBrokerEventQueue implements EventQueue {
	
	private boolean enabled = false;
	private MessageQueue<Object> queue;
	
	public EventBrokerEventQueue(MessageQueue<Object> queue) {
		super();
		this.queue = queue;
	}

	@Override
	public void publish(RecordTransaction recordTransaction) {
		queue.publish(recordTransaction);
	}
	
	@Override
	public void publish(SurveyEvent event) {
		queue.publish(event);
	}

	public void publish(InitializeRDBEvent initializeRDBEvent) {
		queue.publish(initializeRDBEvent);
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
