package org.openforis.collect.model.proxy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.EntityChange;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityChangeProxy extends NodeChangeProxy<EntityChange> {

	public EntityChangeProxy(EntityChange change, Locale locale) {
		super(change, locale);
	}

	@ExternalizedProperty
	public Map<Integer, Boolean> getRelevant() {
		return convertToChildDefinitionIdMap(change.getChildrenRelevance());
	}

	@ExternalizedProperty
	public Map<Integer, Integer> getMinCountByChildDefinitionId() {
		return change.getMinCountByChildDefinitionId();
	}

	@ExternalizedProperty
	public Map<Integer, Integer> getMaxCountByChildDefinitionId() {
		return change.getMaxCountByChildDefinitionId();
	}

	@ExternalizedProperty
	public Map<Integer, ValidationResultFlag> getMinCountValidation() {
		return convertToChildDefinitionIdMap(change.getChildrenMinCountValidation());
	}

	@ExternalizedProperty
	public Map<Integer, ValidationResultFlag> getMaxCountValidation() {
		return convertToChildDefinitionIdMap(change.getChildrenMaxCountValidation());
	}

	private <V extends Object> Map<Integer, V> convertToChildDefinitionIdMap(Map<String, V> from) {
		EntityDefinition entityDef = change.getNode().getDefinition();
		Map<Integer, V> map = new HashMap<Integer, V>();
		Set<Entry<String, V>> entries = from.entrySet();
		for (Entry<String, V> entry : entries) {
			String childName = entry.getKey();
			Integer childDefId = entityDef.getChildDefinition(childName).getId();
			map.put(childDefId, entry.getValue());
		}
		return map;
	}

}