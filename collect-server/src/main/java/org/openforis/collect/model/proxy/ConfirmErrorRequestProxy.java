/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.ErrorConfirmRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class ConfirmErrorRequestProxy extends RecordUpdateRequestProxy<ErrorConfirmRequest> {
	
	private Integer nodeId;

	@Override
	public ErrorConfirmRequest toUpdateRequest(CollectRecord record) {
		ErrorConfirmRequest request = new RecordUpdateRequest.ErrorConfirmRequest();
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