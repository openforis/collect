/**
 * 
 */
package org.openforis.collect.io.data.csv;

import org.apache.commons.lang3.ObjectUtils;
import org.openforis.idm.metamodel.AttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public abstract class BasicAttributeColumnProvider<T extends AttributeDefinition> extends BasicColumnProvider {

	protected T attributeDefinition;

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
}
