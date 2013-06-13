package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeDeleteChange;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChangeProxy extends NodeChangeProxy<NodeDeleteChange> {

	public NodeDeleteChangeProxy(NodeDeleteChange change) {
		super(change);
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return change.getNode().getInternalId();
	}

}