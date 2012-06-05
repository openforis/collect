package org.openforis.collect.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class LenientIntegerRangeTest {

	@Test
	public void testParseValidSingleValue() {
		LenientIntegerRange r = new LenientIntegerRange("12");
		assertEquals(12, r.getFrom().intValue());
		assertEquals(12, r.getTo().intValue());
		assertTrue(r.isValid());
	}

	@Test
	public void testParseValidRange() {
		LenientIntegerRange r = new LenientIntegerRange("5-100");
		assertEquals(5, r.getFrom().intValue());
		assertEquals(100, r.getTo().intValue());
		assertTrue(r.isValid());
	}

	@Test
	public void testParseValidSingleValueRange() {
		LenientIntegerRange r = new LenientIntegerRange("5-5");
		assertEquals(5, r.getFrom().intValue());
		assertEquals(5, r.getTo().intValue());
		assertTrue(r.isValid());
	}

	@Test
	public void testParseRealSingleValue() {
		LenientIntegerRange r = new LenientIntegerRange("20.5");
		assertNull(r.getFrom());
		assertNull(r.getTo());
		assertFalse(r.isValid());
	}

	@Test
	public void testParseRealRange() {
		LenientIntegerRange r = new LenientIntegerRange("2.2-20.5");
		assertNull(r.getFrom());
		assertNull(r.getTo());
		assertFalse(r.isValid());
	}

	@Test
	public void testParseFromRealToInteger() {
		LenientIntegerRange r = new LenientIntegerRange("2.2-5");
		assertNull(r.getFrom());
		assertEquals(5, r.getTo().intValue());
		assertFalse(r.isValid());
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
