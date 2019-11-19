package org.openforis.collect.datacleansing;

import java.util.UUID;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQuery extends PersistedSurveyObject<Integer> {

	private static final long serialVersionUID = 1L;
	
	public enum ErrorSeverity {
		ERROR('e'), WARNING('w'), NO_ERROR('n');
		
		private char code;

		ErrorSeverity(char code) {
			this.code = code;
		}
		
		public static ErrorSeverity fromCode(String code) {
			if (code == null || code.length() > 1) {
				throw new IllegalArgumentException("Invalid code for Severity: " + code);
			}
			return fromCode(code.charAt(0));
		}
		
		public static ErrorSeverity fromCode(char code) {
			ErrorSeverity[] values = values();
			for (ErrorSeverity severity : values) {
				if (severity.code == code) {
					return severity;
				}
			}
			throw new IllegalArgumentException("Invalid code for Severity: " + code);
		}
		
		public char getCode() {
			return code;
		}
	}
	
	private String title;
	private String description;
	private int entityDefinitionId;
	private int attributeDefinitionId;
	private String conditions;
	private ErrorSeverity errorSeverity;
	private Integer typeId;
	
	private transient DataQueryType type;
		
	public DataQuery(CollectSurvey survey) {
		super(survey);
	}
	
	public DataQuery(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public EntityDefinition getEntityDefinition() {
		NodeDefinition def = getSurvey().getSchema().getDefinitionById(entityDefinitionId);
		return (EntityDefinition) def;
	}
	
	public AttributeDefinition getAttributeDefinition() {
		NodeDefinition def = getSurvey().getSchema().getDefinitionById(attributeDefinitionId);
		if (def == null) {
			throw new IllegalStateException(String.format("Definition with id %d not found in the schema", attributeDefinitionId));
		}
		return (AttributeDefinition) def;
	}
	
	public void setAttributeDefinition(AttributeDefinition def) {
		attributeDefinitionId = def.getId();
	}
	
	public void setEntityDefinition(EntityDefinition def) {
		entityDefinitionId = def.getId();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getEntityDefinitionId() {
		return entityDefinitionId;
	}

	public void setEntityDefinitionId(int entityDefinitionId) {
		this.entityDefinitionId = entityDefinitionId;
	}

	public int getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(int attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public ErrorSeverity getErrorSeverity() {
		return errorSeverity;
	}
	
	public void setErrorSeverity(ErrorSeverity severity) {
		this.errorSeverity = severity;
	}
	
	public Integer getTypeId() {
		return type == null ? typeId: type.getId();
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
		this.type = null;
	}
	
	public DataQueryType getType() {
		return type;
	}
	
	public void setType(DataQueryType type) {
		this.type = type;
		this.typeId = type == null ? null: type.getId();
	}
	
	@Override
	public boolean deepEquals(Object obj, boolean ignoreId) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj, ignoreId))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataQuery other = (DataQuery) obj;
		if (attributeDefinitionId != other.attributeDefinitionId)
			return false;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (entityDefinitionId != other.entityDefinitionId)
			return false;
		if (errorSeverity != other.errorSeverity)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.deepEquals(other.type, ignoreId))
			return false;
		if (! ignoreId) {
			if (typeId == null) {
				if (other.typeId != null)
					return false;
			} else if (!typeId.equals(other.typeId))
				return false;
		}
		return true;
	}
	
	

}