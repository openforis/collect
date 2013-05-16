/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeColumn extends DataColumn {
	
	CodeColumn(String name, NodeDefinition defn,
			Path relPath, Integer length, String defaultValue) {
		super(name, Types.VARCHAR, "varchar", defn, relPath, length, true, defaultValue);
	}
	
}
