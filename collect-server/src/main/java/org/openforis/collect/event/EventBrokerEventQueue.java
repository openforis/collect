package org.openforis.collect.event;

import org.fao.foris.simpleeventbroker.EventBroker;

public class EventBrokerEventQueue implements EventQueue {
	
	private EventBroker queue;
	
	public EventBrokerEventQueue(EventBroker queue) {
		super();
		this.queue = queue;
	}

	@Override
	public void publish(RecordTransaction recordTransaction) {
		queue.publish(recordTransaction);
	}

}
