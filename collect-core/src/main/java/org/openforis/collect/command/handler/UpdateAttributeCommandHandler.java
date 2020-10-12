package org.openforis.collect.command.handler;

import org.openforis.collect.command.UpdateAttributeCommand;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;

public class UpdateAttributeCommandHandler<C extends UpdateAttributeCommand<?>> extends NodeCommandHandler<C> {

	@Override
	public RecordCommandResult executeForResult(C command) {
		CollectRecord record = findRecord(command);
		Attribute<?, Value> attribute = findNode(command, record);
		NodeChangeSet nodeChangeSet = recordUpdater.updateAttribute(attribute, command.getValue());
		return new RecordCommandResult(record, nodeChangeSet);
	}
}
