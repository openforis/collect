/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.FieldUpdateRequest;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * 
 * @author S. Ricci
 *
 */
public class FieldUpdateRequestProxy extends NodeUpdateRequestProxy<FieldUpdateRequest<?>> {
	
	private Integer nodeId;
	private int fieldIndex;
	protected String value;
	protected String remarks;
	protected FieldSymbol symbol;
	
	@Override
	public FieldUpdateRequest<?> toNodeUpdateRequest(CollectRecord record) {
		return toFieldUpdateRequest(record);	
	}
	protected <T> FieldUpdateRequest<T> toFieldUpdateRequest(CollectRecord record) {
		FieldUpdateRequest<T> req = new NodeUpdateRequest.FieldUpdateRequest<T>();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		@SuppressWarnings("unchecked")
		Field<T> field = (Field<T>) attribute.getField(fieldIndex);
		req.setField(field);
		T parsedValue = field.parseValue(value);
		req.setValue(parsedValue);
		req.setSymbol(symbol);
		req.setRemarks(remarks);
		return req;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
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
