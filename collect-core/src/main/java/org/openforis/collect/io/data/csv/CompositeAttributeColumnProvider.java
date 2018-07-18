/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

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

	public CompositeAttributeColumnProvider(CSVDataExportParameters config, T defn) {
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
	protected List<Column> generateSingleAttributeColumns() {
		return generateAttributeColumns(0);
	}
	
	@Override
	protected List<Column> generateAttributeColumns(int attributeIdx) {
		List<Column> columns = new ArrayList<Column>();
		if (isMergedValueSupported()) {
			columns.add(generateMergedValueColumn(attributeIdx));
		}
		columns.addAll(generateAttributeFieldColumns(attributeIdx));
		return columns;
	}
	
	protected List<Column> generateAttributeFieldColumns(int attributeIdx) {
		List<Column> columns = new ArrayList<Column>(fieldNames.length);
		String attrPosSuffix = generateAttributePositionSuffix(attributeIdx);
		for (int fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
			columns.add(generateFieldColumn(fieldNames[fieldIdx], attrPosSuffix));
		}
		return columns;
	}

	protected abstract String[] getFieldNames();
	
	protected Column generateMergedValueColumn(int attributeIdx) {
		return new Column(generateHeadingPrefix() + generateAttributePositionSuffix(attributeIdx));
	}

	protected Column generateFieldColumn(String fieldName, String suffix) {
		return new Column(generateHeadingPrefix() + getConfig().getFieldHeadingSeparator() + fieldName + suffix);
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
	public List<Object> extractValues(Node<?> axis) {
		checkValidAxis(axis);
		List<Node<?>> attributes = extractNodes(axis);
		int maxAttributeValues = getMaxAttributeValues();
		int totHeadings = fieldNames.length * maxAttributeValues;
		List<Object> values = new ArrayList<Object>(totHeadings);
		
		for (int attrIdx = 0; attrIdx < maxAttributeValues; attrIdx ++) {
			Attribute<?, ?> attr = attrIdx < attributes.size() ? (Attribute<?, ?>) attributes.get(attrIdx): null;
			if (isMergedValueSupported()) {
				String val = extractMergedValue(attr);
				values.add(val);
			}
			for (String fieldName : fieldNames) {
				Object val = null;
				if (attr != null) {
					val = extractValue(attr, fieldName);
				}
				values.add(val);
			}
		}
		return values;
	}
	
	protected List<Node<?>> extractNodes(Node<?> axis) {
		Entity parentEntity = getAttributeParentEntity((Entity) axis);
		return parentEntity.getChildren(attributeDefinition);
	}

	protected void checkValidAxis(Node<?> axis) {
		if (axis == null) {
			throw new NullPointerException("Axis must be non-null");
		} else if ( ! (axis instanceof Entity) ) {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}
	
	protected Object extractValue(Field<?> field) {
		Object value = field.getValue();
		return value;
	}

	protected Object extractValue(Attribute<?, ?> attr, String fieldName) {
		Field<?> field = attr.getField(fieldName);
		return extractValue(field);
	}
	
}
