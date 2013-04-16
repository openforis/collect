package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.AddAttributeResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class AddAttributeResponseProxy extends AttributeUpdateResponseProxy implements AddNodeResponseProxy {

	public AddAttributeResponseProxy(
			MessageContextHolder messageContextHolder,
			AddAttributeResponse response) {
		super(messageContextHolder, response);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, response.getNode());
	}

}