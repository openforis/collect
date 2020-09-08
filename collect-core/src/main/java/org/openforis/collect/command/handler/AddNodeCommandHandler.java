package org.openforis.collect.command.handler;

import java.util.Locale;

import org.openforis.collect.command.AddNodeCommand;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;

public class AddNodeCommandHandler<C extends AddNodeCommand> extends NodeCommandHandler<C> {

	public AddNodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager, MessageSource messageSource) {
		super(surveyManager, recordProvider, recordManager, messageSource);
	}

	@Override
	public void execute(C command, EventListener eventListener) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = record.findNodeByPath(command.getParentEntityPath());
		NodeDefinition nodeDef = parentEntity.getDefinition().getChildDefinition(command.getNodeDefId());
		NodeChangeSet changeSet = recordUpdater.addNode(parentEntity, nodeDef);
		
		recordManager.save(record);
		
		EventProducerContext context = new EventProducer.EventProducerContext(messageSource, Locale.ENGLISH, command.getUsername());
		new EventProducer(context, eventListener).produceFor(changeSet);
	}
	
}
