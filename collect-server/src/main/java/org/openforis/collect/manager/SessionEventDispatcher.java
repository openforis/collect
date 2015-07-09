package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.event.EventBrokerEventQueue;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.model.CollectRecord;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionEventDispatcher {
	
	@Autowired
	private SessionManager sessionManager;

	private EventBrokerEventQueue eventQueue;
	
	public SessionEventDispatcher(EventBrokerEventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void recordSaved(CollectRecord record) {
		List<RecordEvent> events = sessionManager.flushPendingEvents();
		if (! events.isEmpty()) {
			for (RecordEvent event : events) {
				//TODO remove this, assign an ID to the record immediately when it's created
				event.initializeRecordId(record.getId());
			}
			String surveyName = record.getSurvey().getName();
			eventQueue.publish(new RecordTransaction(surveyName, record.getId(), record.getStep().toRecordStep(), events));
		}
	}

}
