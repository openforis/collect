package org.openforis.collect.io.data.csv.columnProviders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.data.csv.Column;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 * @deprecated
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
	
	public List<Column> getColumns() {
		return Collections.unmodifiableList(Arrays.asList(new Column(headerName)));
	}
	
	public List<Object> extractValues(Node<?> axis) {
		if ( axis instanceof Entity ) {
			Entity entity = (Entity) axis;
			int cnt = entity.getCount(defn.getName());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < cnt; i++) {
				if ( i > 0 ) {
					sb.append(delimiter);
				}
				String mainFieldName = null;
				if (defn.hasMainField()) {
					mainFieldName = defn.getMainFieldName();
				}
				Object val = extractValue(entity, mainFieldName, i);
				sb.append(val == null ? "" : val);
			}
			return Arrays.<Object>asList(sb.toString());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private Object extractValue(Entity entity, String fieldName, int i) {
		Attribute<?,?> attr = (Attribute<?, ?>) entity.getChild(defn, i);
		if ( attr == null ) {
			return null; 
		} else {
			Object v;
			if (fieldName == null) {
				v = attr.getValue();
			} else {
				Field<?> fld = attr.getField(fieldName);
				v = fld.getValue();
			}
			return v;
		}
	}
}