/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.NodeDeleteRequest;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteRequestProxy extends NodeUpdateRequestProxy<NodeDeleteRequest> {
	
	private Integer nodeId;

	@Override
	public NodeDeleteRequest toNodeUpdateRequest(CollectRecord record) {
		NodeDeleteRequest result = new NodeUpdateRequest.NodeDeleteRequest();
		Node<?> node = record.getNodeByInternalId(nodeId);
		result.setNode(node);
		return result;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}