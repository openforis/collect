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
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationRule;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class RecordKeyUniquenessValidator implements ValidationRule<Attribute<?, ?>> {

	private RecordManager recordManager;
	
	public RecordKeyUniquenessValidator(RecordManager recordManager) {
		this.recordManager = recordManager;
	}

	@Override
	public ValidationResultFlag evaluate(Attribute<?, ?> node) {
		Entity rootEntity = node.getParent();
		CollectRecord record = (CollectRecord) rootEntity.getRecord();
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		String[] keys = getKeys(rootEntity);

		List<CollectRecord> records = recordManager.getSummaries(survey, rootEntity.getName(), keys);
		boolean unique = checkUniqueness(records, record);
		return ValidationResultFlag.valueOf(unique);
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
			Attribute<?, ?> attribute = (Attribute<?, ?>) entity.get(keyName, 0);
			Field<?> field = attribute.getField(0);
			String value = "";
			if(field.getValue() != null){
				value = field.getValue().toString();
			}
			keys.add(value);
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
