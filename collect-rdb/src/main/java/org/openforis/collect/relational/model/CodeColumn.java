/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.collect.relational.DatabaseExporterConfig;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeColumn extends DataColumn {

	CodeColumn(String name, NodeDefinition defn,
			Path relPath, Integer length) {
		super(name, Types.VARCHAR, "varchar", defn, relPath, length, true);
	}
	
	@Override
	public Object extractValue(DatabaseExporterConfig config, Node<?> context) {
		Object value = super.extractValue(config, context);
		if ( value == null ) {
			return config.getDefaultCode();
		} else {
			return value;
		}
	}


}
