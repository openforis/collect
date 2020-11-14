package org.openforis.collect.command.handler;

import java.util.List;

import org.openforis.collect.command.UpdateMultipleAttributeCommand;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

public class UpdateMultipleAttributeCommandHandler<C extends UpdateMultipleAttributeCommand<?>>
		extends NodeCommandHandler<C> {

	@Override
	public RecordCommandResult executeForResult(C command) {
		CollectRecord record = findRecord(command);
		Entity parentEntity = record.findNodeByPath(command.getParentEntityPath());
		AttributeDefinition attrDef = record.getSurvey().getSchema().getDefinitionById(command.getNodeDefId());
		@SuppressWarnings("unchecked")
		List<Value> values = (List<Value>) command.getValues();
		NodeChangeSet nodeChangeSet;
		if (attrDef.isMultiple()) {
			nodeChangeSet = recordUpdater.updateMultipleAttribute(parentEntity, attrDef, values);
		} else {
			Attribute<?, Value> attribute = parentEntity.getChild(attrDef);
			Value value = values == null || values.isEmpty() ? command.getValue() : values.get(0);
			nodeChangeSet = recordUpdater.updateAttribute(attribute, value);
		}
		return new RecordCommandResult(record, nodeChangeSet);
	}
}
