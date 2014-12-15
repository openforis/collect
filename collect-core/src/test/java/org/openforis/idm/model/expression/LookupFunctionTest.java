/**
 * 
 */
package org.openforis.idm.model.expression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.TestSurveyContext;

/**
 * @author M. Togna
 * 
 */
public class LookupFunctionTest extends AbstractTest {

	public static Coordinate TEST_COORDINATE = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(805750 9333820)");
	private static final String TEST_SAMPLING_POINT_DATA = "001";

	@Before
	public void setup() {
		TestSurveyContext surveyContext = (TestSurveyContext) record.getSurveyContext();
		surveyContext.lookupProvider.coordinate = TEST_COORDINATE;
		surveyContext.lookupProvider.samplingPointData = TEST_SAMPLING_POINT_DATA;
	}
	
	@Test
	public void testLookupFunction1Arg() throws InvalidExpressionException {
		String expr = "idm:lookup('sampling_design', 'location', 'cluster', 'id', 'plot', 1)";

		Object object = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, object);
	}

	@Test
	public void testLookupFunction2Arg() throws InvalidExpressionException {
		String expr = "idm:lookup('sampling_design', 'location', 'cluster' , 'id')";
		Object object = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, object);
	}
	
	@Test
	public void testLookupFunctionWithPath() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "id", new Code("205_128"));

		String expr = "idm:lookup('sampling_design', 'location', 'cluster', id, 'plot', '0')";

		Object object = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, object);
	}
	
	@Test
	public void testSamplingPointLookupFunction() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "id", new Code("205_128"));

		String expr = "idm:samplingPointData('region', id)";

		Object object = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_SAMPLING_POINT_DATA, object);
	}
}
