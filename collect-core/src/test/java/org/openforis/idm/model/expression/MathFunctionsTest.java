/**
 * 
 */
package org.openforis.idm.model.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;

/**
 * @author S. Ricci
 *
 */
public class MathFunctionsTest extends AbstractExpressionTest {

	@Test
	public void testConstants() throws InvalidExpressionException{
		Double pi = (Double) evaluateExpression(cluster, "math:PI()");
		assertEquals(Double.valueOf(Math.PI), pi);
	}
	
	@Test
	public void testDegreesToRadiansToDegrees() throws InvalidExpressionException{
		Double angle = 45d;
		Double radians = (Double) evaluateExpression(cluster, String.format("math:rad(%f)", angle));
		assertEquals(Double.valueOf(Math.toRadians(angle)), radians);
		Double degrees = (Double) evaluateExpression(cluster, String.format("math:deg(%f)", radians));
		//assertEquals(Double.valueOf(Math.toDegrees(radians)), degrees);
		assertNotNull(degrees);
	}
	
	@Test
	public void testSin() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:sin(45)");
			assertNotNull(value);
			double val = ((Double) value).doubleValue();
			assertTrue(val > 0.70d && val < 0.71);
		}
		{
			double angle = 90d;
			EntityBuilder.addValue(cluster, "plot_direction",  angle);
			Object value = evaluateExpression(cluster, "math:sin(plot_direction)");
			assertEquals(Double.valueOf(1d), (Double) value);
		}
	}
	
	@Test
	public void testTan() throws InvalidExpressionException {
		{
			Object value = evaluateExpression(cluster, "math:tan(45)");
			assertNotNull(value);
			double val = ((Double) value).doubleValue();
			assertTrue(val > 0.99 && val <= 1.00);
		}
		{
			double angle = 80d;
			EntityBuilder.addValue(cluster, "plot_direction",  angle);
			Object value = evaluateExpression(cluster, "math:tan(plot_direction)");
			assertNotNull(value);
			double val = ((Double) value).doubleValue();
			assertTrue(val > 5.67 && val < 5.68);
		}
		{
			Object value = evaluateExpression(cluster, "math:tan(plot/centre/dist)");
			assertNull(value);
		}
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
}
