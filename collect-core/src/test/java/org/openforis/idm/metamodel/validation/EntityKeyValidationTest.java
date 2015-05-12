package org.openforis.idm.metamodel.validation;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityKeyValidationTest extends ValidationTest {

	@Test
	public void test() {
		Entity plot1 = EntityBuilder.addEntity(cluster, "plot");
		CodeAttribute plotNo1 = EntityBuilder.addValue(plot1, "no", new Code("1"));
		Entity plot2 = EntityBuilder.addEntity(cluster, "plot");
		CodeAttribute plotNo2 = EntityBuilder.addValue(plot2, "no", new Code("1"));
		
		ValidationResults results1 = validate(plotNo1);
		assertTrue(containsEntityKeyValidator(results1.getErrors()));
		
		ValidationResults results2 = validate(plotNo2);
		assertTrue(containsEntityKeyValidator(results2.getErrors()));
		
	}
	
	private boolean containsEntityKeyValidator(List<ValidationResult> results) {
		for (ValidationResult result : results) {
			ValidationRule<?> validator = result.getValidator();
			if (validator instanceof EntityKeyValidator) {
				return true;
			}
		}
		return false;
	}
	
}
