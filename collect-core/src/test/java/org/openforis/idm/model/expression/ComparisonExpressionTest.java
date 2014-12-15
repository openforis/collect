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
import org.openforis.idm.model.RealValue;

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
		monthlyConsumption = EntityBuilder.addValue(energySource, "monthly_consumption", (RealRange) null);
		distanceToForest = EntityBuilder.addValue(household, "distance_to_forest", (Double) null);
	}
	
	@Test
	public void testGtLtOnRange() throws InvalidExpressionException {
		monthlyConsumption.setValue(new RealRange(11.0, 13.0));
		monthlyConsumption.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(energySource, "monthly_consumption > 10 and monthly_consumption < 14.5"));
	}
	
	@Test
	public void testGteqLtOnRange() throws InvalidExpressionException {
		monthlyConsumption.setValue(new RealRange(10.1, 13.0));
		monthlyConsumption.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(energySource, "monthly_consumption >= 10.1 and monthly_consumption < 14.5"));
	}
	
	@Test
	public void testGtLtWrongOnRange() throws InvalidExpressionException {
		monthlyConsumption.setValue(new RealRange(0.0, 15.0));
		monthlyConsumption.updateSummaryInfo();
		Assert.assertFalse(evaluateExpression(energySource, "monthly_consumption > 10 and monthly_consumption < 14.5"));
	}
	
	@Test
	public void testGtOnNumber() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(23.5));
		distanceToForest.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(household, "distance_to_forest > 10"));
	}

	@Test
	public void testGtEqOnNumber() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(10.0));
		distanceToForest.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(household, "distance_to_forest >= 10"));
	}

	@Test
	public void testLtOnNumber() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(23.5));
		distanceToForest.updateSummaryInfo();
		Assert.assertFalse(evaluateExpression(household, "distance_to_forest < 10"));
	}

	@Test
	public void testLtEqOnNumber() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(8.98));
		distanceToForest.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(household, "distance_to_forest <= 10"));
	}

	@Test
	public void testEqOnNumber() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(10.0));
		distanceToForest.updateSummaryInfo();
		Assert.assertTrue(evaluateExpression(household, "distance_to_forest = 10"));
	}

	@Test
	public void testEqOnNumber2() throws InvalidExpressionException {
		distanceToForest.setValue(new RealValue(8.0));
		distanceToForest.updateSummaryInfo();
		Assert.assertFalse(evaluateExpression(household, "distance_to_forest = 10"));
	}

	private boolean evaluateExpression(Node<?> context, String expr) throws InvalidExpressionException {
		return expressionEvaluator.evaluateBoolean(context, null, expr);
	}
}
