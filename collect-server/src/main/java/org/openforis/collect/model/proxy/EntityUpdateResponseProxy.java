package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.RecordUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;

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
	public Map<String, Object> getRelevant() {
		return response.getRelevant();
	}

	@ExternalizedProperty
	public Map<String, Object> getRequired() {
		return response.getRequired();
	}

	@ExternalizedProperty
	public Map<String, Object> getMinCountValidation() {
		return response.getMinCountValidation();
	}

	@ExternalizedProperty
	public Map<String, Object> getMaxCountValidation() {
		return response.getMaxCountValidation();
	}

}