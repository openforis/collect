package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.RecordUpdateResponse.DeleteNodeResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class DeleteNodeResponseProxy extends RecordUpdateResponseProxy<DeleteNodeResponse> {

	public DeleteNodeResponseProxy(
			MessageContextHolder messageContextHolder,
			DeleteNodeResponse response) {
		super(messageContextHolder, response);
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return response.getDeletedNodeId();
	}

}