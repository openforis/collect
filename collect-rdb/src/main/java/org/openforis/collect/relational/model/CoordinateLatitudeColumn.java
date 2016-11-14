package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.path.Path;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CoordinateLatitudeColumn extends DataColumn {

	CoordinateLatitudeColumn(String name, CoordinateAttributeDefinition defn, Path relPath) {
		super(name, RDBJdbcType.fromType(Double.class), defn, 
				relPath, getFieldLength(Double.class), true);
	}

}
