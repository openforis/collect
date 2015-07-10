package org.openforis.collect.event;

import org.fao.foris.simpleeventbroker.EventBroker;
import org.openforis.collect.relational.event.InitializeRDBEvent;

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
	
	@Override
	public void publish(SurveyEvent event) {
		queue.publish(event);
	}

	public void publish(InitializeRDBEvent initializeRDBEvent) {
		queue.publish(initializeRDBEvent);
	}

}
