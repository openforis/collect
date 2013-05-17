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
public class FieldUpdateRequestProxy extends NodeUpdateRequestProxy<FieldUpdateRequest> {
	
	private Integer nodeId;
	private int fieldIndex;
	protected Object value;
	protected String remarks;
	protected FieldSymbol symbol;
	
	@Override
	public FieldUpdateRequest toNodeUpdateOptions(CollectRecord record) {
		FieldUpdateRequest opts = new NodeUpdateRequest.FieldUpdateRequest();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		Field<?> field = attribute.getField(fieldIndex);
		opts.setField(field);
		opts.setValue(value);
		opts.setSymbol(symbol);
		opts.setRemarks(remarks);
		return opts;	
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
