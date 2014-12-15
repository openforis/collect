/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;

/**
 * @author M. Togna
 * @author G. Miceli
 */
public class RelevanceExpressionTest extends AbstractTest {

	protected Entity energySource;

	@Before
	public void beforeTest() {
		energySource = EntityBuilder.addEntity(household, "energy_source");
	}

	@Test
	public void testTrue() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", 345.45);
		String expr = "plot_direction >= 0 and plot_direction <= 359";
		assertTrue(evaluateExpression(expr, cluster));
	}

	@Test
	public void testFalse() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", 385.45);
		String expr = "plot_direction >= 0 and plot_direction <= 359";
		assertFalse(evaluateExpression(expr, cluster));
	}

	@Test
	public void testRelevanceOnNodeExpression()
			throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", 345.45);
		RealAttribute plotDistance = EntityBuilder.addValue(cluster,
				"plot_distance", 12.2);
		String expr = "parent()/plot_direction";
		assertTrue(evaluateExpression(expr, plotDistance));
	}

	@Test
	public void testRelevanceOnNegativeNodeExpression()
			throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", 345.45);
		RealAttribute plotDistance = EntityBuilder.addValue(cluster,
				"plot_distance", 12.2);
		String expr = "not(parent()/plot_direction)";
		assertFalse(evaluateExpression(expr, plotDistance));
	}

	@Test
	public void testBlankValueWithMissingValue()
			throws InvalidExpressionException {
		String expr = "plot_direction != ''";
		assertFalse(evaluateExpression(expr, cluster));
	}

	@Test
	public void testBlankValueWithMissingValue2()
			throws InvalidExpressionException {
		String expr = "plot_direction = ''";
		assertFalse(evaluateExpression(expr, cluster));
	}

	@Test
	public void testBlankValue() throws InvalidExpressionException {
		String expr = "plot_direction != ''";
		EntityBuilder.addValue(cluster, "plot_direction", (Double) null);
		assertFalse(evaluateExpression(expr, cluster));
	}

	@Test
	public void testBlankTextValueTreatedAsMissing()
			throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "remarks", "");
		String expr = "remarks = ''";
		assertFalse(evaluateExpression(expr, cluster));
	}

	@Test
	public void testNotBlankValue() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", (Double) 2.25);
		String expr = "plot_direction != ''";
		assertTrue(evaluateExpression(expr, cluster));
	}

	@Test
	public void testNotFunction() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "plot_direction", 12.8);
		String expr = "not(plot_direction != 12.8)";
		assertTrue(evaluateExpression(expr, cluster));
	}

	@Test
	public void testEqStringValue() throws InvalidExpressionException {
		EntityBuilder.addValue(energySource, "type", new Code("other"));
		assertTrue(evaluateExpression("type='other'", energySource));
	}

	@Test
	public void testNotEqStringValue() throws InvalidExpressionException {
		EntityBuilder.addValue(energySource, "type", new Code("other"));
		assertFalse(evaluateExpression("type != 'other'", energySource));
	}

	@Test
	@Ignore
	public void testDefaultWithMissingNode2() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(plot, "soil");
		assertFalse(plot.isRelevant("soil"));
	}

	@Test
	public void testBooleanValue2() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot, "accessibility", new Code("0"));
		EntityBuilder.addValue(plot, "permanent", true);
		EntityBuilder.addEntity(plot, "soil");
		assertTrue(plot.isRelevant("soil"));
	}

	@Test
	@Ignore
	public void testBooleanValue3() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot, "accessibility", new Code("1"));
		EntityBuilder.addValue(plot, "permanent", true);
		EntityBuilder.addEntity(plot, "soil");
		assertFalse(plot.isRelevant("soil"));
	}

	@Test
	@Ignore
	public void testBooleanValue4() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot, "accessibility", new Code("0"));
		EntityBuilder.addValue(plot, "permanent", false);
		EntityBuilder.addEntity(plot, "soil");
		assertFalse(plot.isRelevant("soil"));
	}

	private boolean evaluateExpression(String expr,
			Node<? extends NodeDefinition> context)
			throws InvalidExpressionException {
		return expressionEvaluator.evaluateBoolean(context, null, expr);
	}

}
