package org.openforis.collect.manager.dataimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.referencedataimport.Line;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataLine extends Line {

	private Map<AttributeDefinition, String> recordKeysByDefn;
	private Map<AttributeDefinition, String> ancestorKeysByDefn;
	private Map<FieldValueKey, String> fieldValues;
	private Map<FieldValueKey, String> columnNameByField;
	
	public DataLine() {
		recordKeysByDefn = new HashMap<AttributeDefinition, String>();
		ancestorKeysByDefn = new HashMap<AttributeDefinition, String>();
		fieldValues = new HashMap<FieldValueKey, String>();
		columnNameByField = new HashMap<FieldValueKey, String>();
	}
	
	public void setAncestorKey(AttributeDefinition keyDefn, String value) {
		if ( keyDefn.getParentEntityDefinition() == keyDefn.getRootEntity() ) {
			recordKeysByDefn.put(keyDefn, value);
		}
		ancestorKeysByDefn.put(keyDefn, value);
	}

	public void setFieldValue(int attrDefnId, String fieldName, String value) {
		fieldValues.put(new FieldValueKey(attrDefnId, fieldName), value);
	}
	
	public void setColumnNameByField(int attrDefnId, String fieldName, String colName) {
		columnNameByField.put(new FieldValueKey(attrDefnId, fieldName), colName);
	}
	
	public String[] getRecordKeyValues(EntityDefinition rootEntityDefn) {
		List<AttributeDefinition> rootKeyAttrDefns = rootEntityDefn.getKeyAttributeDefinitions();
		String[] recordKeys = new String[rootKeyAttrDefns.size()];
		for (int i = 0; i < rootKeyAttrDefns.size(); i++) {
			AttributeDefinition keyDefn = rootKeyAttrDefns.get(i);
			String key = recordKeysByDefn.get(keyDefn);
			recordKeys[i] = key;
		}
		return recordKeys;
	}
	
	public Map<AttributeDefinition, String> getRecordKeys() {
		return recordKeysByDefn;
	}
	
	public Map<AttributeDefinition, String> getAncestorKeys() {
		return ancestorKeysByDefn;
	} 
	
	public Map<FieldValueKey, String> getFieldValues() {
		return fieldValues;
	}
	
	public String getColumnName(FieldDefinition<?> fieldDefn) {
		return columnNameByField.get(fieldDefn);
	}

	public Map<FieldValueKey, String> getColumnNamesByField() {
		return columnNameByField;
	}
	
	public static class FieldValueKey {
		
		private int attributeDefinitionId;
		private String fieldName;
		
		public FieldValueKey(int attributeDefinitionId,
				String fieldName) {
			super();
			this.attributeDefinitionId = attributeDefinitionId;
			this.fieldName = fieldName;
		}

		public int getAttributeDefinitionId() {
			return attributeDefinitionId;
		}
		
		public String getFieldName() {
			return fieldName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + attributeDefinitionId;
			result = prime * result
					+ ((fieldName == null) ? 0 : fieldName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldValueKey other = (FieldValueKey) obj;
			if (attributeDefinitionId != other.attributeDefinitionId)
				return false;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

	}
	
}
