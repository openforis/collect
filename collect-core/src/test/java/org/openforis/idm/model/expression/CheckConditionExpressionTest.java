/**
 * 
 */
package org.openforis.idm.model.expression;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;

/**
 * @author M. Togna
 * 
 */
public class CheckConditionExpressionTest extends AbstractTest {

	@Test
	public void testTrue() throws InvalidExpressionException {
		RealAttribute plotDirection =  EntityBuilder.addValue(cluster, "plot_direction", 345.45);

		String expr = "$this >= 0 and $this <= 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertTrue(b);
	}

	@Test
	public void testFalse() throws InvalidExpressionException {
		RealAttribute plotDirection =  EntityBuilder.addValue(cluster, "plot_direction", 385.45);
		
		String expr = "$this >= 0 and $this <= 359";
		boolean b = evaluateExpression(expr, plotDirection);
		Assert.assertFalse(b);
	}

	private boolean evaluateExpression(String expr, Attribute<?,?> thisNode) throws InvalidExpressionException {
		return expressionEvaluator.evaluateBoolean(thisNode.getParent(), thisNode, expr);
	}
}
