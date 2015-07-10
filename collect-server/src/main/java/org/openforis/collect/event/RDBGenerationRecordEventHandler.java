package org.openforis.collect.event;

import org.fao.foris.simpleeventbroker.AbstractEventHandler;
import org.fao.foris.simpleeventbroker.EventHandlerMonitor;
import org.openforis.collect.relational.CollectRDBGenerator;
import org.openforis.collect.relational.event.InitializeRDBEvent;

public class RDBGenerationRecordEventHandler extends AbstractEventHandler {

	private static final int TIEMOUT_MILLIS = 60000;
	private CollectRDBGenerator rdbGenerator;

	protected RDBGenerationRecordEventHandler(CollectRDBGenerator rdbGenerator) {
		super(RDBGenerationRecordEventHandler.class.getSimpleName(), TIEMOUT_MILLIS);
		this.rdbGenerator = rdbGenerator;
	}

	@Override
	public void handle(Object event, EventHandlerMonitor eventHandlerMonitor) {
		if (event instanceof RecordTransaction) {
			rdbGenerator.process((RecordTransaction) event);
		} else if (event instanceof SurveyEvent) {
			if (event instanceof SurveyCreatedEvent) {
				rdbGenerator.createRDBs(((SurveyCreatedEvent) event).getSurveyName());
			} else if (event instanceof SurveyDeletedEvent) {
				rdbGenerator.deleteRDBs(((SurveyDeletedEvent) event).getSurveyName());
			}
		} else if (event instanceof InitializeRDBEvent) {
			InitializeRDBEvent evt = (InitializeRDBEvent) event;
			rdbGenerator.createRDB(evt.getSurveyName(), evt.getStep());
		}
	}

	
}
