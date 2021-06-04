/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.lang.Objects;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public abstract class AttributeDefinition extends NodeDefinition implements Calculable {
	
	private static final long serialVersionUID = 1L;

	protected boolean key;
	private List<Check<?>> checks;
	private List<AttributeDefault> attributeDefaults;
	private boolean calculated;
	private Integer referencedAttributeId;
	private AttributeDefinition referencedAttribute;
	/**
	 * Custom field labels
	 */
	private FieldLabelMap fieldLabels;
	
	AttributeDefinition(Survey survey, int id) {
		super(survey, id);
		this.calculated = false;
	}

	AttributeDefinition(Survey survey, AttributeDefinition source, int id) {
		super(survey, source, id);
		this.key = source.key;
		this.calculated = source.calculated;
		this.checks = Objects.clone(source.checks);
		this.attributeDefaults = Objects.clone(source.attributeDefaults);
		if (survey == source.getSurvey()) {
			this.setReferencedAttribute(source.getReferencedAttribute());
		}
	}
	
	@Override
	protected void init() {
		super.init();
		if (referencedAttributeId != null) {
			referencedAttribute = (AttributeDefinition) getSchema().getDefinitionById(referencedAttributeId);
		}
	}
	
	@Override
	void detach() {
		clearReferenceFromAttributes();
		super.detach();
	}
	
	public Set<AttributeDefinition> getReferencingAttributes() {
		final Set<AttributeDefinition> result = new HashSet<AttributeDefinition>();
		getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				if (def instanceof AttributeDefinition 
						&& ((AttributeDefinition) def).getReferencedAttribute() == AttributeDefinition.this) {
					result.add((AttributeDefinition) def);
				}
			}
		});
		return result;
	}
	
	public Set<AttributeDefinition> clearReferenceFromAttributes() {
		Set<AttributeDefinition> referencingAttributes = getReferencingAttributes();
		for (AttributeDefinition referencingAttribute : referencingAttributes) {
			referencingAttribute.clearReferencedAttribute();
		}
		return referencingAttributes;
	}
	
	public boolean isSingleFieldKeyAttribute() {
		return false;
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
	
	public void setAttributeDefaults(List<AttributeDefault> attributeDefaults) {
		if (attributeDefaults == null) {
			this.attributeDefaults = null;
		} else {
			this.attributeDefaults = new ArrayList<AttributeDefault>(attributeDefaults);
		}
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

	public abstract <V extends Value> V createValue(Object val);
	
	public abstract <V extends Value> V createValue(String string);
	
	public abstract <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues);

//	public Set<NodePathPointer> getCheckDependencyPaths() {
//		Survey survey = getSurvey();
//		return survey.getCheckDependencies(this);
//	}
	
	protected abstract FieldDefinitionMap getFieldDefinitionMap();
	
	public List<String> getFieldNames() {
		return getFieldDefinitionMap().getFieldNames();
	}
	
	public List<FieldDefinition<?>> getFieldDefinitions() {
		return getFieldDefinitionMap().getFieldDefinitions();
	}
	
	public FieldDefinition<?> getFieldDefinition(String name) {
		return getFieldDefinitionMap().get(name);
	}
	
	public FieldDefinition<?> getMainFieldDefinition() {
		return getFieldDefinition(getMainFieldName());
	}
	
	public FieldDefinition<?> findFieldDefinition(String name) {
		FieldDefinition<?> fieldDef = getFieldDefinition(name);
		if (fieldDef == null) {
			//try to replace upper camel case to lower underscore case 
			String newFieldName = name.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
			fieldDef = getFieldDefinition(newFieldName);
		}
		return fieldDef;
	}
	
	/**
	 * Returns the name of the fields that will be used to generate the value of a "key" attribute
	 */
	public List<String> getKeyFieldNames() {
		if (hasMainField()) {
			return Arrays.asList(getMainFieldName());
		} else {
			return getFieldNames();
		}
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
	
	public boolean hasField(String fieldName) {
		return getFieldDefinitionMap().containsKey(fieldName);
	}
	
	public void setReferencedAttributeId(Integer referencedAttributeId) {
		this.referencedAttributeId = referencedAttributeId;
	}
	
	public AttributeDefinition getReferencedAttribute() {
		return referencedAttribute;
	}
	
	public void setReferencedAttribute(AttributeDefinition referencedAttribute) {
		this.referencedAttribute = referencedAttribute;
		this.referencedAttributeId = referencedAttribute == null ? null : referencedAttribute.getId();
	}
	
	public void clearReferencedAttribute() {
		setReferencedAttribute(null);
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
	
	static class FieldDefinitionMap implements Map<String, FieldDefinition<?>> {
		
		private final Map<String, FieldDefinition<?>> internalMap;
		private final List<String> fieldNames;
		private final List<FieldDefinition<?>> fieldDefinitions;
		
		FieldDefinitionMap(FieldDefinition<?>... fieldDefs) {
			this.fieldDefinitions = Arrays.asList(fieldDefs);
			this.fieldNames = new ArrayList<String>(fieldDefs.length);
			this.internalMap = new LinkedHashMap<String, FieldDefinition<?>>(fieldDefs.length);
			for (FieldDefinition<?> def : fieldDefs) {
				this.fieldNames.add(def.getName());
				this.internalMap.put(def.getName(), def);
			}
		}
		
		public List<String> getFieldNames() {
			return fieldNames;
		}

		public List<FieldDefinition<?>> getFieldDefinitions() {
			return fieldDefinitions;
		}
		
		public int size() {
			return internalMap.size();
		}

		public boolean isEmpty() {
			return internalMap.isEmpty();
		}

		public boolean containsKey(Object key) {
			return internalMap.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return internalMap.containsValue(value);
		}

		public FieldDefinition<?> get(Object key) {
			return internalMap.get(key);
		}

		public Set<String> keySet() {
			return internalMap.keySet();
		}

		public Collection<FieldDefinition<?>> values() {
			return internalMap.values();
		}

		public Set<java.util.Map.Entry<String, FieldDefinition<?>>> entrySet() {
			return internalMap.entrySet();
		}

		@Override
		public FieldDefinition<?> put(String key, FieldDefinition<?> value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends String, ? extends FieldDefinition<?>> m) {
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
