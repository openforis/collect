/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.openforis.collect.manager.RecordFileException;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordUpdateRequest.BaseAttributeRequest;
import org.openforis.collect.remoting.service.FileWrapper;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
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
public abstract class BaseAttributeRequestProxy<T extends BaseAttributeRequest<?>> extends RecordUpdateRequestProxy<T> {
	
	protected Object value;
	protected String remarks;
	protected FieldSymbol symbol;
	
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
