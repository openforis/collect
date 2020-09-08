package org.openforis.collect.command.handler;

import java.util.Locale;

import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
import org.openforis.collect.command.UpdateTextAttributeCommand;
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
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

public class UpdateAttributeCommandHandler<C extends UpdateAttributeCommand> extends NodeCommandHandler<C> {

	public UpdateAttributeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager, MessageSource messageSource) {
		super(surveyManager, recordProvider, recordManager, messageSource);
	}

	@Override
	public void execute(UpdateAttributeCommand command, EventListener eventListener) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findAttribute(command, record);
		Value value = extractValue(command);
		NodeChangeSet changeSet = recordUpdater.updateAttribute(attribute, value);
		
		recordManager.save(record);
		
		EventProducerContext context = new EventProducer.EventProducerContext(messageSource, Locale.ENGLISH, command.getUsername());
		new EventProducer(context, eventListener).produceFor(changeSet);
	}
	
	private Value extractValue(UpdateAttributeCommand command) {
		if (command instanceof UpdateCodeAttributeCommand) {
			return new Code(((UpdateCodeAttributeCommand) command).getCode());
		} else if (command instanceof UpdateBooleanAttributeCommand) {
			return new BooleanValue(((UpdateBooleanAttributeCommand) command).getValue());
		} else if (command instanceof UpdateDateAttributeCommand) {
			return Date.parse(((UpdateDateAttributeCommand) command).getValue());
		} else if (command instanceof UpdateTextAttributeCommand) {
			return new TextValue(((UpdateTextAttributeCommand) command).getValue());
		} else {
			throw new IllegalArgumentException("Unsupported update attribute command type: " + command);
		}
	}
}
