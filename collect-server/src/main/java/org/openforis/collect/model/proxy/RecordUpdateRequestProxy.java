/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.manager.RecordFileException;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.AddAttributeRequest;
import org.openforis.collect.model.RecordUpdateRequest.AddEntityRequest;
import org.openforis.collect.model.RecordUpdateRequest.ApplyDefaultValueRequest;
import org.openforis.collect.model.RecordUpdateRequest.ApproveMissingValueRequest;
import org.openforis.collect.model.RecordUpdateRequest.ConfirmErrorRequest;
import org.openforis.collect.model.RecordUpdateRequest.DeleteNodeRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateAttributeRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateFieldRequest;
import org.openforis.collect.model.RecordUpdateRequest.UpdateRemarksRequest;
import org.openforis.collect.remoting.service.FileWrapper;
import org.openforis.collect.web.session.SessionState;
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
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
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
	
	@SuppressWarnings("unchecked")
	//TODO use RecordUpdateRequestProxy subclasses instead
	public RecordUpdateRequest toUpdateRequest(CollectRecord record, RecordFileManager fileManager, SessionManager sessionManager) {
		switch (method) {
		case ADD: 
			{
				Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
				EntityDefinition parentEntityDefn = parentEntity.getDefinition();
				NodeDefinition childDefn = parentEntityDefn.getChildDefinition(nodeName);
				if ( childDefn instanceof EntityDefinition ) {
					AddEntityRequest request = new RecordUpdateRequest.AddEntityRequest();
					request.setParentEntityId(parentEntityId);
					request.setNodeName(nodeName);
					return request;		
				} else {
					AddAttributeRequest<Value> request = new RecordUpdateRequest.AddAttributeRequest<Value>();
					request.setParentEntityId(parentEntityId);
					request.setNodeName(nodeName);
					request.setRemarks(remarks);
					request.setSymbol(symbol);
					if ( value != null ) {
						Value parsedValue = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) childDefn, value);
						request.setValue(parsedValue);
					}
					return request;
				}
			}
		case APPLY_DEFAULT_VALUE:
			{
			ApplyDefaultValueRequest request = new RecordUpdateRequest.ApplyDefaultValueRequest();
			Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			request.setAttribute(attribute);
			return request;
			}
		case APPROVE_MISSING:
			{
			ApproveMissingValueRequest request = new RecordUpdateRequest.ApproveMissingValueRequest();
			request.setParentEntityId(parentEntityId);
			request.setNodeName(nodeName);
			return request;
			}
		case CONFIRM_ERROR:
			{
			ConfirmErrorRequest request = new RecordUpdateRequest.ConfirmErrorRequest();
			Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
			request.setAttribute(attribute);
			return request;
			}
		case DELETE:
			{
			DeleteNodeRequest request = new RecordUpdateRequest.DeleteNodeRequest();
			Node<?> node = record.getNodeByInternalId(nodeId);
			request.setNode(node);
			return request;
			}
		case UPDATE:
			{
			if ( fieldIndex < 0 ) {
				UpdateAttributeRequest<Value> request = new RecordUpdateRequest.UpdateAttributeRequest<Value>();
				Attribute<?, ?> attribute = (Attribute<AttributeDefinition, Value>) record.getNodeByInternalId(nodeId);
				request.setAttribute((Attribute<?, Value>) attribute);
				if ( value != null ) {
					Value parsedValue;
					if ( attribute instanceof FileAttribute ) {
						parsedValue = parseFileAttributeValue(record, fileManager, sessionManager, nodeId, value);
					} else {
						parsedValue = parseCompositeAttributeValue(attribute.getParent(), attribute.getDefinition(), value);
					}
					request.setValue(parsedValue);
				}
				request.setSymbol(symbol);
				request.setRemarks(remarks);
				return request;
			} else {
				UpdateFieldRequest request = new RecordUpdateRequest.UpdateFieldRequest();
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
			UpdateRemarksRequest request = new RecordUpdateRequest.UpdateRemarksRequest();
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
	
	protected File parseFileAttributeValue(CollectRecord record, RecordFileManager fileManager, 
			SessionManager sessionManager, Integer nodeId, Object requestValue) {
		File result;
		SessionState sessionState = sessionManager.getSessionState();
		String sessionId = sessionState.getSessionId();
		if ( requestValue != null ) {
			if ( requestValue instanceof FileWrapper ) {
				FileWrapper fileWrapper = (FileWrapper) requestValue;
				try {
					result = fileManager.saveToTempFolder(fileWrapper.getData(), fileWrapper.getFileName(), sessionId, record, nodeId);
				} catch (RecordFileException e) {
					throw new RuntimeException("Error parsing saving temporary file", e);
				}
			} else {
				throw new IllegalArgumentException("Invalid value type: expected byte[]");
			}
		} else {
			fileManager.prepareDeleteFile(sessionId, record, nodeId);
			result = null;
		}
		return result;
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
