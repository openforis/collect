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
import org.openforis.idm.metamodel.EntityDefinition;
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
	
	protected List<String> generateAttributeFieldHeadings(int attributeIdx) {
		List<String> headings = new ArrayList<String>(fieldNames.length);
		String attrPosSuffix = generateAttributePositionSuffix(attributeIdx);
		for (int fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
			headings.add(generateFieldHeading(fieldNames[fieldIdx]) + attrPosSuffix);
		}
		return headings;
	}

	protected abstract String[] getFieldNames();
	
	protected String generateMergedValueHeading(int attributeIdx) {
		return generateHeadingPrefix() + generateAttributePositionSuffix(attributeIdx);
	}

	protected String generateFieldHeading(String fieldName) {
		return generateHeadingPrefix() + getConfig().getFieldHeadingSeparator() + fieldName;
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
		List<Node<?>> attributes = extractNodes(axis);
		int maxAttributeValues = getMaxAttributeValues();
		int totHeadings = fieldNames.length * maxAttributeValues;
		List<String> values = new ArrayList<String>(totHeadings);
		
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
		return values;
	}
	
	protected List<Node<?>> extractNodes(Node<?> axis) {
		Entity parentEntity = findNodesParentEntity((Entity) axis);
		return parentEntity.getChildren(attributeDefinition);
	}

	private Entity findNodesParentEntity(Entity axis) {
		List<EntityDefinition> ancestorEntityDefs = attributeDefinition.getAncestorEntityDefinitionsUpTo(axis.getDefinition());
		Entity currentParentEntity = axis;
		if (! ancestorEntityDefs.isEmpty()) {
			for (int i = ancestorEntityDefs.size() - 1; i >= 0; i--) {
				EntityDefinition ancestorEntityDef = ancestorEntityDefs.get(i);
				if (ancestorEntityDef.isMultiple()) {
					throw new IllegalStateException(String.format(
							"Error extracting values for composite attribute %s in survey %s: single entity expected but multiple found: %s", 
							attributeDefinition.getPath(), attributeDefinition.getSurvey().getName(), ancestorEntityDef.getPath()));
				} else {
					currentParentEntity = currentParentEntity.getChild(ancestorEntityDef);
				}
			}
		}
		return currentParentEntity;
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
