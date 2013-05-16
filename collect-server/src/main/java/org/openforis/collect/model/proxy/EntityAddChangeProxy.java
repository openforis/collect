package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeChange.EntityAddChange;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddChangeProxy extends EntityChangeProxy implements NodeAddChangeProxy {

	public EntityAddChangeProxy(
			MessageContextHolder messageContextHolder,
			EntityAddChange change) {
		super(messageContextHolder, change);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, change.getNode());
	}
	
}