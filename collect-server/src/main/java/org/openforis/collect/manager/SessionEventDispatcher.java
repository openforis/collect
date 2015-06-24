package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventSource;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionEventDispatcher implements EventSource {
	
	@Autowired
	private SessionManager sessionManager;
	private List<EventListener> listeners = new ArrayList<EventListener>();

	@Override
	public void register(EventListener listener) {
		listeners.add(listener);
	}

	public void recordSaved() {
		List<RecordEvent> events = sessionManager.flushPendingEvents();
		notifyListeners(events);
	}

	public void recordDeleted(CollectRecord record, String userName) {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		notifyListeners(Arrays.asList(new RecordDeletedEvent(record.getId(), rootEntityDefn.getId(), rootEntity.getInternalId(), new Date(), userName)));
	}
	
	private void notifyListeners(List<? extends RecordEvent> events) {
		for (EventListener eventListener : listeners) {
			eventListener.onEvents(events);
		}
	}
	
}
