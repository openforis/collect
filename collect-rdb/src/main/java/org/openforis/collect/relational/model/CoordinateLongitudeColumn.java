package org.openforis.collect.relational.model;

import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.path.Path;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CoordinateLongitudeColumn extends CoordinateLatLonColumn {

	CoordinateLongitudeColumn(String name, CoordinateAttributeDefinition defn, Path relPath) {
		super(name, defn, relPath);
	}

}
