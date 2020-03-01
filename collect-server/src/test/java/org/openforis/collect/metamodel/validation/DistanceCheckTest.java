/**
 * 
 */
package org.openforis.collect.metamodel.validation;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.model.CollectTestSurveyContext;
import org.openforis.collect.model.TestLookupProviderImpl;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author M. Togna
 * 
 */
public class DistanceCheckTest extends ValidationTest {

	public static Coordinate TEST_COORDINATE = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(805750 9333820)");
	private static final String TEST_SAMPLING_POINT_DATA = "001";

	@Before
	public void setup() {
		CollectTestSurveyContext surveyContext = (CollectTestSurveyContext) record.getSurveyContext();
		TestLookupProviderImpl lookupProvider = (TestLookupProviderImpl) surveyContext.getExpressionFactory().getLookupProvider();
		lookupProvider.coordinate = TEST_COORDINATE;
		lookupProvider.samplingPointData = TEST_SAMPLING_POINT_DATA;
	}

	@Test
	public void testValidMaxDistance() {
		String coordStr = "SRID=EPSG:21035;POINT(805750 9333820)";
		Coordinate coord = Coordinate.parseCoordinate(coordStr);
		EntityBuilder.addValue(cluster, "id", new Code("001"));
		CoordinateAttribute vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord);
		ValidationResults results = validate(vehicleLocation);
		Assert.assertFalse(containsDistanceCheck(results.getErrors()));
	}

	@Test
	public void testErrorMaxDistance() {
		String coordStr = "SRID=EPSG:21035;POINT(915750 9333820)";
		Coordinate coord = Coordinate.parseCoordinate(coordStr);
		EntityBuilder.addValue(cluster, "id", new Code("001"));
		CoordinateAttribute vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord);
		ValidationResults results = validate(vehicleLocation);
		Assert.assertTrue(containsDistanceCheck(results.getErrors()));
	}

	@Test
	public void testWarnMaxDistance() {
		String coordStr = "SRID=EPSG:21035;POINT(885750 9333820)";
		Coordinate coord = Coordinate.parseCoordinate(coordStr);
		EntityBuilder.addValue(cluster, "id", new Code("001"));
		CoordinateAttribute vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord);
		ValidationResults results = validate(vehicleLocation);
		Assert.assertFalse(containsDistanceCheck(results.getErrors()));
		Assert.assertTrue(containsDistanceCheck(results.getWarnings()));
	}
	
	@Test
	public void testEvalutateDistanceCheckDestinationPoint() {
		EntityBuilder.addValue(cluster, "id", new Code("001"));
		Coordinate coord = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(885750 9333820)");
		CoordinateAttribute vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord);
		CoordinateAttributeDefinition defn = vehicleLocation.getDefinition();
		DistanceCheck check = (DistanceCheck) defn.getChecks().get(0);
		Coordinate destinationPoint = check.evaluateDestinationPoint(vehicleLocation);
		Assert.assertEquals(TEST_COORDINATE, destinationPoint);
	}

	@Test
	public void testEvalutateDistanceCheckMaxDistance() {
		EntityBuilder.addValue(cluster, "id", new Code("001"));
		Coordinate coord = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(885750 9333820)");
		CoordinateAttribute vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord);
		CoordinateAttributeDefinition defn = vehicleLocation.getDefinition();
		DistanceCheck check = (DistanceCheck) defn.getChecks().get(0);
		Double maxDistance = check.evaluateMaxDistance(vehicleLocation);
		Assert.assertEquals(Double.valueOf(100000d), maxDistance);
	}
	
	private boolean containsDistanceCheck(List<ValidationResult> errors) {
		for (ValidationResult result : errors) {
			if (result.getValidator() instanceof DistanceCheck) {
				return true;
			}
		}
		return false;
	}

}