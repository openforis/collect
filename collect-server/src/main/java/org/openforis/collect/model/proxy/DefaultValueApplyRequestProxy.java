/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.DefaultValueApplyRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class DefaultValueApplyRequestProxy extends NodeUpdateRequestProxy<DefaultValueApplyRequest> {
	
	private Integer nodeId;

	@Override
	public DefaultValueApplyRequest toNodeUpdateRequest(CollectRecord record) {
		DefaultValueApplyRequest opts = new NodeUpdateRequest.DefaultValueApplyRequest();
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