/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.NodeDeleteRequest;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteRequestProxy extends RecordUpdateRequestProxy<NodeDeleteRequest> {
	
	private Integer nodeId;

	@Override
	public NodeDeleteRequest toUpdateRequest(CollectRecord record) {
		NodeDeleteRequest request = new RecordUpdateRequest.NodeDeleteRequest();
		Node<?> node = record.getNodeByInternalId(nodeId);
		request.setNode(node);
		return request;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}