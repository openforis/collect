/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.ErrorConfirmRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class ConfirmErrorRequestProxy extends NodeUpdateRequestProxy<ErrorConfirmRequest> {
	
	private Integer nodeId;

	@Override
	public ErrorConfirmRequest toNodeUpdateRequest(CollectRecord record) {
		ErrorConfirmRequest opts = new NodeUpdateRequest.ErrorConfirmRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		opts.setAttribute(attribute);
		return opts;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}