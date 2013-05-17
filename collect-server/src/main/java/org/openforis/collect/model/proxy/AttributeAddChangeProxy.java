package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeChange.AttributeAddChange;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddChangeProxy extends AttributeChangeProxy implements NodeAddChangeProxy {

	public AttributeAddChangeProxy(
			MessageContextHolder messageContextHolder,
			AttributeAddChange change) {
		super(messageContextHolder, change);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(messageContextHolder, change.getNode());
	}

}