/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.state.NodeState;

/**
 * @author M. Togna
 * 
 */
public class NodeStateProxy implements Proxy {

	private transient NodeState nodeState;

	public NodeStateProxy(NodeState nodeState) {
		this.nodeState = nodeState;
	}

	@ExternalizedProperty
	public Integer getNodeId() {
		return nodeState.getNode().getId();
	}

	@ExternalizedProperty
	public boolean isRelevant() {
		return nodeState.isRelevant();
	}

	@ExternalizedProperty
	public boolean isRequired() {
		return nodeState.isRequired();
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResults() {
		return new ValidationResultsProxy(nodeState.getValidationResults());
	}

}
