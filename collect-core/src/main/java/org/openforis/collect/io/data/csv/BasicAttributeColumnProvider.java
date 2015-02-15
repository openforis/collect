/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.idm.metamodel.AttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public abstract class BasicAttributeColumnProvider<T extends AttributeDefinition> extends BasicColumnProvider {

	protected T attributeDefinition;
	private List<String> columnHeadings;

	public BasicAttributeColumnProvider(CSVExportConfiguration config, T attrDefn) {
		super(config);
		this.attributeDefinition = attrDefn;
	}

	protected String getAttributeName() {
		return attributeDefinition.getName();
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

	protected abstract List<String> generateColumnHeadings();
}
