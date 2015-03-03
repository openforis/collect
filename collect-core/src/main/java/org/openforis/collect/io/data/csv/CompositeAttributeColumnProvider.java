/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
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
	protected int getNumberOfColumnsPerAttribute() {
		return isMergedValueSupported() ? 1: getFieldNames().length;
	}

	@Override
	protected List<String> generateSingleAttributeColumnHeadings() {
		return generateAttributeColumnHeadings(0);
	}
	
	@Override
	protected List<String> generateAttributeColumnHeadings(int attributeIdx) {
		List<String> headings = new ArrayList<String>();
		if (isMergedValueSupported()) {
			headings.add(generateMergedValueHeading(attributeIdx));
		}
		headings.addAll(generateAttributeFieldHeadings(attributeIdx));
		return headings;
	}

	protected String generateAttributePositionSuffix(int attributeIdx) {
		return attributeDefinition.isMultiple() ? "[" + (attributeIdx + 1) + "]": "";
	}

	protected List<String> generateAttributeFieldHeadings(int attributeIdx) {
		String attrPosSuffix = generateAttributePositionSuffix(attributeIdx);
		List<String> headings = new ArrayList<String>();
		for (int fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
			headings.add(generateFieldHeading(fieldNames[fieldIdx]) + attrPosSuffix);
		}
		return headings;
	}

	protected abstract String[] getFieldNames();
	
	protected String generateMergedValueHeading(int attributeIdx) {
		String attrPosSuffix = generateAttributePositionSuffix(attributeIdx);
		return attributeDefinition.getName() + attrPosSuffix;
	}

	protected String generateFieldHeading(String fieldName) {
		return attributeDefinition.getName() + getConfig().getFieldHeadingSeparator() + fieldName;
	}
	
	protected boolean isMergedValueSupported() {
		return getConfig().isIncludeCompositeAttributeMergedColumn() && (
				attributeDefinition instanceof CoordinateAttributeDefinition
				|| attributeDefinition instanceof DateAttributeDefinition
				|| attributeDefinition instanceof TimeAttributeDefinition
				);
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
