/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;

/**
 * @author M. Togna
 * 
 */
public class CoordinateValidator implements ValidationRule<CoordinateAttribute> {

	private static final double MAX_ALTITUDE = 8850;

	@Override
	public ValidationResultFlag evaluate(CoordinateAttribute node) {
		Coordinate coordinate = node.getValue();
		CoordinateAttributeDefinition definition = node.getDefinition();
		CollectSurvey survey = definition.getSurvey();
		List<SpatialReferenceSystem> srs = survey.getSpatialReferenceSystems();

		boolean valid = coordinate.getX() != null && coordinate.getY() != null && isSrsIdValid(srs, coordinate.getSrsId());
		
		if (valid && survey.getAnnotations().isIncludeCoordinateAltitude(definition)) {
			valid = coordinate.getAltitude() == null || coordinate.getAltitude() < MAX_ALTITUDE;
		}
		
		if ( valid ) {
			valid = node.getSurveyContext().getCoordinateOperations().validate(coordinate);
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
