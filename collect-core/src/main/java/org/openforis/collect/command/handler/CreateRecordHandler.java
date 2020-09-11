package org.openforis.collect.command.handler;

import java.util.Iterator;
import java.util.List;

import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

public class CreateRecordHandler extends RecordCommandHandler<CreateRecordCommand> {

	private RecordUpdater recordUpdater;

	public CreateRecordHandler() {
		this.recordUpdater = new RecordUpdater();
	}

	@Override
	protected RecordCommandResult executeForResult(CreateRecordCommand command) {
		String username = command.getUsername();
		User user = userManager.loadByUserName(username);
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		List<EntityDefinition> rootDefs = survey.getSchema().getRootEntityDefinitions();
		EntityDefinition firstRootEntity = rootDefs.get(0);
		String firstRootEntityName = firstRootEntity.getName();

		final CollectRecord record = recordManager.instantiateRecord(survey, firstRootEntityName, user,
				command.getFormVersion(), Step.ENTRY);
		NodeChangeSet changeSet = recordManager.initializeRecord(record);

		List<String> keyValues = command.getKeyValues();
		Iterator<String> keyValuesIt = keyValues.iterator();

		List<AttributeDefinition> keyAttributeDefinitions = firstRootEntity.getKeyAttributeDefinitions();
		Iterator<AttributeDefinition> keyDefsIt = keyAttributeDefinitions.iterator();

		while (keyDefsIt.hasNext()) {
			AttributeDefinition keyDef = keyDefsIt.next();
			String keyVal = keyValuesIt.next();
			Value keyValue = keyDef.createValue(keyVal);
			Attribute<?, Value> keyAttr = record.findNodeByPath(keyDef.getPath());
			recordUpdater.updateAttribute(keyAttr, keyValue);
		}
		
		return new RecordCommandResult(record, changeSet);
	}

	@Override
	protected RecordEvent transformEvent(RecordCommandResult result, RecordEvent event) {
		event.initializeRecordId(result.getRecord().getId());
		return event;
	}

}
