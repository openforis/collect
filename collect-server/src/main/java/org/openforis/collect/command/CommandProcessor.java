package org.openforis.collect.command;

import java.util.List;

import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventQueue;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.event.RecordTransaction;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.openforis.rmb.KeepAlive;
import org.openforis.rmb.KeepAliveMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class CommandProcessor implements KeepAliveMessageHandler<Command> {

	@Autowired
	private EventQueue eventQueue;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	
	private RecordUpdater recordUpdater;
	
	public CommandProcessor() {
		recordUpdater = new RecordUpdater();
	}
	
	@Override
	public void handle(Command command, KeepAlive keepAlive) {
		if (command instanceof CreateRecordCommand) {
			process((CreateRecordCommand) command);
		} else if (command instanceof UpdateAttributeCommand) {
			process((UpdateAttributeCommand) command);
		}
	}
	
	protected void process(CreateRecordCommand command) {
		String username = command.getUsername();
		User user = userManager.loadByUserName(username);
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		List<EntityDefinition> rootDefs = survey.getSchema().getRootEntityDefinitions();
		String firstRootEntityName = rootDefs.get(0).getName();
		
		CollectRecord record = recordManager.instantiateRecord(survey, firstRootEntityName, user, command.getFormVersion(), Step.ENTRY);
		NodeChangeSet changeSet = recordManager.initializeRecord(record);
		recordManager.save(record);

		if (eventQueue.isEnabled()) {
			List<RecordEvent> events = new EventProducer().produceFor(changeSet, user.getUsername());
			eventQueue.publish(new RecordTransaction(survey.getName(), record.getId(), record.getStep().toRecordStep(), events));
		}
	}
	
	protected void process(UpdateAttributeCommand command) {
		Value value = extractValue(command);
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		CollectRecord record = recordManager.load(survey, command.getRecordId());
		Entity parentEntity = (Entity) record.getNodeByInternalId(command.getParentEntityId());
		AttributeDefinition attributeDef = (AttributeDefinition) survey.getSchema().getDefinitionById(command.getAttributeDefId());
		Attribute<?,Value> attribute = parentEntity.getChild(attributeDef);
		NodeChangeSet changeSet = recordUpdater.updateAttribute(attribute, value);
		
		List<RecordEvent> events = new EventProducer().produceFor(changeSet, command.getUsername());
		eventQueue.publish(new RecordTransaction(survey.getName(), record.getId(), record.getStep().toRecordStep(), events));
	}

	private Value extractValue(UpdateAttributeCommand command) {
		if (command instanceof UpdateCodeAttributeCommand) {
			return new Code(((UpdateCodeAttributeCommand) command).getCode());
		} else if (command instanceof UpdateBooleanAttributeCommand) {
			return new BooleanValue(((UpdateBooleanAttributeCommand) command).getValue());
		} else {
			throw new IllegalArgumentException("Unsupported update attribute command type: " + command);
		}
	}

}
