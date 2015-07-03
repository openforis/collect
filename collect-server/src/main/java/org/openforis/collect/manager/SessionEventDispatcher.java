package org.openforis.collect.manager;

import java.util.List;

import org.fao.foris.simpleeventbroker.EventBroker;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionEventDispatcher {
	
	@Autowired
	private SessionManager sessionManager;

	private EventBroker eventQueue;
	
	public SessionEventDispatcher(EventBroker eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void recordSaved(CollectRecord record) {
		List<RecordEvent> events = sessionManager.flushPendingEvents();
		if (! events.isEmpty()) {
			for (RecordEvent event : events) {
				//TODO remove this, assign an ID to the record immediately when it's created
				event.initializeRecordId(record.getId());
			}
			eventQueue.publish(events);
		}
	}

}
