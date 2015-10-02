/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public abstract class AttributeDefinition extends NodeDefinition implements Calculable, KeyAttributeDefinition {
	
	private static final long serialVersionUID = 1L;

	protected boolean key;
	private List<Check<?>> checks;
	private List<AttributeDefault> attributeDefaults;
	private boolean calculated;
	/**
	 * Custom field labels
	 */
	private FieldLabelMap fieldLabels;
	
	AttributeDefinition(Survey survey, int id) {
		super(survey, id);
		this.calculated = false;
	}

	@Override
	public boolean isCalculated() {
		return calculated;
	}
	
	public void setCalculated(boolean calculated) {
		this.calculated = calculated;
	}
	
	public List<Check<?>> getChecks() {
		return CollectionUtils.unmodifiableList(this.checks);
	}
	
	public void addCheck(Check<?> check) {
		if ( checks == null ) {
			checks = new ArrayList<Check<?>>();
		}
		checks.add(check);
	}
	
	public void removeAllChecks() {
		if ( checks != null ) {
			checks.clear();
		}
	}
	
	public void removeCheck(Check<?> check) {
		checks.remove(check);
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return CollectionUtils.unmodifiableList(this.attributeDefaults);
	}

	public void addAttributeDefault(AttributeDefault def) {
		if ( attributeDefaults == null ) {
			attributeDefaults = new ArrayList<AttributeDefault>();
		}
		attributeDefaults.add(def);
	}
	
	public void removeAllAttributeDefaults() {
		if ( attributeDefaults != null ) {
			attributeDefaults.clear();
		}
	}
	public void removeAttributeDefault(AttributeDefault def) {
		attributeDefaults.remove(def);
	}
	
	public void moveAttributeDefault(AttributeDefault def, int toIndex) {
		CollectionUtils.shiftItem(attributeDefaults, def, toIndex);
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public abstract <V extends Value> V createValue(String string);

//	public Set<NodePathPointer> getCheckDependencyPaths() {
//		Survey survey = getSurvey();
//		return survey.getCheckDependencies(this);
//	}
	
	protected abstract FieldDefinitionMap getFieldDefinitionMap();
	
	public List<String> getFieldNames() {
		return getFieldDefinitionMap().getFieldNames();
	}
	
	public List<FieldDefinition<?>> getFieldDefinitions() {
		return getFieldDefinitionMap().listValues();
	}
	
	public FieldDefinition<?> getFieldDefinition(String name) {
		return getFieldDefinitionMap().get(name);
	}
	
	public FieldDefinition<?> getMainFieldDefinition() {
		return getFieldDefinition(getMainFieldName());
	}

	public List<FieldLabel> getFieldLabels() {
		if ( this.fieldLabels == null ) {
			return Collections.emptyList();
		} else {
			return fieldLabels.values();
		}
	}
	
	public String getFieldLabel(String field) {
		String defaultLanguage = getSurvey().getDefaultLanguage();
		return getFieldLabel(field, defaultLanguage);
	}
	
	public String getFieldLabel(String field, String language) {
		return fieldLabels == null ? null: fieldLabels.getText(field, language);
	}
	
	public void setFieldLabel(String field, String language, String text) {
		if ( fieldLabels == null ) {
			fieldLabels = new FieldLabelMap();
		}
		fieldLabels.setText(field, language, text);
	}

	public void addFieldLabel(FieldLabel label) {
		if ( fieldLabels == null ) {
			fieldLabels = new FieldLabelMap();
		}
		fieldLabels.add(label);
	}

	public void removeFieldLabel(String field, String language) {
		if (fieldLabels != null ) {
			fieldLabels.remove(field, language);
		}
	}
	
	public abstract boolean hasMainField();
	
	public abstract String getMainFieldName();
	
	public abstract Class<? extends Value> getValueType();

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeDefinition other = (AttributeDefinition) obj;
		if (key != other.key)
			return false;
		if (attributeDefaults == null) {
			if (other.attributeDefaults != null)
				return false;
		} else if (! CollectionUtils.deepEquals(attributeDefaults, other.attributeDefaults))
			return false;
		if (checks == null) {
			if (other.checks != null)
				return false;
		} else if (! CollectionUtils.deepEquals(checks, other.checks))
			return false;
		return true;
	}

	/**
	 * Build name prefixing all names of ancestor single entities
	 * @return
	 */
	public String getCompoundName() {
		StringBuilder sb = new StringBuilder(getName());
		NodeDefinition ancestor = getParentDefinition();
		while ( ancestor != null && !ancestor.isMultiple() ) {
			sb.insert(0, "_");
			sb.insert(0, ancestor.getName());
			ancestor = ancestor.getParentDefinition();
		}
		return sb.toString();
	}
	
	public static class FieldLabel extends TypedLanguageSpecificText<String> {

		private static final long serialVersionUID = 1L;

		public FieldLabel(String field, String language, String text) {
			super(field, language, text);
		}

	}
	
	public static class FieldLabelMap extends TypedLanguageSpecificTextAbstractMap<FieldLabel, String> {
		private static final long serialVersionUID = 1L;
	}
	
	static class FieldDefinitionMap extends LinkedHashMap<String, FieldDefinition<?>> {
		
		private static final long serialVersionUID = 1L;

		public FieldDefinitionMap(FieldDefinition<?>... fieldDefs) {
			for (FieldDefinition<?> def : fieldDefs) {
				super.put(def.getName(), def);
			}
		}
		
		public List<String> getFieldNames() {
			return Collections.unmodifiableList(new ArrayList<String>(keySet()));
		}

		public List<FieldDefinition<?>> listValues() {
			return Collections.unmodifiableList(new ArrayList<FieldDefinition<?>>(values()));
		}
		
		@Override
		public FieldDefinition<?> put(String key, FieldDefinition<?> value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public FieldDefinition<?> remove(Object key) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		
	}
}
