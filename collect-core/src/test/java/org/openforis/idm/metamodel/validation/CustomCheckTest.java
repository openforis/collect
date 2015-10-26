/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.RealAttribute;

/**
 * @author M. Togna
 * 
 */
public class CustomCheckTest extends ValidationTest {

	@Test
	public void testPass() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 16.0);
		EntityBuilder.addValue(tree, "dbh", 2.0);

		ValidationResults results = validate(totalHeight);
		assertFalse(containsCustomCheck(results.getWarnings()));
	}

	@Test
	public void testPassLtEq() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");

		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 16.0);
		EntityBuilder.addValue(tree, "dbh", 16.0);

		ValidationResults results = validate(totalHeight);
		assertFalse(containsCustomCheck(results.getWarnings()));
	}

	@Test
	public void testPassLtEqWithCondition1() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		EntityBuilder.addValue(tree, "health", new Code("1"));
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 16.0);
		EntityBuilder.addValue(tree, "dbh", 16.0);

		ValidationResults results = validate(totalHeight);
		assertFalse(containsCustomCheck(results.getWarnings()));
	}

	@Test
	public void testFailLtEqWithCondition() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 2.0);
		EntityBuilder.addValue(tree, "dbh", 16.5);
		EntityBuilder.addValue(tree, "health", new Code("1"));
		ValidationResults results = validate(totalHeight);
		assertTrue(containsCustomCheck(results.getWarnings()));
		CustomCheck check = (CustomCheck) results.getWarnings().get(0).getValidator();
		String message = check.getMessageWithEvaluatedExpressions(totalHeight, Locale.ENGLISH.getLanguage());
		System.out.println(message);
	}

	@Test
	public void testCustomMessageWithoutExpressions() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 2.0);
		RealAttribute dbh = EntityBuilder.addValue(tree, "dbh", 16.5);
		EntityBuilder.addValue(tree, "health", new Code("1"));
		ValidationResults results = validate(dbh);
		CustomCheck check = (CustomCheck) results.getWarnings().get(0).getValidator();
		String message = check.getMessageWithEvaluatedExpressions(totalHeight);
		Assert.assertEquals("Unusual relationship between DBH and total height.", message);
	}
	
	@Test
	public void testCustomMessageWithExpressions() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 2.0);
		EntityBuilder.addValue(tree, "dbh", 16.5);
		EntityBuilder.addValue(tree, "health", new Code("1"));
		ValidationResults results = validate(totalHeight);
		CustomCheck check = (CustomCheck) results.getWarnings().get(0).getValidator();
		String message = check.getMessageWithEvaluatedExpressions(totalHeight, Locale.ENGLISH.getLanguage());
		Assert.assertEquals("Unusual relationship between dbh (16.5) and total height (2.0).", message);
	}
	
	@Test
	public void testPassLtEqWithCondition() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		Entity tree = EntityBuilder.addEntity(plot, "tree");
		RealAttribute totalHeight = EntityBuilder.addValue(tree, "total_height", 2.0);
		EntityBuilder.addValue(tree, "dbh", 16.5);
		EntityBuilder.addValue(tree, "health", new Code("2"));
		ValidationResults results = validate(totalHeight);
		assertFalse(containsCustomCheck(results.getWarnings()));
	}

	private boolean containsCustomCheck(List<ValidationResult> results) {
		for (ValidationResult result : results) {
			if (result.getValidator() instanceof CustomCheck) {
				return true;
			}
		}
		return false;
	}

}
