package org.openforis.collect.command.handler;

import java.util.Locale;

import org.openforis.collect.command.DeleteNodeCommand;
import org.openforis.collect.event.EventListener;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.EventProducer.EventProducerContext;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

public class DeleteNodeCommandHandler<C extends DeleteNodeCommand> extends NodeCommandHandler<C> {

	public DeleteNodeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager, MessageSource messageSource) {
		super(surveyManager, recordProvider, recordManager, messageSource);
	}

	@Override
	public void execute(C command, EventListener eventListener) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findAttribute(command, record);
		NodeChangeSet changeSet = recordUpdater.deleteNode(attribute);
		
		recordManager.save(record);
		
		EventProducerContext context = new EventProducer.EventProducerContext(messageSource, Locale.ENGLISH, command.getUsername());
		new EventProducer(context, eventListener).produceFor(changeSet);
	}
	
}
