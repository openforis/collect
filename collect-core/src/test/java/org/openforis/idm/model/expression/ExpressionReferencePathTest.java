package org.openforis.idm.model.expression;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.AbstractTest;

public class ExpressionReferencePathTest extends AbstractTest {

	@Test
	public void testReferencePath() throws InvalidExpressionException {
		String expression = "$this  < total_height";

		Set<String> paths = expressionEvaluator.determineReferencedPaths(expression);

		Assert.assertEquals(1, paths.size());
		
		String resultPath = paths.iterator().next();
		Assert.assertEquals("total_height", resultPath);
	}
}
