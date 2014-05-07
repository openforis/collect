package org.openforis.collect.model.proxy;

import java.util.Locale;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.model.NodeDeleteChange;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChangeProxy extends NodeChangeProxy<NodeDeleteChange> {

	public NodeDeleteChangeProxy(NodeDeleteChange change, Locale locale) {
		super(change, locale);
	}

	@ExternalizedProperty
	public Integer getDeletedNodeId() {
		return change.getNode().getInternalId();
	}

}