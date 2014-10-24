/**
 * 
 */
package org.openforis.idm.model.expression;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;

/**
 * @author M. Togna
 * 
 */
public class ComparisonExpressionTest extends AbstractTest {

	protected Entity energySource;
	protected RealRangeAttribute monthlyConsumption;
	protected RealAttribute distanceToForest;

	@Before
	public void beforeTest() {
		energySource = EntityBuilder.addEntity(household, "energy_source");
		monthlyConsumption = EntityBuilder.addValue(energySource, "monthly_consumption", new RealRange(null, null));
		distanceToForest = EntityBuilder.addValue(household, "distance_to_forest", (Double) null);
	}
	
	@Test
	public void testGtLtOnRange() throws InvalidExpressionException {
		String expr = "monthly_consumption > 10 and monthly_consumption < 14.5";
		monthlyConsumption.setFrom(11.0);
		monthlyConsumption.setTo(13.0);
		Assert.assertTrue(evaluateExpression(energySource, expr));
	}
	
	@Test
	public void testGteqLtOnRange() throws InvalidExpressionException {
		String expr = "monthly_consumption >= 10.1 and monthly_consumption < 14.5";
		monthlyConsumption.setFrom(10.1);
		monthlyConsumption.setTo(13.0);
		Assert.assertTrue(evaluateExpression(energySource, expr));
	}
	
	@Test
	public void testGtLtWrongOnRange() throws InvalidExpressionException {
		String expr = "monthly_consumption > 10 and monthly_consumption < 14.5";
		monthlyConsumption.setFrom(0.0);
		monthlyConsumption.setTo(15.0);
		Assert.assertFalse(evaluateExpression(energySource, expr));
	}
	
	@Test
	public void testGtOnNumber() throws InvalidExpressionException {
		String expr = "distance_to_forest > 10";
		distanceToForest.setNumber(23.5);
		Assert.assertTrue(evaluateExpression(household, expr));
	}

	@Test
	public void testGtEqOnNumber() throws InvalidExpressionException {
		String expr = "distance_to_forest >= 10";
		distanceToForest.setNumber(10.0);
		Assert.assertTrue(evaluateExpression(household, expr));
	}

	@Test
	public void testLtOnNumber() throws InvalidExpressionException {
		String expr = "distance_to_forest < 10";
		distanceToForest.setNumber(23.5);
		Assert.assertFalse(evaluateExpression(household, expr));
	}

	@Test
	public void testLtEqOnNumber() throws InvalidExpressionException {
		String expr = "distance_to_forest <= 10";
		distanceToForest.setNumber(8.98);
		Assert.assertTrue(evaluateExpression(household, expr));
	}

	@Test
	public void testEqOnNumber() throws InvalidExpressionException {
		String expr = "distance_to_forest = 10";
		distanceToForest.setNumber(10.0);
		Assert.assertTrue(evaluateExpression(household, expr));
	}

	@Test
	public void testEqOnNumber2() throws InvalidExpressionException {
		String expr = "distance_to_forest = 10";
		distanceToForest.setNumber(8.0);
		Assert.assertFalse(evaluateExpression(household, expr));
	}

	private boolean evaluateExpression(Node<?> context, String expr) throws InvalidExpressionException {
		ExpressionFactory expressionFactory = context.getRecord().getSurveyContext().getExpressionFactory();
		ValueExpression expression = expressionFactory.createValueExpression(expr);
		Object object = expression.evaluate(context, null);
		return (Boolean) object;
	}
}
