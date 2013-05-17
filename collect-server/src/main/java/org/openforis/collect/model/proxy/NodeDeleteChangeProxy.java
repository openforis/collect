package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeChange.NodeDeleteChange;
import org.openforis.collect.spring.MessageContextHolder;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChangeProxy extends NodeChangeProxy<NodeDeleteChange> {

	public NodeDeleteChangeProxy(
			MessageContextHolder messageContextHolder,
			NodeDeleteChange change) {
		super(messageContextHolder, change);
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return change.getNode().getInternalId();
	}

}