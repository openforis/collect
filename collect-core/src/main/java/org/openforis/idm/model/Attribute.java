/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.validation.ValidationResults;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public abstract class Attribute<D extends AttributeDefinition, V extends Value> extends Node<D> implements Comparable<Attribute<?, ?>> {

	private static final long serialVersionUID = 1L;

	private Field<?>[] fields;
	private List<Field<?>> fieldList;
	private Map<String, Field<?>> fieldByName;
	private transient ValidationResults validationResults;
	//summary info
	private transient boolean empty;
	private transient boolean hasData;

	private boolean allFieldsFilled;
	
	protected Attribute(D definition) {
		super(definition);
		initFields();
		empty = true;
		hasData = false;
		allFieldsFilled = false;
	}

	public void clearFieldSymbols() {
		for ( Field<?> field : fields ) {
			field.setSymbol( null );
		}
	} 
	
	public void clearFieldStates() {
		for ( Field<?> field : fields ) {
			field.getState().set(0);
		}
	}
	
	private void initFields() {
		List<FieldDefinition<?>> fieldsDefinitions = definition.getFieldDefinitions();
		this.fields = new Field<?>[fieldsDefinitions.size()];
		this.fieldByName = new LinkedHashMap<String, Field<?>>(fieldsDefinitions.size());
		for (int i = 0; i < fieldsDefinitions.size(); i++) {
			FieldDefinition<?> fieldDefn = fieldsDefinitions.get(i);
			Field<?> field = (Field<?>) fieldDefn.createNode();
			field.setAttribute(this);
			field.index = i;
			this.fields[i] = field;
			this.fieldByName.put(fieldDefn.getName(), field);
		}
		fieldList = Collections.unmodifiableList(Arrays.asList(fields));
	}
	
	public Field<?> getField(int idx) {
		return fields[idx];
	}
	
	/**
	 * @param name
	 * @return the field requested, or null if field name is invalid
	 */
	public Field<?> getField(String name) {
		return fieldByName.get(name);
	}

	public List<Field<?>> getFields() {
		return fieldList;
	}

	public int getFieldCount() {
		return fields.length;
	}
	
	public void clearValue() {
		clearFieldValues();
	}
	
	protected void clearFieldValues() {
		for (Field<?> field : fields) {
			field.setValue(null);
		}
	}

	/**
	 * Reset all properties of all attributeFields (remarks, value, symbol)
	 */
	public void clearFields() {
		for (Field<?> field : fields) {
			field.clear();
		}
	}
	
	/**
	 * @return a non-null, immutable value
	 */
	public abstract V getValue();

	/**
	 * @param value a non-null, immutable value to set
	 */
	public void setValue(V value) {
		if ( value == null ) {
			clearValue();
		} else {
			setValueInFields(value);
		}
	}
	
	protected abstract void setValueInFields(V value);
	
	/**
	 * @return true if value is empty (there is not a field with value specified)
	 */
	@Override
	public boolean isEmpty() {
		return empty;
	}
	
	protected boolean calculateIsEmpty() {
		for (Field<?> field : fields) {
			if ( field.hasValue() ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return true if all fields are empty and 
	 *  no remarks or symbol are specified
	 */
	@Override
	public boolean hasData() {
		return hasData;
	}
	
	protected boolean calculateHasData() {
		for (Field<?> field : fields) {
			if ( field.hasData() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if all fields have value specified
	 * @return
	 */
	public boolean isFilled() {
		return allFieldsFilled;
	}

	protected boolean calculateAllFieldsFilled() {
		for ( Field<?> field : fields ) {
			if ( ! field.hasValue() ) {
				return false;
			}
		}
		return true;
	}
	
	public ValidationResults getValidationResults() {
		return validationResults;
	}
	
	public void setValidationResults(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}
	
	public void updateSummaryInfo() {
		empty = calculateIsEmpty();
		hasData = calculateHasData();
		allFieldsFilled = empty ? false: calculateAllFieldsFilled();
	}

	public abstract String extractTextValue();
	
	@Override
	protected void write(StringWriter sw, int indent) {
		for (int i = 0; i < indent; i++) {
			sw.append('\t');
		}
		if ( indent == 0 ) {
			sw.append(getPath());
		} else {
			sw.append(getName());
			if ( this.getDefinition().isMultiple() ) {
				sw.append("[");
				sw.append(String.valueOf(getIndex() + 1));
				sw.append("]");
			}
		}
		sw.append(" (");
		List<Field<?>> fields = getFields();
		if ( fields.size() == 1 ) {
			Object value = fields.get(0).getValue();
			sw.append(value == null ? null: value.toString());
		} else {
			for (int i = 0; i < fields.size(); i++) {
				Field<?> field = fields.get(i);
				sw.append(field.getName());
				sw.append(": ");
				Object value = field.getValue();
				sw.append(value == null ? null: value.toString());
				if ( i < fields.size() - 1) {
					sw.append("\t");
				}
			}
		}
		sw.append(")");
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return Arrays.deepEquals(fields, ((Attribute<?,?>) obj).fields);
	}

	@Override
	public int compareTo(Attribute<?, ?> o) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		for (int i = 0; i < getFieldCount(); i++) {
			compareToBuilder.append(getField(i), o.getField(i));
		}
		return compareToBuilder.toComparison();
	}
}
