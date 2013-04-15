/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.AddAttributeUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.AddEntityUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.ApplyDefaultValueUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.ApproveMissingValueUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.ConfirmErrorUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.DeleteNodeUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateAttributeUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateFieldUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateRemarksUpdateRequest;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordUpdateRequestProxy implements Proxy {
	
	public enum Method {
		ADD, UPDATE, DELETE, CONFIRM_ERROR, APPROVE_MISSING, UPDATE_REMARKS, APPLY_DEFAULT_VALUE;
	}
	
	private Integer parentEntityId;
	private String nodeName;
	private Integer nodeId;
	private Integer fieldIndex;
	private Object value;
	private Method method;
	private String remarks;
	private FieldSymbol symbol;
	
	public RecordUpdateRequest toUpdateRequest(CollectRecord record) {
		switch (method) {
		case ADD: 
			{
				Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
				EntityDefinition parentEntityDefn = parentEntity.getDefinition();
				NodeDefinition childDefn = parentEntityDefn.getChildDefinition(nodeName);
				if ( childDefn instanceof EntityDefinition ) {
					AddEntityUpdateRequest request = new RecordUpdateRequest.AddEntityUpdateRequest();
					request.setParentEntityId(parentEntityId);
					request.setNodeName(nodeName);
					return request;		
				} else {
					AddAttributeUpdateRequest<Value> request = new RecordUpdateRequest.AddAttributeUpdateRequest<Value>();
					request.setParentEntityId(parentEntityId);
					request.setNodeName(nodeName);
					request.setRemarks(remarks);
					request.setSymbol(symbol);
					Value parsedValue = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) childDefn, value);
					request.setValue(parsedValue);
					return request;
				}
			}
		case APPLY_DEFAULT_VALUE:
			{
			ApplyDefaultValueUpdateRequest request = new RecordUpdateRequest.ApplyDefaultValueUpdateRequest();
			Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			request.setAttribute(attribute);
			return request;
			}
		case APPROVE_MISSING:
			{
			ApproveMissingValueUpdateRequest request = new RecordUpdateRequest.ApproveMissingValueUpdateRequest();
			request.setParentEntityId(parentEntityId);
			request.setNodeName(nodeName);
			return request;
			}
		case CONFIRM_ERROR:
			{
			ConfirmErrorUpdateRequest request = new RecordUpdateRequest.ConfirmErrorUpdateRequest();
			Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			request.setAttribute(attribute);
			return request;
			}
		case DELETE:
			{
			DeleteNodeUpdateRequest request = new RecordUpdateRequest.DeleteNodeUpdateRequest();
			Node<?> node = record.getNodeByInternalId(nodeId);
			request.setNode(node);
			return request;
			}
		case UPDATE:
			{
			if ( fieldIndex < 0 ) {
				UpdateAttributeUpdateRequest<Value> request = new RecordUpdateRequest.UpdateAttributeUpdateRequest<Value>();
				@SuppressWarnings("unchecked")
				Attribute<AttributeDefinition, Value> attribute = (Attribute<AttributeDefinition, Value>) record.getNodeByInternalId(nodeId);
				request.setAttribute(attribute);
				Value parsedValue = parseCompositeAttributeValue(attribute.getParent(), attribute.getDefinition(), value);
				request.setValue(parsedValue);
				request.setSymbol(symbol);
				request.setRemarks(remarks);
				return request;
			} else {
				UpdateFieldUpdateRequest request = new RecordUpdateRequest.UpdateFieldUpdateRequest();
				Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
				request.setAttribute(attribute);
				request.setFieldIndex(fieldIndex);
				request.setValue(value);
				request.setSymbol(symbol);
				request.setRemarks(remarks);
				return request;	
			}
		}
		case UPDATE_REMARKS:
			{
			UpdateRemarksUpdateRequest request = new RecordUpdateRequest.UpdateRemarksUpdateRequest();
			Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			request.setAttribute(attribute);
			request.setFieldIndex(fieldIndex);
			request.setRemarks(remarks);
			return request;
			}
		default:
			throw new IllegalArgumentException("Method not supported: " + method);
		} 
	}
	
	protected Value parseCompositeAttributeValue(Entity parentEntity, AttributeDefinition defn, Object value) {
		Value result;
		if(defn instanceof CodeAttributeDefinition) {
			if ( value instanceof String) {
				String stringVal = (String) value;
				result = parseCode(parentEntity, (CodeAttributeDefinition) defn, stringVal );
			} else {
				throw new IllegalArgumentException("Invalid value type: expected String");
			}
		} else if(defn instanceof RangeAttributeDefinition) {
			if ( value instanceof String) {
				String stringVal = (String) value;
				RangeAttributeDefinition rangeDef = (RangeAttributeDefinition) defn;
				RangeAttributeDefinition.Type type = rangeDef.getType();
				NumericRange<?> range = null;
				Unit unit = null; //todo check if unit is required here or is set later by the client
				switch(type) {
					case INTEGER:
						range = IntegerRange.parseIntegerRange(stringVal, unit);
						break;
					case REAL:
						range = RealRange.parseRealRange(stringVal, unit);
						break;
				}
				result = range;
			} else {
				throw new IllegalArgumentException("Invalid value type: expected String");
			}
		} else {
			throw new IllegalArgumentException("Invalid AttributeDefinition: expected CodeAttributeDefinition or RangeAttributeDefinition");
		}
		return result;
	}
	
	protected Code parseCode(Entity parent, CodeAttributeDefinition def, String value) {
		CollectRecord record = (CollectRecord) parent.getRecord();
		List<CodeListItem> items = record.getAssignableCodeListItems(parent, def);
		Code code = parseCode(record, value, items);
		return code;
	}
	
	protected Code parseCode(CollectRecord record, String value, List<CodeListItem> codeList) {
		Code code = null;
		String[] strings = value.split(":");
		String codeStr = null;
		String qualifier = null;
		switch(strings.length) {
			case 2:
				qualifier = strings[1].trim();
			case 1:
				codeStr = strings[0].trim();
				break;
			default:
				//TODO throw error: invalid parameter
		}
		CodeListItem codeListItem = record.findCodeListItem(codeList, codeStr);
		if(codeListItem != null) {
			code = new Code(codeListItem.getCode(), qualifier);
		}
		if (code == null) {
			code = new Code(codeStr, qualifier);
		}
		return code;
	}
	
	public Integer getParentEntityId() {
		return parentEntityId;
	}
	
	public void setParentEntityId(Integer parentEntityId) {
		this.parentEntityId = parentEntityId;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method;
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
