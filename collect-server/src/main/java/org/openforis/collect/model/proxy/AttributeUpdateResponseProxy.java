package org.openforis.collect.model.proxy;

import java.util.Map;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeUpdateResponseProxy extends NodeUpdateResponseProxy<AttributeUpdateResponse> {

	public AttributeUpdateResponseProxy(
			MessageContextHolder messageContextHolder,
			AttributeUpdateResponse response) {
		super(messageContextHolder, response);
	}
	
	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		if ( response.getValidationResults() == null ) {
			return null;
		} else {
			return new ValidationResultsProxy(messageContextHolder, (Attribute<?, ?>) response.getNode(), response.getValidationResults());
		}
	}

	@ExternalizedProperty
	public Map<Integer, Object> getUpdatedFieldValues() {
		return response.getUpdatedFieldValues();
	}
	
}