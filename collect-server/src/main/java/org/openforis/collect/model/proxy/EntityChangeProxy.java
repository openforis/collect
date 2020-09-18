package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.model.EntityChange;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityChangeProxy extends NodeChangeProxy<EntityChange> {

	public EntityChangeProxy(EntityChange change, ProxyContext context) {
		super(change, context);
	}

	@ExternalizedProperty
	public Map<Integer, Boolean> getRelevant() {
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
	public Map<Integer, ValidationResultFlag> getMinCountValidation() {
		return change.getChildrenMinCountValidation();
	}

	@ExternalizedProperty
	public Map<Integer, ValidationResultFlag> getMaxCountValidation() {
		return change.getChildrenMaxCountValidation();
	}

}