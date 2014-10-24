/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author M. Togna
 * 
 */
public class ExpressionTest extends AbstractExpressionTest {

	@Test
	public void testMissingNode() throws InvalidExpressionException {
		String expr = "crew_no";
		Object object = evaluateExpression(expr);
		assertNull(object);
	}
	
	@Test
	public void testNegativeMissingNode() throws InvalidExpressionException {
		String expr = "not(crew_no)";
		Object object = evaluateExpression(expr);
		assertEquals(Boolean.TRUE, object);
	}
	
	@Test
	public void testNegativeNullNode() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "crew_no", (Integer) null);
		String expr = "not(crew_no)";
		Object object = evaluateExpression(expr);
		Assert.assertEquals(Boolean.TRUE, object);
	}
	
	@Test
	public void testAddExpression() throws InvalidExpressionException {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "dbh", 54.2);

		String expr = "plot[1]/tree[1]/dbh + 1";
		Object object = evaluateExpression(expr);
		Assert.assertEquals(55.2, object);
	}

	@Test
	public void testAddWithParentFuncExpression() throws InvalidExpressionException {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "dbh", 54.2);

		String expr = "plot[1]/tree[1]/dbh/parent()/dbh + 1";
		Object object = evaluateExpression(expr);
		Assert.assertEquals(55.2, object);
	}

	@Test
	public void testMissingValueExpressionWithOperation() throws InvalidExpressionException {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "dbh", 54.2);

		String expr = "plot[25]/tree[3]/dbh/parent()/dbh + 4";
		Object object = evaluateExpression(expr);
		Assert.assertEquals(4d, object);
	}

	@Test
	public void testMissingValueExpression2() throws InvalidExpressionException {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "dbh", 54.2);

		String expr = "plot[1]/tree[3]/dbh/parent()/dbh";
		Object object = evaluateExpression(expr);
		Assert.assertNull(object);
	}


	@Test
	public void testConstant() throws InvalidExpressionException {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "dbh", 54.2);
		
		String expr = "543534";
		Object object = evaluateExpression(expr);
		Assert.assertEquals(Double.valueOf(expr), object);
	}

}
