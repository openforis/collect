package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.AttributeAddResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddResponseProxy extends AttributeUpdateResponseProxy implements NodeAddResponseProxy {

	public AttributeAddResponseProxy(
			MessageContextHolder messageContextHolder,
			AttributeAddResponse response) {
		super(messageContextHolder, response);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, response.getNode());
	}

}