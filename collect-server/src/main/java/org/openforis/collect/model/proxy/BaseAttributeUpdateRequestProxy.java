/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
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
	
	protected Code parseCode(CodeListManager codeListManager, Entity parent, CodeAttributeDefinition def, String value) {
		CollectRecord record = (CollectRecord) parent.getRecord();
		List<CodeListItem> items = codeListManager.getAssignableCodeListItems(parent, def);
		Code code = parseCode(codeListManager, record, value, items);
		return code;
	}
	
	protected Code parseCode(CodeListManager codeListManager, CollectRecord record, 
			String value, List<CodeListItem> codeList) {
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
		CodeListItem codeListItem = codeListManager.findCodeListItem(codeList, codeStr);
		if(codeListItem != null) {
			code = new Code(codeListItem.getCode(), qualifier);
		}
		if (code == null) {
			code = new Code(codeStr, qualifier);
		}
		return code;
	}

	protected Object parseFieldValue(Field<?> field, String value, Integer fieldIndex) {
		Object fieldValue = null;
		if(StringUtils.isBlank(value)) {
			return null;
		}
		Attribute<?, ?> attribute = field.getAttribute();
		AttributeDefinition defn = attribute.getDefinition();
		if(defn instanceof BooleanAttributeDefinition) {
			fieldValue = Boolean.parseBoolean(value);
		} else if(defn instanceof CoordinateAttributeDefinition) {
			if(fieldIndex != null) {
				if(fieldIndex == 2) {
					fieldValue = value;
				} else {
					fieldValue = Double.valueOf(value);
				}
			}
		} else if(defn instanceof DateAttributeDefinition) {
			Integer val = Integer.valueOf(value);
			fieldValue = val;
		} else if(defn instanceof NumberAttributeDefinition) {
			NumericAttributeDefinition numberDef = (NumericAttributeDefinition) defn;
			if(fieldIndex != null && fieldIndex == 2) {
				//unit id
				fieldValue = Integer.parseInt(value);
			} else {
				NumericAttributeDefinition.Type type = numberDef.getType();
				Number number = null;
				switch(type) {
					case INTEGER:
						number = Integer.valueOf(value);
						break;
					case REAL:
						number = Double.valueOf(value);
						break;
				}
				if(number != null) {
					fieldValue = number;
				}
			}
		} else if(defn instanceof RangeAttributeDefinition) {
			if(fieldIndex != null && fieldIndex == 3) {
				//unit id
				fieldValue = Integer.parseInt(value);
			} else {
				RangeAttributeDefinition.Type type = ((RangeAttributeDefinition) defn).getType();
				Number number = null;
				switch(type) {
					case INTEGER:
						number = Integer.valueOf(value);
						break;
					case REAL:
						number = Double.valueOf(value);
						break;
				}
				if(number != null) {
					fieldValue = number;
				}
			}
		} else if(defn instanceof TimeAttributeDefinition) {
			fieldValue = Integer.valueOf(value);
		} else {
			fieldValue = value;
		}
		return fieldValue;
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
