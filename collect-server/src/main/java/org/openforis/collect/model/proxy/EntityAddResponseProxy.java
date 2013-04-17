package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeUpdateResponse.EntityAddResponse;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddResponseProxy extends EntityUpdateResponseProxy implements NodeAddResponseProxy {

	public EntityAddResponseProxy(
			MessageContextHolder messageContextHolder,
			EntityAddResponse response) {
		super(messageContextHolder, response);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, response.getNode());
	}
	
}