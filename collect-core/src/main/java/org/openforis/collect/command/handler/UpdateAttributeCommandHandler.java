package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

public class UpdateAttributeCommandHandler<C extends UpdateAttributeCommand> implements CommandHandler<List<RecordEvent>, C> {

	private RecordManager recordManager;
	private SurveyManager surveyManager;
	
	private RecordUpdater recordUpdater = new RecordUpdater();
	
	public UpdateAttributeCommandHandler(RecordManager recordManager, SurveyManager surveyManager) {
		super();
		this.recordManager = recordManager;
		this.surveyManager = surveyManager;
	}

	@Override
	public List<RecordEvent> execute(C command) {
		Value value = extractValue(command);
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		CollectRecord record = recordManager.load(survey, command.getRecordId());
		Entity parentEntity = (Entity) record.getNodeByInternalId(command.getParentEntityId());
		AttributeDefinition attributeDef = (AttributeDefinition) survey.getSchema().getDefinitionById(command.getAttributeDefId());
		Attribute<?,Value> attribute = parentEntity.getChild(attributeDef);
		NodeChangeSet changeSet = recordUpdater.updateAttribute(attribute, value);
		
		List<RecordEvent> events = new EventProducer().produceFor(changeSet, command.getUsername());
		return events;
	}
	
	private Value extractValue(C command) {
		if (command instanceof UpdateCodeAttributeCommand) {
			return new Code(((UpdateCodeAttributeCommand) command).getCode());
		} else if (command instanceof UpdateBooleanAttributeCommand) {
			return new BooleanValue(((UpdateBooleanAttributeCommand) command).getValue());
		} else {
			throw new IllegalArgumentException("Unsupported update attribute command type: " + command);
		}
	}

}
