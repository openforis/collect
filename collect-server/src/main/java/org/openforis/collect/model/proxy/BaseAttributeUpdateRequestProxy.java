/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileException;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.remoting.service.FileWrapper;
import org.openforis.collect.remoting.service.NodeUpdateRequest.BaseAttributeUpdateRequest;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.File;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseAttributeUpdateRequestProxy<T extends BaseAttributeUpdateRequest<?>> extends NodeUpdateRequestProxy<T> {
	
	private static final String QUALIFIER_SEPARATOR = ":";
	
	protected Object value;
	protected String remarks;
	protected FieldSymbol symbol;
	
	@Override
	public T toNodeUpdateRequest(CollectRecord record) {
		throw new UnsupportedOperationException();
	}
	
	public abstract T toAttributeUpdateRequest(CodeListManager codeListManager, RecordFileManager fileManager, 
			SessionManager sessionManager, CollectRecord record);
	
	protected File parseFileAttributeValue(RecordFileManager fileManager, CollectRecord record,
			SessionManager sessionManager, Integer nodeId, Object value) {
		File result;
		SessionState sessionState = sessionManager.getSessionState();
		String sessionId = sessionState.getSessionId();
		if ( value != null ) {
			if ( value instanceof FileWrapper ) {
				FileWrapper fileWrapper = (FileWrapper) value;
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
	
	protected Value parseCompositeAttributeValue(CodeListManager codeListManager,
			Entity parentEntity, String attributeName, Object value) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		AttributeDefinition defn = (AttributeDefinition) parentEntityDefn.getChildDefinition(attributeName);
		if(defn instanceof CodeAttributeDefinition) {
			if ( value instanceof String) {
				String stringVal = (String) value;
				Value result = parseCode(codeListManager, parentEntity, (CodeAttributeDefinition) defn, stringVal);
				return result;
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
				return range;
			} else {
				throw new IllegalArgumentException("Invalid value type: expected String");
			}
		} else {
			throw new IllegalArgumentException("Invalid AttributeDefinition: expected CodeAttributeDefinition or RangeAttributeDefinition");
		}
	}
	
	protected Code parseCode(CodeListManager codeListManager, Entity parent, CodeAttributeDefinition defn, String value) {
		Code code = parseCode(value);
		if ( code == null ) {
			return null;
		} else {
			String normalizedCode = findNormalizedCode(codeListManager, parent, defn, code.getCode());
			return new Code(normalizedCode, code.getQualifier());
		}
	}
	
	protected String findNormalizedCode(CodeListManager codeListManager, Entity parent,
			CodeAttributeDefinition defn, String code) {
		CodeListItem codeListItem = codeListManager.findValidItem(parent, defn, code);
		if(codeListItem == null) {
			return code;
		} else {
			return codeListItem.getCode();
		}
	}

	protected Code parseCode(String value) {
		Code code = null;
		String[] strings = value.split(QUALIFIER_SEPARATOR);
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
		if ( codeStr != null ) {
			code = new Code(codeStr, qualifier);
		}
		return code;
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
