/**
 * 
 */
package org.openforis.idm.model.expression;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.AbstractTest;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;

import junit.framework.Assert;

/**
 * @author M. Togna
 * 
 */
public class AbsoluteModelPathExpressionTest extends AbstractTest {

	@Test
	public void testInterateRoot() throws InvalidExpressionException {
		EntityBuilder.addEntity(cluster, "plot");
		List<Node<?>> list = iterateExpression("/cluster", record);

		Assert.assertEquals(1, list.size());
	}

	@Test
	public void testIteratePath() throws InvalidExpressionException {
		EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addEntity(cluster, "plot");
		List<Node<?>> list = iterateExpression("/cluster/plot", record);

		Assert.assertEquals(3, list.size());
	}
	
	@Test
	public void testIteratePath2() throws InvalidExpressionException {
		Entity plot1 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot1, "no", new Code("1"));
		Entity plot2 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot2, "no", new Code("1"));
		Entity plot3 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot3, "no", new Code("1"));

		List<Node<?>> list = iterateExpression("/cluster/plot/no", record);

		Assert.assertEquals(3, list.size());
	}


	@Test(expected=InvalidExpressionException.class)
	public void testInvalidExpression() throws InvalidExpressionException {
		Entity plot1 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot1, "no", new Code("1"));
		Entity plot2 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot2, "no", new Code("1"));
		Entity plot3 = EntityBuilder.addEntity(cluster, "plot");
		EntityBuilder.addValue(plot3, "no", new Code("1"));

		iterateExpression("/plot/no", record);
	}

	private List<Node<?>> iterateExpression(String expr, Record record) throws InvalidExpressionException {
		SurveyContext surveyContext = record.getSurveyContext();
		ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
		return expressionEvaluator.evaluateAbsolutePath(record, expr);
	}
}
