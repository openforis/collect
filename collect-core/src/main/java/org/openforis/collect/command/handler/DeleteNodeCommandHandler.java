package org.openforis.collect.command.handler;

import org.openforis.collect.command.DeleteNodeCommand;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;

public class DeleteNodeCommandHandler<C extends DeleteNodeCommand> extends NodeCommandHandler<C> {

	private transient RecordFileManager recordFileManager;

	@Override
	public RecordCommandResult executeForResult(C command) {
		final CollectRecord record = findRecord(command);
		Node<?> node = findNode(command, record);

		if (node instanceof Entity) {
			deleteDescendantFiles(record, (Entity) node);
		}

		NodeChangeSet changeSet = recordUpdater.deleteNode(node);

		return new RecordCommandResult(record, changeSet);
	}

	private void deleteDescendantFiles(final CollectRecord record, Entity entity) {
		entity.traverseDescendants(new NodeVisitor() {
			public void visit(Node<? extends NodeDefinition> descendant, int idx) {
				if (descendant instanceof FileAttribute) {
					if (!record.isPreview()) {
						recordFileManager.deleteRepositoryFile((FileAttribute) descendant);
					}
				}
			}
		});
	}
	
	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}

}
