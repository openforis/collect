package org.openforis.collect.io.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.Line;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataLine extends Line {

	private Map<Integer, EntityIdentifier<?>> ancestorIdentifierByDefinitionId;
	private Map<FieldValueKey, String> fieldValues;
	private Map<FieldValueKey, String> columnNameByField;
	
	public DataLine() {
		ancestorIdentifierByDefinitionId = new HashMap<Integer, EntityIdentifier<?>>();
		fieldValues = new LinkedHashMap<FieldValueKey, String>();
		columnNameByField = new HashMap<FieldValueKey, String>();
	}
	
	public void setFieldValue(int attrDefnId, String fieldName, String value) {
		FieldValueKey key = new FieldValueKey(attrDefnId, fieldName);
		setFieldValue(key, value);
	}
	
	public void setFieldValue(FieldValueKey key, String value) {
		fieldValues.put(key, value);
	}
	
	public void setColumnNameByField(int attrDefnId, String fieldName, String colName) {
		FieldValueKey key = new FieldValueKey(attrDefnId, fieldName);
		setColumnNameByField(key, colName);
	}

	public void setColumnNameByField(FieldValueKey key, String colName) {
		columnNameByField.put(key, colName);
	}
	
	public Value[] getRecordKeyValues(EntityDefinition rootEntityDefn) {
		EntityKeysIdentifier identifier = (EntityKeysIdentifier) ancestorIdentifierByDefinitionId.get(rootEntityDefn.getId());
		return identifier.getKeyValues();
	}
	
	public void addAncestorIdentifier(EntityIdentifier<?> identifier) {
		int entityDefnId = identifier.getDefinition().getEntityDefinitionId();
		ancestorIdentifierByDefinitionId.put(entityDefnId, identifier);
	}
	
	public EntityIdentifier<?> getAncestorIdentifier(int entityDefinitionId) {
		return ancestorIdentifierByDefinitionId.get(entityDefinitionId);
	}
	
	public Map<FieldValueKey, String> getFieldValues() {
		return fieldValues;
	}
	
	public String getColumnName(FieldDefinition<?> fieldDefn) {
		return columnNameByField.get(new FieldValueKey(fieldDefn));
	}

	public Map<FieldValueKey, String> getColumnNamesByField() {
		return columnNameByField;
	}
	
	@Override
	public String toString() {
		return "DataLine [ancestorIdentifierByDefinitionId=" + ancestorIdentifierByDefinitionId + ", fieldValues="
				+ fieldValues + ", columnNameByField=" + columnNameByField + "]";
	}
	
	public static class FieldValueKey {
		
		private int attributeDefinitionId;
		private int attributePosition;
		private String fieldName;
		
		public FieldValueKey(FieldDefinition<?> fieldDefn) {
			this(fieldDefn.getAttributeDefinition(), fieldDefn.getName());
		}

		public FieldValueKey(AttributeDefinition defn) {
			this(defn, defn.getMainFieldName());
		}

		public FieldValueKey(AttributeDefinition defn, String fieldName) {
			this(defn.getId(), 1, fieldName);
		}
		
		public FieldValueKey(int attributeDefinitionId, 
				String fieldName) {
			this(attributeDefinitionId, 1, fieldName);
		}
		
		public FieldValueKey(int attributeDefinitionId, 
				int attributePosition, String fieldName) {
			super();
			this.attributeDefinitionId = attributeDefinitionId;
			this.attributePosition = attributePosition;
			this.fieldName = fieldName;
		}

		public int getAttributeDefinitionId() {
			return attributeDefinitionId;
		}
		
		public int getAttributePosition() {
			return attributePosition;
		}
		
		public String getFieldName() {
			return fieldName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + attributeDefinitionId;
			result = prime * result + attributePosition;
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
			if (attributePosition != other.attributePosition)
				return false;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}
		
		
		@Override
		public String toString() {
			return "FieldValueKey [defId=" + attributeDefinitionId + ", pos="
					+ attributePosition + ", field=" + fieldName + "]";
		}
		
	}
	
	public static class EntityIdentifierDefinition {
		
		private int entityDefinitionId;

		public EntityIdentifierDefinition(int entityDefinitionId) {
			super();
			this.entityDefinitionId = entityDefinitionId;
		}
		
		public int getEntityDefinitionId() {
			return entityDefinitionId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + entityDefinitionId;
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
			EntityIdentifierDefinition other = (EntityIdentifierDefinition) obj;
			if (entityDefinitionId != other.entityDefinitionId)
				return false;
			return true;
		}

	}
	
	public static class EntityPositionIdentifierDefinition extends EntityIdentifierDefinition {

		public EntityPositionIdentifierDefinition(int entityDefinitionId) {
			super(entityDefinitionId);
		}
		
	}
	
	public static class SingleEntityIdentifierDefinition extends EntityIdentifierDefinition {

		public SingleEntityIdentifierDefinition(int entityDefinitionId) {
			super(entityDefinitionId);
		}
		
	}
	
	public static class EntityKeysIdentifierDefintion extends EntityIdentifierDefinition {
		
		private int[] keyDefinitionIds;

		public EntityKeysIdentifierDefintion(int entityDefinitionId,
				int[] keyDefinitionIds) {
			super(entityDefinitionId);
			this.keyDefinitionIds = keyDefinitionIds;
		}
		
		public EntityKeysIdentifierDefintion(EntityDefinition entityDefn) {
			super(entityDefn.getId());
			List<AttributeDefinition> keyDefns = entityDefn.getKeyAttributeDefinitions();
			int[] keyDefinitionIds = new int[keyDefns.size()];
			for (int i = 0; i < keyDefns.size(); i++) {
				AttributeDefinition k = keyDefns.get(i);
				keyDefinitionIds[i] = k.getId();
			}
			this.keyDefinitionIds = keyDefinitionIds;
		}
		
		public int[] getKeyDefinitionIds() {
			return keyDefinitionIds;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Arrays.hashCode(keyDefinitionIds);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityKeysIdentifierDefintion other = (EntityKeysIdentifierDefintion) obj;
			if (!Arrays.equals(keyDefinitionIds, other.keyDefinitionIds))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "EntityKeysIdentifierDefintion [keyDefs=" + Arrays.toString(keyDefinitionIds) + "]";
		}
	}
	
	
	
	public static class EntityIdentifier<T extends EntityIdentifierDefinition> {

		private T definition;

		public EntityIdentifier(T definition) {
			super();
			this.definition = definition;
		}

		public T getDefinition() {
			return definition;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((definition == null) ? 0 : definition.hashCode());
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
			EntityIdentifier<?> other = (EntityIdentifier<?>) obj;
			if (definition == null) {
				if (other.definition != null)
					return false;
			} else if (!definition.equals(other.definition))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return definition.toString();
		}
	}
	
	public static class EntityKeysIdentifier extends EntityIdentifier<EntityKeysIdentifierDefintion> {
		
		private Map<Integer, Value> keyByDefinitionId;

		public EntityKeysIdentifier(EntityKeysIdentifierDefintion definition) {
			super(definition);
			keyByDefinitionId = new HashMap<Integer, Value>();
		}

		public void addKeyValue(int keyDefnId, Value value) {
			keyByDefinitionId.put(keyDefnId, value);
		}
		
		public Value getKeyValue(int keyDefnId) {
			return keyByDefinitionId.get(keyDefnId);
		}
		
		public Value[] getKeyValues() {
			int[] definitionIds = getDefinition().getKeyDefinitionIds();
			Value[] result = new Value[definitionIds.length];
			for (int i = 0; i < definitionIds.length; i++) {
				int id = definitionIds[i];
				Value key = keyByDefinitionId.get(id);
				result[i] = key;
			}
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime
					* result
					+ ((keyByDefinitionId == null) ? 0 : keyByDefinitionId
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityKeysIdentifier other = (EntityKeysIdentifier) obj;
			if (keyByDefinitionId == null) {
				if (other.keyByDefinitionId != null)
					return false;
			} else if (!keyByDefinitionId.equals(other.keyByDefinitionId))
				return false;
			return true;
		}

	}
	
	public static class EntityPositionIdentifier extends EntityIdentifier<EntityPositionIdentifierDefinition> {
		
		private int position;

		public EntityPositionIdentifier(
				EntityPositionIdentifierDefinition definition, int position) {
			super(definition);
			this.position = position;
		}

		public int getPosition() {
			return position;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + position;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityPositionIdentifier other = (EntityPositionIdentifier) obj;
			if (position != other.position)
				return false;
			return true;
		}
		
	}
}
