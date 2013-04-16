package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityUpdateResponseProxy extends NodeUpdateResponseProxy<EntityUpdateResponse> {

	public EntityUpdateResponseProxy(
			MessageContextHolder messageContextHolder,
			EntityUpdateResponse response) {
		super(messageContextHolder, response);
	}
	
	@ExternalizedProperty
	public Map<String, Boolean> getRelevant() {
		return response.getChildrenRelevance();
	}

	@ExternalizedProperty
	public Map<String, Boolean> getRequired() {
		return response.getChildrenRequireness();
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getMinCountValidation() {
		return response.getChildrenMinCountValidation();
	}

	@ExternalizedProperty
	public Map<String, ValidationResultFlag> getMaxCountValidation() {
		return response.getChildrenMaxCountValidation();
	}

}