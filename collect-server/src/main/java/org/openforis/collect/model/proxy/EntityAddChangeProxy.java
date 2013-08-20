package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.EntityChange;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddChangeProxy extends EntityChangeProxy implements NodeAddChangeProxy {

	public EntityAddChangeProxy(EntityChange change) {
		super(change);
	}

	@Override
	@ExternalizedProperty
	public NodeProxy getCreatedNode() {
		return NodeProxy.fromNode(change.getNode());
	}
	
}