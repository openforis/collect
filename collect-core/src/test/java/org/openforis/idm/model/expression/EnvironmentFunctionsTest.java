package org.openforis.idm.model.expression;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EnvironmentFunctionsTest extends AbstractExpressionTest {

	@Test
	public void testRunningOnDesktop() throws InvalidExpressionException {
		assertTrue(evaluateBooleanExpression(cluster, null, "env:desktop()"));
	}
	
	@Test
	public void testNotRunningOnMobile() throws InvalidExpressionException {
		assertTrue(evaluateBooleanExpression(cluster, null, "not(env:mobile())"));
	}
}
