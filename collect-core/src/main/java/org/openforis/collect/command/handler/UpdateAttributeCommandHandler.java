package org.openforis.collect.command.handler;

import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.command.UpdateBooleanAttributeCommand;
import org.openforis.collect.command.UpdateCodeAttributeCommand;
import org.openforis.collect.command.UpdateDateAttributeCommand;
import org.openforis.collect.command.UpdateTextAttributeCommand;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

public class UpdateAttributeCommandHandler<C extends UpdateAttributeCommand> extends NodeCommandHandler<C> {

	@Override
	public RecordCommandResult executeForResult(UpdateAttributeCommand command) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findAttribute(command, record);
		Value value = extractValue(command);
		NodeChangeSet nodeChangeSet = recordUpdater.updateAttribute(attribute, value);
		return new RecordCommandResult(record, nodeChangeSet);
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
