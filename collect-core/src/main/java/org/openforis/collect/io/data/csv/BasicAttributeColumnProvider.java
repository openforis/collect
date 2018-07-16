/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.idm.metamodel.AttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public abstract class BasicAttributeColumnProvider<T extends AttributeDefinition> extends BasicColumnProvider {

	protected T attributeDefinition;
	private List<Column> columns;

	public BasicAttributeColumnProvider(CSVDataExportParameters config, T attrDefn) {
		super(config);
		this.attributeDefinition = attrDefn;
	}

	protected int getMaxAttributeValues() {
		if (attributeDefinition.isMultiple()) {
			return ObjectUtils.defaultIfNull(attributeDefinition.getFixedMaxCount(), getConfig().getMaxMultipleAttributeValues());
		} else {
			return 1;
		}
	}
	
	@Override
	public List<Column> getColumns() {
		if (columns == null) {
			columns = generateColumns();
		}
		return columns;
	}

	protected List<Column> generateColumns() {
		if (attributeDefinition.isMultiple()) {
			int maxAttrValues = getMaxAttributeValues();
			int numberOfColumnsPerAttribute = getNumberOfColumnsPerAttribute();
			List<Column> columns = new ArrayList<Column>(maxAttrValues * numberOfColumnsPerAttribute);
			for (int i = 0; i < maxAttrValues; i++) {
				columns.addAll(generateAttributeColumns(i));
			}
			return columns;
		} else {
			return generateSingleAttributeColumns();
		}
	}
	
	@Override
	protected String generateHeadingPrefix() {
		return ColumnProviders.generateHeadingPrefix(attributeDefinition, config);
	}

	protected abstract int getNumberOfColumnsPerAttribute();

	protected abstract List<Column> generateSingleAttributeColumns();

	protected abstract List<Column> generateAttributeColumns(int i);
	
	protected String generateAttributePositionSuffix(int attributeIdx) {
		return attributeDefinition.isMultiple() ? "[" + (attributeIdx + 1) + "]": "";
	}
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(null)
			.append("Attribute", attributeDefinition.getName())
			.append("Columns", getColumns())
			.build();
	}
}
