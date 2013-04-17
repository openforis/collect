/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.FieldUpdateRequest;
import org.openforis.idm.model.Attribute;

/**
 * 
 * @author S. Ricci
 *
 */
public class FieldUpdateRequestProxy extends RecordUpdateRequestProxy<FieldUpdateRequest> {
	
	private Integer nodeId;
	private int fieldIndex;
	protected Object value;
	protected String remarks;
	protected FieldSymbol symbol;
	
	@Override
	public FieldUpdateRequest toUpdateRequest(CollectRecord record) {
		FieldUpdateRequest request = new RecordUpdateRequest.FieldUpdateRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		request.setAttribute(attribute);
		request.setFieldIndex(fieldIndex);
		request.setValue(value);
		request.setSymbol(symbol);
		request.setRemarks(remarks);
		return request;	
	}
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public int getFieldIndex() {
		return fieldIndex;
	}

	public void setFieldIndex(int fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public FieldSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(FieldSymbol symbol) {
		this.symbol = symbol;
	}
	
}
