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
 * @author M. Togna
 */
public class SingleAttributeColumnProvider implements ColumnProvider {
	private String attributeName;
	private String headerName;

	public SingleAttributeColumnProvider(AttributeDefinition defn) {
		this(defn, defn.getName());
	}
	
	public SingleAttributeColumnProvider(AttributeDefinition defn, String headerName) {
		this.attributeName = defn.getName();
		this.headerName = headerName;
	}
	
	public List<String> getColumnHeadings() {
		return Collections.unmodifiableList(Arrays.asList(headerName));
	}

	public List<String> extractValues(Node<?> axis) {
		if ( axis == null ) {
			throw new NullPointerException("Axis must be non-null");
		} else if ( axis instanceof Entity ) {
			Attribute<?,?> attr = (Attribute<?, ?>) ((Entity) axis).get(attributeName, 0);
			if ( attr == null ) {
				return Arrays.asList(""); 
			} else {
				Field<?> fld = attr.getField(0);
				Object v = fld.getValue();
				return Arrays.asList(v == null ? "" : v.toString());
			}
		} else {
			throw new UnsupportedOperationException("Axis must be an Entity");
		}
	}
}
