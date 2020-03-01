/**
 * 
 */
package org.openforis.collect.model.expression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.AbstractTest;
import org.openforis.collect.model.CollectTestSurveyContext;
import org.openforis.collect.model.TestLookupProviderImpl;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author M. Togna
 * 
 */
public class CollectLookupFunctionTest extends AbstractTest {

	public static Coordinate TEST_COORDINATE = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(805750 9333820)");
	private ExpressionEvaluator expressionEvaluator;
	
	@Before
	public void setup() {
		CollectTestSurveyContext surveyContext = (CollectTestSurveyContext) record.getSurveyContext();
		((TestLookupProviderImpl) surveyContext.getExpressionFactory().getLookupProvider()).coordinate = TEST_COORDINATE;
		expressionEvaluator = surveyContext.getExpressionEvaluator();
	}
	
	@Test
	public void testLookupFunction1Arg() throws InvalidExpressionException {
		String expr = "idm:lookup('sampling_design', 'location','cluster', 'id','plot', 1)";
		Object value = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, value);
	}

	@Test
	public void testLookupFunction2Arg() throws InvalidExpressionException {
		String expr = "idm:lookup('sampling_design', 'location','cluster' ,'id')";
		Object value = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, value);
	}

	@Test
	public void testLookupFunctionWithPath() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "id", new Code("205_128"));
		String expr = "idm:lookup('sampling_design', 'location','cluster', id,'plot', '0')";
		Object value = expressionEvaluator.evaluateValue(cluster, null, expr);
		Assert.assertEquals(TEST_COORDINATE, value);
	}
}