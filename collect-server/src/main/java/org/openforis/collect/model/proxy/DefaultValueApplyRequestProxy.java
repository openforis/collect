/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.DefaultValueApplyRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class DefaultValueApplyRequestProxy extends RecordUpdateRequestProxy<DefaultValueApplyRequest> {
	
	private Integer nodeId;

	@Override
	public DefaultValueApplyRequest toUpdateRequest(CollectRecord record) {
		DefaultValueApplyRequest request = new RecordUpdateRequest.DefaultValueApplyRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		request.setAttribute(attribute);
		return request;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}