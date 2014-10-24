package org.openforis.idm.model.expression;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;

public class ExpressionReferencePathTest extends AbstractTest {

	@Test
	public void testReferencePath() throws InvalidExpressionException {
		ExpressionFactory ef = cluster.getRecord().getSurveyContext().getExpressionFactory();

		String expression = "$this  <= ../../total_height * 8";
		BooleanExpression expr = ef.createBooleanExpression(expression);
		Set<String> paths = expr.getReferencedPaths();

		Assert.assertEquals(1, paths.size());
		String resultPath = paths.iterator().next();
		Assert.assertEquals("../../total_height", resultPath);

	}
}
