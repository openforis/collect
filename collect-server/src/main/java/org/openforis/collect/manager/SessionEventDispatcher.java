package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionEventDispatcher {
	
	@Autowired
	private SessionManager sessionManager;
	
	private List<EventListener> listeners;

	public SessionEventDispatcher(List<EventListener> listeners) {
		this.listeners = listeners;
	}
	
	public void recordSaved(CollectRecord record) {
		List<RecordEvent> events = sessionManager.flushPendingEvents();
		if (! events.isEmpty()) {
			for (RecordEvent event : events) {
				//TODO remove this, assign an ID to the record immediately when it's created
				event.initializeRecordId(record.getId());
			}
			notifyListeners(events);
		}
	}

	private void notifyListeners(List<? extends RecordEvent> events) {
		for (EventListener eventListener : listeners) {
			eventListener.onEvents(events);
		}
	}
	
}
