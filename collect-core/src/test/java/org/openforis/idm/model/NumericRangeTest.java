/**
 * 
 */
package org.openforis.idm.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
/**
 * @author M. Togna
 *
 */
public class NumericRangeTest {

	@Test
	public void testIntegerRangePosValues(){
		IntegerRange range = getIntegerRange("4-8");
		assertEquals(4, (int) range.getFrom());
		assertEquals(8, (int) range.getTo());
	}
	
	@Test
	public void testIntegerRangePosValue(){
		IntegerRange range = getIntegerRange("45");
		assertEquals(45, (int) range.getFrom());
		assertEquals(45, (int) range.getTo());
	}
	
	@Test
	public void testIntegerRangeNegValues(){
		IntegerRange range = getIntegerRange("-88--1");
		assertEquals(-88, (int) range.getFrom());
		assertEquals(-1, (int) range.getTo());
	}
	
	@Test
	public void testIntegerRangeNegValue(){
		IntegerRange range = getIntegerRange("-526");
		assertEquals(-526, (int) range.getFrom());
		assertEquals(-526, (int) range.getTo());
	}
	
	@Test
	public void testRealRangePosValues(){
		RealRange range = getRealRange("4.34-8.34");
		assertEquals(4.34, (double) range.getFrom(), 0);
		assertEquals(8.34, (double) range.getTo(), 0);
	}
	
	@Test
	public void testRealRangePosValues2(){
		RealRange range = getRealRange("5-5");
		assertEquals(5, (double) range.getFrom(), 0);
		assertEquals(5, (double) range.getTo(), 0);
	}
	
	@Test
	public void testRealRangePosValue(){
		RealRange range = getRealRange("45.1234");
		assertEquals(45.1234, (double) range.getFrom(), 0);
		assertEquals(45.1234, (double) range.getTo(), 0);
	}
	
	@Test
	public void testRealRangeNegValues2(){
		RealRange range = getRealRange("-35--5");
		assertEquals(-35, (double) range.getFrom(), 0);
		assertEquals(-5, (double) range.getTo(), 0);
	}
	
	@Test
	public void testRealRangeNegValues(){
		RealRange range = getRealRange("-34.777--0.45");
		assertEquals(-34.777, (double) range.getFrom(), 0);
		assertEquals(-0.45, (double) range.getTo(), 0);
	}
	
	@Test
	public void testRealRangeNegValue(){
		RealRange range = getRealRange("-45.5");
		assertEquals(-45.5, (double) range.getFrom(), 0);
		assertEquals(-45.5, (double) range.getTo(), 0);
	}
	
	private RealRange getRealRange(String str){
		return RealRange.parseRealRange(str, null);
	}
	
	private IntegerRange getIntegerRange(String str){
		return IntegerRange.parseIntegerRange(str, null);
	}
	
}
