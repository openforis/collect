/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.RemarksUpdateRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class RemarksUpdateRequestProxy extends RecordUpdateRequestProxy<RemarksUpdateRequest> {
	
	private Integer nodeId;
	private Integer fieldIndex;
	private String remarks;
	
	@Override
	public RemarksUpdateRequest toUpdateRequest(CollectRecord record) {
		RemarksUpdateRequest request = new RecordUpdateRequest.RemarksUpdateRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		request.setAttribute(attribute);
		request.setFieldIndex(fieldIndex);
		request.setRemarks(remarks);
		return request;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public Integer getFieldIndex() {
		return fieldIndex;
	}

	public void setFieldIndex(Integer fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
