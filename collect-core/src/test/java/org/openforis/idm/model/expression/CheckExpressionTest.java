/**
 * 
 */
package org.openforis.idm.model.expression;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;

/**
 * @author M. Togna
 * 
 */
public class CheckExpressionTest extends AbstractTest {

	@Test
	public void testStatic() throws InvalidExpressionException {
		RealAttribute plotDirection = EntityBuilder.addValue(cluster, "plot_direction", 385.45);

		String expr = "900 < 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertFalse(b);
	}

	@Test
	public void testTrue() throws InvalidExpressionException {
		RealAttribute plotDirection = EntityBuilder.addValue(cluster, "plot_direction", 345.45);

		String expr = "$this >= 0 and $this <= 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertTrue(b);
	}

	@Test
	public void testFalse() throws InvalidExpressionException {
		RealAttribute plotDirection = EntityBuilder.addValue(cluster, "plot_direction", 385.45);

		String expr = "$this >= 0 and $this <= 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertFalse(b);
	}

	@Ignore
	@Test(expected = InvalidExpressionException.class)
	public void testDefaultWithInvalidPath() throws InvalidExpressionException {
		RealAttribute plotDirection = EntityBuilder.addValue(cluster, "plot_direction", 345.45);

		String expr = "parent()/missing_attr >= 0 and $this <= 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertTrue(b);
	}
	
	private boolean evaluateExpression(String expr, Node<? extends NodeDefinition> thisNode) throws InvalidExpressionException {
		return expressionEvaluator.evaluateBoolean(thisNode.getParent(), thisNode, expr);
	}
}
