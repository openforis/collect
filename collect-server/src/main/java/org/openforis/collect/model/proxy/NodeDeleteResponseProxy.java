package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.NodeDeleteResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteResponseProxy extends NodeUpdateResponseProxy<NodeDeleteResponse> {

	public NodeDeleteResponseProxy(
			MessageContextHolder messageContextHolder,
			NodeDeleteResponse response) {
		super(messageContextHolder, response);
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return response.getNode().getInternalId();
	}

}