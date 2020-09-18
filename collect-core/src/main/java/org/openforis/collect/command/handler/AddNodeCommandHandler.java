package org.openforis.collect.command.handler;

import org.openforis.collect.command.AddNodeCommand;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;

public class AddNodeCommandHandler<C extends AddNodeCommand> extends NodeCommandHandler<C> {

	@Override
	public RecordCommandResult executeForResult(C command) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = record.findNodeByPath(command.getParentEntityPath());
		NodeDefinition nodeDef = parentEntity.getDefinition().getChildDefinition(command.getNodeDefId());
		NodeChangeSet changeSet = recordUpdater.addNode(parentEntity, nodeDef);
		
		return new RecordCommandResult(record, changeSet);
	}
	
}
