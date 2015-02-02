package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SingleFieldAttributeColumnProvider extends BasicAttributeColumnProvider<AttributeDefinition> {
	
	private String headerName;

	public SingleFieldAttributeColumnProvider(CSVExportConfiguration config, AttributeDefinition defn) {
		this(config, defn, defn.getName());
	}
	
	public SingleFieldAttributeColumnProvider(CSVExportConfiguration config, AttributeDefinition defn, String headerName) {
		super(config, defn);
		this.headerName = headerName;
	}
	
	@Override
	public List<String> generateColumnHeadings() {
		if (attributeDefinition.isMultiple()) {
			int maxAttrValues = getMaxAttributeValues();
			List<String> headings = new ArrayList<String>(maxAttrValues);
			for (int i = 0; i < maxAttrValues; i++) {
				headings.add(headerName + "[" + (i + 1) + "]");
			}
			return headings;
		} else {
			return Collections.unmodifiableList(Arrays.asList(headerName));
		}
	}

	public List<String> extractValues(Node<?> axis) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else if ( axis instanceof Entity ) {
			Entity entity = (Entity) axis;
			int maxAttrValues = getMaxAttributeValues();
			List<String> values = new ArrayList<String>(maxAttrValues);
			int attrCount = entity.getCount(attributeDefinition);
			for (int attrIdx = 0; attrIdx < maxAttrValues; attrIdx++) {
				String val;
				if (attrIdx < attrCount) {
					Attribute<?, ?> attr = (Attribute<?, ?>) entity.get(attributeDefinition, attrIdx);
					val = extractValue(attr);
				} else {
					val = "";
				}
				values.add(val);
			}
			return values;
		} else {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}

	private String extractValue(Attribute<?, ?> attr) {
		String mainFieldName = attr.getDefinition().getMainFieldName();
		Field<?> fld = attr.getField(mainFieldName);
		Object v = fld.getValue();
		String stringVal = v == null ? "" : v.toString();
		return stringVal;
	}
}
