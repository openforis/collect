/**
 * 
 */
package org.openforis.collect.model.validation;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.AtomicAttribute;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.state.NodeState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class RecordKeyUniquenessValidator implements ValidationRule {

	@Autowired
	private RecordManager recordManager;

	@Override
	public boolean evaluate(NodeState nodeState) {
		Attribute<?, ?> attr = (Attribute<?, ?>) nodeState.getNode();
		Entity rootEntity = attr.getParent();
		CollectRecord record = (CollectRecord) rootEntity.getRecord();
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		String[] keys = getKeys(rootEntity);

		List<CollectRecord> records = recordManager.getSummaries(survey, rootEntity.getName(), keys);
		return checkUniqueness(records, record);
	}

	private boolean checkUniqueness(List<CollectRecord> records, CollectRecord record) {
		int count = 0;
		for (CollectRecord collectRecord : records) {
			if (!collectRecord.getId().equals(record.getId())) {
				count++;
			}
		}
		return count == 0;
	}

	private String[] getKeys(Entity entity) {
		List<String> keys = new ArrayList<String>();
		List<AttributeDefinition> keyAttributeDefinitions = getKeyAttributeDefinitions(entity);
		for (NodeDefinition keyAttributeDefinition : keyAttributeDefinitions) {
			String keyName = keyAttributeDefinition.getName();
			AtomicAttribute<?, ?> attribute = (AtomicAttribute<?, ?>) entity.get(keyName, 0);
			Object object = attribute.getValue();
			keys.add(object.toString());
		}
		return keys.toArray(new String[] {});
	}

	private List<AttributeDefinition> getKeyAttributeDefinitions(Entity entity) {
		List<AttributeDefinition> keyAttributeDefinitions = new ArrayList<AttributeDefinition>();
		EntityDefinition definition = entity.getDefinition();
		List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			if (childDefinition instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) childDefinition).isKey()) {
				keyAttributeDefinitions.add((AttributeDefinition) childDefinition);
			}
		}
		return keyAttributeDefinitions;
	}

}
