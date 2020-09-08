package org.openforis.collect.command.handler;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.command.CreateRecordCommand;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.MessageSource;
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
import org.openforis.idm.model.Value;

public class CreateRecordHandler implements CommandHandler<CreateRecordCommand> {

	private RecordManager recordManager;
	private SurveyManager surveyManager;
	private UserManager userManager;
	private MessageSource messageSource;
	
	private RecordUpdater recordUpdater;
	
	public CreateRecordHandler(RecordManager recordManager, SurveyManager surveyManager, UserManager userManager, MessageSource messageSource) {
		super();
		this.recordManager = recordManager;
		this.surveyManager = surveyManager;
		this.userManager = userManager;
		this.messageSource = messageSource;

		this.recordUpdater = new RecordUpdater();
	}

	@Override
	public void execute(CreateRecordCommand command, final EventListener eventListener) {
		String username = command.getUsername();
		User user = userManager.loadByUserName(username);
		CollectSurvey survey = surveyManager.getById(command.getSurveyId());
		List<EntityDefinition> rootDefs = survey.getSchema().getRootEntityDefinitions();
		EntityDefinition firstRootEntity = rootDefs.get(0);
		String firstRootEntityName = firstRootEntity.getName();
		
		final CollectRecord record = recordManager.instantiateRecord(survey, firstRootEntityName, user, command.getFormVersion(), Step.ENTRY);
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
		recordManager.save(record);
		EventProducerContext context = new EventProducerContext(messageSource, Locale.ENGLISH, user.getUsername());
		new EventProducer(context, new EventListener() {
			public void onEvent(RecordEvent event) {
				event.initializeRecordId(record.getId());
				eventListener.onEvent(event);
			}
		}).produceFor(changeSet);
	}
	
}
