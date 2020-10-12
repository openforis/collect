package org.openforis.collect.command.handler;

import org.openforis.collect.command.NodeCommand;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

public abstract class NodeCommandHandler<C extends NodeCommand> extends RecordCommandHandler<C> {

	protected RecordUpdater recordUpdater = new RecordUpdater();

	protected  <N extends Node<?>> N findNode(NodeCommand command) {
		CollectRecord record = findRecord(command);
		return findNode(command, record);
	}

	protected  <N extends Node<?>> N findNode(NodeCommand command, CollectRecord record) {
		return record.findNodeByPath(command.getNodePath());
	}

	protected Entity findParentEntity(NodeCommand command) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = record.findNodeByPath(command.getParentEntityPath());
		return parentEntity;
	}

}