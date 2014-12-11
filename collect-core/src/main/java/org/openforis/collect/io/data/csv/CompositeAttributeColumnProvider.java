/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 */
public abstract class CompositeAttributeColumnProvider<T extends AttributeDefinition> implements ColumnProvider {

	protected static final String FIELD_SEPARATOR = "_";
	private static final String MULTIPLE_ATTRIBUTE_VALUE_SEPARATOR = ", ";

	protected T defn;
	private String[] fieldNames;

	public CompositeAttributeColumnProvider(T defn) {
		this.defn = defn;
		this.fieldNames = getFieldNames();
	}

	@Override
	public List<String> getColumnHeadings() {
		String[] fields = getFieldNames();
		String[] headings = new String[fields.length];
		
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			headings[i] = getFieldHeading(field);
		}
		return Arrays.asList(headings);
	}

	protected abstract String[] getFieldNames();
	
	protected String getFieldHeading(String fieldName) {
		return defn.getName() + FIELD_SEPARATOR + fieldName;
	}
	
	@Override
	public List<String> extractValues(Node<?> axis) {
		checkValidAxis(axis);
		List<Node<?>> attributes = ((Entity) axis).getAll(defn);
		if (attributes.isEmpty()) {
			List<String> emptyValues = new ArrayList<String>(fieldNames.length);
			for (int i = 0; i < fieldNames.length; i++) {
				emptyValues.add("");
			}
			return emptyValues;
		} else {
			String[] fields = fieldNames;
			List<String> values = new ArrayList<String>(fields.length);
			for (String fieldName : fields) {
				values.add(extractMergedValue(attributes, fieldName));
			}
			return values;
		}
	}

	protected String extractMergedValue(List<Node<?>> attributes, String fieldName) {
		StringBuffer sb = new StringBuffer();
		for (Node<?> node : attributes) {
			String val = extractValue((Attribute<?, ?>) node, fieldName);
			if ( StringUtils.isNotBlank(val) ) {
				if ( sb.length() > 0 ) {
					sb.append(MULTIPLE_ATTRIBUTE_VALUE_SEPARATOR);
				}
				sb.append(val);
			}				
		}
		return sb.toString();
	}

	protected void checkValidAxis(Node<?> axis) {
		if (axis == null) {
			throw new NullPointerException("Axis must be non-null");
		} else if ( ! (axis instanceof Entity) ) {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}
	
	protected String extractValue(Field<?> field) {
		Object value = field.getValue();
		return value == null ? "" : StringUtils.trimToEmpty(value.toString());
	}

	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		Field<?> field = attr.getField(fieldName);
		return extractValue(field);
	}
	
	protected String getAttributeName() {
		return defn.getName();
	}
	
}
