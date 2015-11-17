/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class CodeValueFKColumn extends DataColumn {
	
	private String defaultCodeValue;

	CodeValueFKColumn(String name, CodeAttributeDefinition defn, Path relPath, String defaultCodeValue) {
		super(name, Types.BIGINT, "bigint", defn, relPath, null, true);
		this.defaultCodeValue = defaultCodeValue;
	}

	public String getDefaultCodeValue() {
		return defaultCodeValue;
	}
}
