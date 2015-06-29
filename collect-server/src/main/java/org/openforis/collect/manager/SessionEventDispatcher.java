package org.openforis.collect.manager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;

public class SessionEventDispatcher {
	
	@Autowired
	private SessionManager sessionManager;
	
	private List<EventListener> listeners;

	public SessionEventDispatcher(List<EventListener> listeners) {
		this.listeners = listeners;
	}
	
	public void recordSaved() {
		List<RecordEvent> events = sessionManager.flushPendingEvents();
		notifyListeners(events);
	}

	public void recordDeleted(CollectRecord record, String userName) {
		Survey survey = record.getSurvey();
		String surveyName = survey.getName();
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		notifyListeners(Arrays.asList(new RecordDeletedEvent(surveyName, record.getId(), rootEntityDefn.getId(), rootEntity.getInternalId(), new Date(), userName)));
	}
	
	private void notifyListeners(List<? extends RecordEvent> events) {
		for (EventListener eventListener : listeners) {
			eventListener.onEvents(events);
		}
	}
	
}
