/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.model.expression.ExpressionFactory.REGEX_PREFIX;

import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author S. Ricci
 *
 */
public class RegExFunctionsTest extends AbstractExpressionTest {

	@Test
	public void testNullValue() throws InvalidExpressionException{
		assertFalse((Boolean) evaluateExpression(cluster, REGEX_PREFIX + ":test(id, '[a-z]*')"));
	}
	
	@Test
	public void testValidValue() throws InvalidExpressionException{
		assertTrue((Boolean) evaluateExpression(cluster, REGEX_PREFIX + ":test('abc', '[a-z]*')"));
	}
	
	@Test
	public void testInvalidValue() throws InvalidExpressionException{
		assertFalse((Boolean) evaluateExpression(cluster, REGEX_PREFIX + ":test('abc1', '[a-z]*')"));
	}
	
	@Test
	public void testAttributeValue() throws InvalidExpressionException{
		EntityBuilder.addValue(cluster, "id", new Code("10_114"));
		assertTrue((Boolean) evaluateExpression(cluster, REGEX_PREFIX + ":test(id, '[0-9]+_[0-9]+')"));
	}
	
}
