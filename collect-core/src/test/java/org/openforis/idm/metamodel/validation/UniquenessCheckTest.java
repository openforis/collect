/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.TextAttribute;

/**
 * @author M. Togna
 * 
 */
public class UniquenessCheckTest extends ValidationTest {

	private static final String MAP_SHEET = "map_sheet";

	@Test
	public void testUniqueSingleMapSheet() {
		TextAttribute mapSheet1 = EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		ValidationResults results = validate(mapSheet1);
		Assert.assertFalse(containsUniquenessError(results.getErrors(), MAP_SHEET));
	}

	@Test
	public void testUniqueMapSheet() {
		TextAttribute mapSheet1 = EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom2");
		ValidationResults results = validate(mapSheet1);
		Assert.assertFalse(containsUniquenessError(results.getErrors(), MAP_SHEET));
	}

	@Test
	public void testUniqueMapSheet2() {
		TextAttribute mapSheet1 = EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom2");
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom2");
		ValidationResults results = validate(mapSheet1);
		Assert.assertFalse(containsUniquenessError(results.getErrors(), MAP_SHEET));
	}

	@Test
	public void testNotUniqueMapSheet() {
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom2");
		TextAttribute mapSheet3 = EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		ValidationResults results = validate(mapSheet3);
		Assert.assertTrue(containsUniquenessError(results.getErrors(), MAP_SHEET));
	}

	@Test
	public void testNotUniqueMapSheet2() {
		TextAttribute mapSheet1 = EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		EntityBuilder.addValue(cluster, MAP_SHEET, "TomTom1");
		ValidationResults results = validate(mapSheet1);
		Assert.assertTrue(containsUniquenessError(results.getErrors(), MAP_SHEET));
	}

	private boolean containsUniquenessError(List<ValidationResult> results, String name) {
		for (ValidationResult result : results) {
			ValidationRule<?> validator = result.getValidator();
			if (validator instanceof UniquenessCheck) {
				return true;
			}
		}
		return false;
	}
}
