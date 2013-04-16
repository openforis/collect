package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.RecordUpdateResponse.NodeUpdateResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 * @param <T> NodeUpdateResponse
 */
public class NodeUpdateResponseProxy<T extends NodeUpdateResponse<?>> extends RecordUpdateResponseProxy<T> {

	public NodeUpdateResponseProxy(
			MessageContextHolder messageContextHolder,
			T response) {
		super(messageContextHolder, response);
	}
	
	@ExternalizedProperty
	public int getNodeId() {
		return response.getNode().getInternalId();
	}

}