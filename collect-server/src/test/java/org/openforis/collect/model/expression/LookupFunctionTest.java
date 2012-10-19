/**
 * 
 */
package org.openforis.collect.model.expression;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.collect.AbstractTest;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.DefaultValueExpression;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author M. Togna
 * 
 */
public class LookupFunctionTest extends AbstractTest {

	public static Coordinate TEST_COORDINATE = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(805750 9333820)");

	@Test
	public void testLookupFunction1Arg() throws InvalidExpressionException {
		Record record = cluster.getRecord();
		SurveyContext recordContext = record.getSurveyContext();

		String expr = "idm:lookup('sampling_design', 'plot_centre','cluster', 'id','plot', 1)";

		DefaultValueExpression expression = recordContext.getExpressionFactory().createDefaultValueExpression(expr);
		Object object = expression.evaluate(cluster, null);
		Assert.assertEquals(TEST_COORDINATE, object);
	}

	@Test
	public void testLookupFunction2Arg() throws InvalidExpressionException {
		Record record = cluster.getRecord();
		SurveyContext recordContext = record.getSurveyContext();

		String expr = "idm:lookup('sampling_design', 'plot_centre','cluster' ,'id')";
		DefaultValueExpression expression = recordContext.getExpressionFactory().createDefaultValueExpression(expr);
		Object object = expression.evaluate(cluster, null);
		Assert.assertEquals(TEST_COORDINATE, object);
	}

	@Test
	public void testLookupFunctionWithPath() throws InvalidExpressionException {
		Record record = cluster.getRecord();
		EntityBuilder.addValue(cluster, "id", new Code("205_128"));
		SurveyContext recordContext = record.getSurveyContext();

		String expr = "idm:lookup('sampling_design', 'plot_centre','cluster', id,'plot', '0')";

		DefaultValueExpression expression = recordContext.getExpressionFactory().createDefaultValueExpression(expr);
		Object object = expression.evaluate(cluster, null);
		Assert.assertEquals(TEST_COORDINATE, object);
	}
}