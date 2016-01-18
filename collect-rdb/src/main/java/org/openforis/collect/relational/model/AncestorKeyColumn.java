/**
 * 
 */
package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class AncestorKeyColumn extends DataColumn {
	
	AncestorKeyColumn(String name, FieldDefinition<?> defn, Path relPath) {
		super(name, RDBJdbcType.fromType(defn.getValueType()), defn, 
				relPath, getFieldLength(defn), true);
	}
	
	protected static Integer getFieldLength(FieldDefinition<?> defn) {
		Class<?> type = defn.getValueType();
		if ( type == Integer.class ) {
			return null;
		} else if ( type == Double.class ) {
			return 24;
		} else if ( type == String.class ) {
			return 255;
		} else {
			throw new UnsupportedOperationException("Unknown field type "+type);				
		}
	}
	
}
