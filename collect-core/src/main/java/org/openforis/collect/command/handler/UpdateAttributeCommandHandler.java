package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.event.EventProducer;
import org.openforis.collect.event.RecordEvent;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordProvider;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Value;

public class UpdateAttributeCommandHandler<C extends UpdateAttributeCommand> extends NodeCommandHandler<C> {

	public UpdateAttributeCommandHandler(SurveyManager surveyManager, RecordProvider recordProvider, RecordManager recordManager) {
		super(surveyManager, recordProvider, recordManager);
	}

	@Override
	public List<RecordEvent> execute(UpdateAttributeCommand command) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findAttribute(command, record);
		Value value = extractValue(command);
		NodeChangeSet changeSet = recordUpdater.updateAttribute(attribute, value);
		
		recordManager.save(record);
		
		List<RecordEvent> events = new EventProducer().produceFor(changeSet, command.getUsername());
		return events;
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
