package org.openforis.collect.model.expression;

import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.expression.AbstractExpressionTest;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class IDMDistanceFunctionTest extends AbstractExpressionTest {

	@Test
	public void testNullArguments() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":distance(vehicle_location, vehicle_location)";
		Object distance = evaluateExpression(expr);
		assertNull(distance);
	}
	
	@Test
	public void testSameLocation() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":distance(vehicle_location, vehicle_location)";
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate(592340d, 9293450d, "EPSG:21036"));
		Object distance = evaluateExpression(expr);
		Assert.assertEquals(Double.valueOf(0.0d), distance);
	}
	
//	@Test
	public void testConstantValues() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":distance('SRID=EPSG:21035;POINT(805750 9333820)', 'SRID=EPSG:21036;POINT(592340 9293450)')";
		Object distance = evaluateExpression(expr);
		Assert.assertEquals(Double.valueOf(452663.0592d), round((Double) distance, 10));
	}
	
//	@Test
	public void testSampingPointLocation() throws InvalidExpressionException {
		EntityBuilder.addValue(cluster, "id", new Code("10_114"));
		EntityBuilder.addValue(cluster, "cluster_location", new Coordinate(592340d, 9293450d, "EPSG:21036"));
		EntityBuilder.addValue(cluster, "vehicle_location", new Coordinate(592342d, 9293460d, "EPSG:21036"));
		
		Object distance = evaluateExpression(ExpressionFactory.IDM_PREFIX + ":distance(cluster_location, vehicle_location)");
		Assert.assertEquals(Double.valueOf(10.2010d), round((Double) distance, 6));
	}

//	@Test
	public void testLatlong() throws InvalidExpressionException {
		String expr = ExpressionFactory.IDM_PREFIX + ":latlong(vehicle_location)";
		EntityBuilder.addValue(cluster, "vehicle_location",  new Coordinate(-721008.49d, 14741405.45d, "EPSG:21035"));
		Object object = evaluateExpression(expr);
		Assert.assertEquals(new Coordinate(12.302697080672786d, 41.87118824208946d, "EPSG:4326"), object);
	}
	
	private Double round(double value, int precision) {
		BigDecimal bd = new BigDecimal(value);
		return bd.round(new MathContext(precision)).doubleValue();
	}
}
