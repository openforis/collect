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
	
}
