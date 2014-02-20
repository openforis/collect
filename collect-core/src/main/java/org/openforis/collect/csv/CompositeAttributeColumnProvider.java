/**
 * 
 */
package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public abstract class CompositeAttributeColumnProvider<T extends AttributeDefinition> implements ColumnProvider {

	protected static final String FIELD_SEPARATOR = "_";

	protected T defn;

	public CompositeAttributeColumnProvider(T defn) {
		this.defn = defn;
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
		if (axis == null) {
			throw new NullPointerException("Axis must be non-null");
		} else if (axis instanceof Entity) {
			Attribute<?, ?> attr = (Attribute<?, ?>) ((Entity) axis).get(defn.getName(), 0);
			if (attr == null) {
				List<String> emptyValues = new ArrayList<String>(getFieldNames().length);
				for (int i = 0; i < getFieldNames().length; i++) {
					emptyValues.add("");
				}
				return emptyValues;
			} else {
				String[] fields = getFieldNames();
				List<String> values = new ArrayList<String>(fields.length);
				for (String fieldName : fields) {
					values.add(extractValue(attr, fieldName));
				}
				return values;
			}
		} else {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}

	protected String extractValue(Field<?> field) {
		Object value = field.getValue();
		return value == null ? "" : value.toString();
	}

	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		Field<?> field = attr.getField(fieldName);
		return extractValue(field);
	}
	
	protected String getAttributeName() {
		return defn.getName();
	}
	
}
