/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;

/**
 * @author M. Togna
 * 
 */
public class CoordinateValidator implements ValidationRule<CoordinateAttribute> {

	private static CoordinateOperations COORDINATE_OPERATIONS;

	static {
		ServiceLoader<CoordinateOperations> loader = ServiceLoader.load(CoordinateOperations.class);
		Iterator<CoordinateOperations> it = loader.iterator();
		if ( it.hasNext() ) {
			COORDINATE_OPERATIONS = it.next();
		}
	}
	
	@Override
	public ValidationResultFlag evaluate(CoordinateAttribute node) {
		Coordinate coordinate = node.getValue();
		CoordinateAttributeDefinition definition = node.getDefinition();
		List<SpatialReferenceSystem> srs = definition.getSurvey().getSpatialReferenceSystems();

		boolean valid = coordinate.getX() != null && coordinate.getY() != null && isSrsIdValid(srs, coordinate.getSrsId());
		
		if ( valid && COORDINATE_OPERATIONS != null ) {
			valid = COORDINATE_OPERATIONS.validate(coordinate);
		}
		
		return ValidationResultFlag.valueOf(valid);
	}

	private boolean isSrsIdValid(List<SpatialReferenceSystem> srs, String srsId) {
		for (SpatialReferenceSystem spatialReferenceSystem : srs) {
			if (spatialReferenceSystem.getId().equals(srsId)) {
				return true;
			}
		}
		return false;
	}

}
