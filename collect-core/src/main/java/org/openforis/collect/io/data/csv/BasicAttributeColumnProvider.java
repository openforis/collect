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
	private List<String> columnHeadings;

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
	public List<String> getColumnHeadings() {
		if (columnHeadings == null) {
			columnHeadings = generateColumnHeadings();
		}
		return columnHeadings;
	}

	protected List<String> generateColumnHeadings() {
		if (attributeDefinition.isMultiple()) {
			int maxAttrValues = getMaxAttributeValues();
			int numberOfColumnsPerAttribute = getNumberOfColumnsPerAttribute();
			List<String> headings = new ArrayList<String>(maxAttrValues * numberOfColumnsPerAttribute);
			for (int i = 0; i < maxAttrValues; i++) {
				headings.addAll(generateAttributeColumnHeadings(i));
			}
			return headings;
		} else {
			return generateSingleAttributeColumnHeadings();
		}
	}
	
	@Override
	protected String generateHeadingPrefix() {
		return ColumnProviders.generateHeadingPrefix(attributeDefinition, config);
	}

	protected abstract int getNumberOfColumnsPerAttribute();

	protected abstract List<String> generateSingleAttributeColumnHeadings();

	protected abstract List<String> generateAttributeColumnHeadings(int i);
	
	protected String generateAttributePositionSuffix(int attributeIdx) {
		return attributeDefinition.isMultiple() ? "[" + (attributeIdx + 1) + "]": "";
	}
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(null)
			.append("Attribute", attributeDefinition.getName())
			.append("Column headings", getColumnHeadings())
			.build();
	}
}
