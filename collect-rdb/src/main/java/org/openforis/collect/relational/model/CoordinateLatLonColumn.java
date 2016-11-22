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
public abstract class CoordinateLatLonColumn extends DataColumn {

	CoordinateLatLonColumn(String name, CoordinateAttributeDefinition defn, Path relPath) {
		super(name, RDBJdbcType.fromType(Double.class), defn, 
				relPath, getFieldLength(Double.class), true);
	}

}
