/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class CompositeAttributeColumnProvider<T extends AttributeDefinition> extends BasicAttributeColumnProvider<T> {

	private String[] fieldNames;

	public CompositeAttributeColumnProvider(CSVExportConfiguration config, T defn) {
		super(config, defn);
		init();
	}

	protected void init() {
		this.fieldNames = getFieldNames();
	}

	@Override
	public List<String> generateColumnHeadings() {
		int maxAttrValues = getMaxAttributeValues();
		int fieldsSize = isMergedValueSupported() ? 1: getFieldNames().length;
		List<String> headings = new ArrayList<String>(fieldsSize * maxAttrValues);
		if (attributeDefinition.isMultiple()) {
			for (int attrIdx = 0; attrIdx < maxAttrValues; attrIdx++) {
				headings.addAll(getAttributeHeadings(attrIdx));
			}
		} else {
			headings.addAll(getSingleAttributeHeadings());
		}
		return headings;
	}

	private List<String> getSingleAttributeHeadings() {
		return getAttributeHeadings(0);
	}
	
	private List<String> getAttributeHeadings(int attributeIdx) {
		List<String> headings = new ArrayList<String>();
		String attrPosSuffix = attributeDefinition.isMultiple() ? "[" + (attributeIdx + 1) + "]": "";
		if (isMergedValueSupported()) {
			headings.add(getMergedValueHeading() + attrPosSuffix);
		}
		for (int fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
			headings.add(getFieldHeading(fieldNames[fieldIdx]) + attrPosSuffix);
		}
		return headings;
	}

	protected abstract String[] getFieldNames();
	
	protected String getMergedValueHeading() {
		return attributeDefinition.getName();
	}

	protected String getFieldHeading(String fieldName) {
		return attributeDefinition.getName() + getConfig().getFieldHeadingSeparator() + fieldName;
	}
	
	protected boolean isMergedValueSupported() {
		return attributeDefinition instanceof BooleanAttributeDefinition 
				|| attributeDefinition instanceof CodeAttributeDefinition
				|| attributeDefinition instanceof CoordinateAttributeDefinition
				|| attributeDefinition instanceof DateAttributeDefinition
				|| attributeDefinition instanceof TextAttributeDefinition
				|| attributeDefinition instanceof TimeAttributeDefinition;
	}
	
	protected String extractMergedValue(Attribute<?, ?> attr) {
		if (attr == null) {
			return "";
		}
		CSVValueFormatter valueFormatter = new CSVValueFormatter();
		String value = valueFormatter.format(attributeDefinition, attr.getValue());
		return value;
	}
	
	@Override
	public List<String> extractValues(Node<?> axis) {
		checkValidAxis(axis);
		List<Node<?>> attributes = ((Entity) axis).getAll(attributeDefinition);
		int maxAttributeValues = getMaxAttributeValues();
		int totHeadings = fieldNames.length * maxAttributeValues;
		List<String> values = new ArrayList<String>(totHeadings);
		if (! attributes.isEmpty()) {
			for (int attrIdx = 0; attrIdx < maxAttributeValues; attrIdx ++) {
				Attribute<?, ?> attr = attrIdx < attributes.size() ? (Attribute<?, ?>) attributes.get(attrIdx): null;
				if (isMergedValueSupported()) {
					String val = extractMergedValue(attr);
					values.add(val);
				}
				for (String fieldName : fieldNames) {
					String val;
					if (attr == null) {
						val = "";
					} else {
						val = extractValue(attr, fieldName);
					}
					values.add(val);
				}
			}
		}
		return values;
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
	
}
