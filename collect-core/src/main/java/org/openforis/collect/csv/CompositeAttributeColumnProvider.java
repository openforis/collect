/**
 * 
 */
package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public abstract class CompositeAttributeColumnProvider implements ColumnProvider {

	private String attributeName;
	private List<String> columnHeadings;

	public CompositeAttributeColumnProvider(String attributeName) {
		this.attributeName = attributeName;
		this.columnHeadings = Arrays.asList(getFieldsHeadings());
	}

	protected abstract String[] getFieldsHeadings();

	protected abstract Field<?>[] getFieldsToExtract(Attribute<?, ?> attr);

	@Override
	public List<String> getColumnHeadings() {
		return columnHeadings;
	}

	@Override
	public List<String> extractValues(Node<?> axis) {
		if (axis == null) {
			throw new NullPointerException("Axis must be non-null");
		} else if (axis instanceof Entity) {
			Attribute<?, ?> attr = (Attribute<?, ?>) ((Entity) axis).get(attributeName, 0);
			if (attr == null) {
				int size = columnHeadings.size();
				List<String> emptyValues = new ArrayList<String>();
				for (int i = 0; i < size; i++) {
					emptyValues.add("");
				}
				return emptyValues;
			} else {
				Field<?>[] fields = getFieldsToExtract(attr);
				List<String> values = new ArrayList<String>(fields.length);
				for (Field<?> field : fields) {
					values.add(getFieldValue(field));
				}
				return values;
			}
		} else {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}

	private String getFieldValue(Field<?> field) {
		Object value = field.getValue();
		return value == null ? "" : value.toString();
	}
	
	protected String getAttributeName() {
		return attributeName;
	}

}
