package org.openforis.collect.relational;

import java.util.List;

import javax.annotation.PostConstruct;

import org.openforis.collect.event.EventBrokerEventQueue;
import org.openforis.collect.event.RecordStep;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.event.InitializeRDBEvent;

public class CollectRDBMonitor {

	private final EventBrokerEventQueue eventQueue;
	private final SurveyManager surveyManager;
	private final CollectLocalRDBStorageManager localRDBStorageManager;

	public CollectRDBMonitor(EventBrokerEventQueue eventQueue,
			SurveyManager surveyManager,
			CollectLocalRDBStorageManager localRDBStorageManager) {
		super();
		this.eventQueue = eventQueue;
		this.surveyManager = surveyManager;
		this.localRDBStorageManager = localRDBStorageManager;
	}

	@PostConstruct
	public void init() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		for (CollectSurvey survey : surveys) {
			for (RecordStep step : RecordStep.values()) {
				if (rdbMissing(survey, step)) {
					eventQueue.publish(new InitializeRDBEvent(survey.getName(),
							step));
				}
			}
		}
	}

	private boolean rdbMissing(CollectSurvey survey, RecordStep step) {
		return localRDBStorageManager.existsRDBFile(survey.getName(), step);
	}

}
