package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.model.CollectRecord;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class SessionEventDispatcher {
	
	@Autowired
	private RecordSessionManager recordSessionManager;

	private EventQueue eventQueue;
	
	public SessionEventDispatcher(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void recordSaved(CollectRecord record) {
		List<RecordEvent> events = recordSessionManager.flushPendingEvents();
		if (! events.isEmpty()) {
			for (RecordEvent event : events) {
				//TODO remove this, assign an ID to the record immediately when it's created
				event.initializeRecordId(record.getId());
			}
			String surveyName = record.getSurvey().getName();
			if (eventQueue.isEnabled()) {
				eventQueue.publish(new RecordTransaction(surveyName, record.getId(), record.getStep().toRecordStep(), events));
			}
		}
	}

}
