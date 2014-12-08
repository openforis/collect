package org.openforis.collect.model.proxy;

import java.util.Locale;
import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.EntityChange;
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
	public Map<String, Boolean> getRelevant() {
		return change.getChildrenRelevance();
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
	public Map<String, ValidationResultFlag> getMinCountValidation() {
		return change.getChildrenMinCountValidation();
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getMaxCountValidation() {
		return change.getChildrenMaxCountValidation();
	}

}