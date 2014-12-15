package org.openforis.collect.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class LenientRealRangeTest {

	@Test
	public void testParseValidSingleValue() {
		LenientRealRange r = new LenientRealRange("12");
		assertEquals(12, r.getFrom(), 0);
		assertEquals(12, r.getTo(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseValidRange() {
		LenientRealRange r = new LenientRealRange("5-100");
		assertEquals(5, r.getFrom(), 0);
		assertEquals(100, r.getTo(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseValidSingleValueRange() {
		LenientRealRange r = new LenientRealRange("5-5");
		assertEquals(5, r.getFrom().doubleValue(), 0);
		assertEquals(5, r.getTo().doubleValue(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseRealSingleValue() {
		LenientRealRange r = new LenientRealRange("20.5");
		assertEquals(20.5, r.getFrom(), 0);
		assertEquals(20.5, r.getTo(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseRealRange() {
		LenientRealRange r = new LenientRealRange("2.2-20.5");
		assertEquals(2.2, r.getFrom(), 0);
		assertEquals(20.5, r.getTo(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseFromRealToInteger() {
		LenientRealRange r = new LenientRealRange("2.2-5");
		assertEquals(2.2, r.getFrom(), 0);
		assertEquals(5.0, r.getTo(), 0);
		assertTrue(r.isValid());
	}

	@Test
	public void testParseInvalidSingleValue() {
		LenientIntegerRange r = new LenientIntegerRange("x");
		assertNull(r.getFrom());
		assertNull(r.getTo());
		assertFalse(r.isValid());
	}

	@Test
	public void testParseInvalidFromValue() {
		LenientIntegerRange r = new LenientIntegerRange("2-x");
		assertEquals(2, r.getFrom(), 0);
		assertNull(r.getTo());
		assertFalse(r.isValid());
	}

	@Test
	public void testParseInvalidToValue() {
		LenientIntegerRange r = new LenientIntegerRange("x-10");
		assertNull(r.getFrom());
		assertEquals(10, r.getTo(), 0);
		assertFalse(r.isValid());
	}

	@Test
	public void testParseInvalidRange() {
		LenientIntegerRange r = new LenientIntegerRange("a-b");
		assertNull(r.getFrom());
		assertNull(r.getTo());
		assertFalse(r.isValid());
	}
}
