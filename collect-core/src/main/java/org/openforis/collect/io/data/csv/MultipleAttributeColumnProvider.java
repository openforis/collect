package org.openforis.collect.io.data.csv;

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
 */
public class MultipleAttributeColumnProvider implements ColumnProvider {

	private String headerName;
	private String delimiter;
	private AttributeDefinition defn;

	public MultipleAttributeColumnProvider(AttributeDefinition defn, String delimiter) {
		this(defn, delimiter, defn.getName());
	}
	
	public MultipleAttributeColumnProvider(AttributeDefinition defn, String delimiter, String headerName) {
		this.defn = defn;
		this.delimiter = delimiter;
		this.headerName = headerName;
	}
	
	public List<String> getColumnHeadings() {
		return Collections.unmodifiableList(Arrays.asList(headerName));
	}
	
	public List<String> extractValues(Node<?> axis) {
		if ( axis instanceof Entity ) {
			Entity entity = (Entity) axis;
			int cnt = entity.getCount(defn.getName());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < cnt; i++) {
				if ( i > 0 ) {
					sb.append(delimiter);
				}
				String fieldName = defn.getMainFieldName();
				String val = extractValue(entity, fieldName, i);
				sb.append(val);
			}
			return Arrays.asList(sb.toString());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private String extractValue(Entity entity, String fieldName, int i) {
		Attribute<?,?> attr = (Attribute<?, ?>) entity.get(defn, i);
		if ( attr == null ) {
			return ""; 
		} else {
			Field<?> fld = attr.getField(fieldName);
			Object v = fld.getValue();
			return v == null ? "" : v.toString();
		}
	}
}