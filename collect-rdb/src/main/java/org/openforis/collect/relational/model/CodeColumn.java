/**
 * 
 */
package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeColumn extends DataColumn {
	
	CodeColumn(String name, NodeDefinition defn,
			Path relPath, Integer length, String defaultValue) {
		super(name, RDBJdbcType.VARCHAR, defn, relPath, length, true, defaultValue);
	}
	
}
