/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author S. Ricci
 *
 */
public class MathFunctionsTest extends AbstractExpressionTest {

	private static final Double DEFAULT_TOLERACE = 1E-5d;

	@Test
	public void testConstants() throws InvalidExpressionException{
		Double pi = (Double) evaluateExpression(cluster, "math:PI()");
		assertEquals(Double.valueOf(Math.PI), pi);
	}
	
	@Test
	public void testDegreesToRadiansToDegrees() throws InvalidExpressionException{
		Double angle = 45d;
		Double radians = (Double) evaluateExpression(cluster, String.format(Locale.ENGLISH, "math:rad(%f)", angle));
		assertEquals(Double.valueOf(Math.toRadians(angle)), radians);
		Double degrees = (Double) evaluateExpression(cluster, String.format(Locale.ENGLISH, "math:deg(%f)", radians));
		//assertEquals(Double.valueOf(Math.toDegrees(radians)), degrees);
		assertNotNull(degrees);
	}
	
	@Test
	public void testSin() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:sin(0)");
			assertEquals(Double.valueOf(0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:sin(90)");
			assertEquals(Double.valueOf(1d), (Double) value);
		}
		{
			double angle = 90d;
			EntityBuilder.addValue(cluster, "plot_direction",  angle);
			Object value = evaluateExpression(cluster, "math:sin(plot_direction)");
			assertEquals(Double.valueOf(1d), (Double) value);
		}
	}
	
	@Test
	public void testCos() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:cos(0)");
			assertEquals(Double.valueOf(1.0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:cos(90)");
			assertAlmostEquals(0d, (Double) value);
		}
	}
	
	@Test
	public void testTan() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:tan(0)");
			assertEquals(Double.valueOf(0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:tan(45)");
			assertNotNull(value);
			double val = ((Double) value).doubleValue();
			assertAlmostEquals(1d, val);
		}
		{
			double angle = 80d;
			EntityBuilder.addValue(cluster, "plot_direction",  angle);
			Object value = evaluateExpression(cluster, "math:tan(plot_direction)");
			assertAlmostEquals(5.671281d, (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:tan(plot/centre/dist)");
			assertNull(value);
		}
	}
	
	@Test
	public void testASin() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:asin(0)");
			assertEquals(Double.valueOf(0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:deg(math:asin(1))");
			assertEquals(Double.valueOf(90d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:deg(math:asin(-1))");
			assertEquals(Double.valueOf(-90d), (Double) value);
		}
	}
	
	@Test
	public void testACos() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:deg(math:acos(0))");
			assertEquals(Double.valueOf(90d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:acos(1)");
			assertEquals(Double.valueOf(0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:deg(math:acos(-1))");
			assertEquals(Double.valueOf(180d), (Double) value);
		}
	}
	
	@Test
	public void testATan() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:deg(math:atan(0))");
			assertEquals(Double.valueOf(0d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:deg(math:atan(1))");
			assertEquals(Double.valueOf(45d), (Double) value);
		}
		{
			Object value = evaluateExpression(cluster, "math:deg(math:atan(-1))");
			assertEquals(Double.valueOf(-45d), (Double) value);
		}
	}
	
	@Test
	public void testSqrt() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:sqrt(16)");
			assertEquals(Double.valueOf(4), value);
		}
		{
			Object value = evaluateExpression(cluster, "math:sqrt(20)");
			assertAlmostEquals(4.47213595d, (Double) value);
		}
	}
	
	@Test
	public void testSqrtWithEmptyValue() throws InvalidExpressionException {
		boolean value = evaluateBooleanExpression(cluster, null, "idm:blank(math:sqrt(plot_direction))");
		assertTrue(value);
	}
	
	@Test
	public void testPow() throws InvalidExpressionException {
		Object value = evaluateExpression(cluster, "math:pow(2, 10)");
		assertEquals(Double.valueOf(1024), value);
	}
	
	@Test
	public void testMinAndMax() throws InvalidExpressionException {
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("4"));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", (Code) null);
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
		}
		Object min = evaluateExpression(cluster, cluster, "math:min(plot/no)");
		assertEquals("1", min);
		
		Object max = evaluateExpression(cluster, cluster, "math:max(plot/no)");
		assertEquals("4", max);
	}
	
	@Test
	public void testRandom() throws InvalidExpressionException {
		Object value = evaluateExpression(cluster, "math:random()");
		assertTrue(value instanceof Double && ((Double) value < 1 && (Double) value >= 0));
	}
	
	private static void assertAlmostEquals(double expected, double value) {
		assertAlmostEquals(expected, value, DEFAULT_TOLERACE);
	}
	
	private static void assertAlmostEquals(Double expected, Double value, double tolerance) {
		assertTrue(expected + tolerance >= value && expected - tolerance <= value);
	}
}
