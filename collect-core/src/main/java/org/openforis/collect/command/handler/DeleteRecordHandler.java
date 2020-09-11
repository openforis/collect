package org.openforis.collect.command.handler;

import java.util.Date;

import org.openforis.collect.command.DeleteRecordCommand;
import org.openforis.collect.event.RecordDeletedEvent;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordPersistenceException;

public class DeleteRecordHandler extends RecordCommandHandler<DeleteRecordCommand> {

	@Override
	protected RecordCommandResult executeForResult(DeleteRecordCommand command) {
		try {
			CollectSurvey survey = surveyManager.getById(command.getSurveyId());
			recordManager.delete(command.getRecordId());

			RecordCommandResult result = new RecordCommandResult();
			result.setEvent(
					new RecordDeletedEvent(survey.getName(), command.getRecordId(), new Date(), command.getUsername()));
			return result;
		} catch (RecordPersistenceException e) {
			throw new RuntimeException(e);
		}
	}

}
