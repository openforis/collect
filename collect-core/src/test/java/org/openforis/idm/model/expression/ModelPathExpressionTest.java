/**
 * 
 */
package org.openforis.idm.model.expression;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.RealAttribute;

/**
 * @author M. Togna
 * 
 */
public class ModelPathExpressionTest extends AbstractTest {

	@Test
	public void testIteratePath() throws InvalidExpressionException {
		String entityName = "plot";
		EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addEntity(cluster, entityName);
		List<Node<?>> list = iterateExpression(entityName, cluster);

		Assert.assertEquals(3, list.size());
	}
	
	@Test
	public void testParent() throws InvalidExpressionException{
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		List<Node<?>> plots = iterateExpression("parent()", plot);
		Assert.assertEquals(1, plots.size());
	}

	@Test
	public void testThis() throws InvalidExpressionException{
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		CodeAttribute plotNum = EntityBuilder.addValue(plot, "no", new Code("1"));
		
		List<Node<?>> plotNums = iterateExpression("$this", plot, plotNum);
		Assert.assertEquals(1, plotNums.size());
	}

	@Test
	public void testAttributeParent() throws InvalidExpressionException{
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		RealAttribute canopyCover = EntityBuilder.addValue(plot, "canopy_cover", 12.56);
		List<Node<?>> plots = iterateExpression("parent()", canopyCover);
		Assert.assertEquals(1, plots.size());
	}
	
	@Test(expected = InvalidExpressionException.class)
	public void testIterateInvalidPath() throws InvalidExpressionException {
		String entityName = "plot";
		EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addEntity(cluster, entityName);
		String expr = "plot^2";
		List<Node<?>> list = iterateExpression(expr, cluster);

		Assert.assertEquals(3, list.size());
	}

	@Test
	public void testIteratePath2() throws InvalidExpressionException {
		String entityName = "plot";
		Entity plot1 = EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addValue(plot1, "no", new Code("1"));
		Entity plot2 = EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addValue(plot2, "no", new Code("1"));
		Entity plot3 = EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addValue(plot3, "no", new Code("1"));

		String expr = "plot/no";
		List<Node<?>> list = iterateExpression(expr, cluster);

		Assert.assertEquals(3, list.size());
	}


	@Test
	public void testParentFunction() throws InvalidExpressionException {
		String entityName = "plot";
		Entity plot = EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addValue(cluster, "id", new Code("123_456"));

		String expr = "parent()/id";
		List<Node<?>> list = iterateExpression(expr, plot);

		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testIteratePath3() throws InvalidExpressionException {
		String entityName = "time_study";
		EntityBuilder.addEntity(cluster, entityName);
		EntityBuilder.addEntity(cluster, entityName);
		List<Node<?>> list = iterateExpression(entityName, cluster);

		Assert.assertEquals(2, list.size());
	}

	private List<Node<?>> iterateExpression(String expr, Node<?> context) throws InvalidExpressionException {
		return iterateExpression(expr, context, null);
	}

	private List<Node<?>> iterateExpression(String expr, Node<?> context, Node<?> thisNode) throws InvalidExpressionException {
		return expressionEvaluator.evaluateNodes(context, thisNode, expr);
	}
}
