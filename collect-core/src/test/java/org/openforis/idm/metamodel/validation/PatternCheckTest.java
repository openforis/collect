/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.TextAttribute;

/**
 * @author M. Togna
 * 
 */
public class PatternCheckTest extends ValidationTest {

	@Test
	public void testUpperCaseLetterPatternPass() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		TextAttribute subplot = EntityBuilder.addValue(plot, "subplot", "D");
		ValidationResults results = validate(subplot);
		assertFalse(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testUpperCaseLetterPatternFail() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		TextAttribute subplot = EntityBuilder.addValue(plot, "subplot", "d");
		ValidationResults results = validate(subplot);
		assertTrue(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testUpperCaseLetterPatternFail2() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		TextAttribute subplot = EntityBuilder.addValue(plot, "subplot", "4");
		ValidationResults results = validate(subplot);
		assertTrue(containsPatternCheck(results.getErrors()));
	}

	// household id: (X-)?[1-9][0-9]*
	@Test
	public void testValidPattern() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "X-1");
		ValidationResults results = validate(id);
		assertFalse(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testValidPattern2() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "X-102357");
		ValidationResults results = validate(id);
		assertFalse(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testValidPattern3() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "102357");
		ValidationResults results = validate(id);
		assertFalse(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testInvalidPattern() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "x-102357");
		ValidationResults results = validate(id);
		assertTrue(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testInvalidPattern2() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "X-1d02357");
		ValidationResults results = validate(id);
		assertTrue(containsPatternCheck(results.getErrors()));
	}

	@Test
	public void testInvalidPattern3() {
		TextAttribute id = EntityBuilder.addValue(household, "id", "X-");
		ValidationResults results = validate(id);
		assertTrue(containsPatternCheck(results.getErrors()));
	}

	private boolean containsPatternCheck(List<ValidationResult> results) {
		for (ValidationResult result : results) {
			if (result.getValidator() instanceof PatternCheck) {
				return true;
			}
		}
		return false;
	}
}
