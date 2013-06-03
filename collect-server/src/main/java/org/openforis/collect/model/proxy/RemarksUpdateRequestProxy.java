/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.RemarksUpdateRequest;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * 
 * @author S. Ricci
 *
 */
public class RemarksUpdateRequestProxy extends NodeUpdateRequestProxy<RemarksUpdateRequest> {
	
	private Integer nodeId;
	private Integer fieldIndex;
	private String remarks;
	
	@Override
	public RemarksUpdateRequest toNodeUpdateRequest(CollectRecord record) {
		RemarksUpdateRequest o = new NodeUpdateRequest.RemarksUpdateRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		Field<?> field = attribute.getField(fieldIndex);
		o.setField(field);
		o.setRemarks(remarks);
		return o;
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
