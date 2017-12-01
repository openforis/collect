package org.openforis.collect.command.handler;

import java.util.Date;

import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;

public class DeleteRecordHandler implements CommandHandler<RecordEvent, DeleteRecordCommand> {

	private RecordManager recordManager;
	private SurveyManager surveyManager;
	
	public DeleteRecordHandler(RecordManager recordManager, SurveyManager surveyManager) {
		super();
		this.recordManager = recordManager;
		this.surveyManager = surveyManager;
	}

	@Override
	public RecordEvent execute(DeleteRecordCommand command) {
		try {
			CollectSurvey survey = surveyManager.getById(command.getSurveyId());
			recordManager.delete(command.getRecordId());
			return new RecordDeletedEvent(survey.getName(), command.getRecordId(), new Date(), command.getUsername());
		} catch (RecordPersistenceException e) {
			throw new RuntimeException(e);
		}
	}
	
}
