/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.state.NodeState;

/**
 * @author M. Togna
 * 
 */
@Deprecated
public class NodeStateProxy implements Proxy {

	private transient NodeState nodeState;

	public NodeStateProxy(NodeState nodeState) {
		this.nodeState = nodeState;
	}

	public static List<NodeStateProxy> fromList(List<NodeState> list) {
		List<NodeStateProxy> result = new ArrayList<NodeStateProxy>();
		if(list != null) {
			for (NodeState nodeState : list) {
				NodeStateProxy proxy = new NodeStateProxy(nodeState);
				result.add(proxy);
			}
		}
		return result;
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
