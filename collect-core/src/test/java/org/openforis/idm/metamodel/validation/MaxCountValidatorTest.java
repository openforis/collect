package org.openforis.idm.metamodel.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class MaxCountValidatorTest extends ValidationTest {

	@Test
	// (expected=ArrayIndexOutOfBoundsException.class)
	public void testMultipleSingleAttribute() {
		updateMaxCount(cluster, "crew_no");
		EntityBuilder.addValue(cluster, "crew_no", 1);
		EntityBuilder.addValue(cluster, "crew_no", 2);
		//ValidationResults results = validate(cluster);
		ValidationResultFlag result = validator.validateMaxCount(cluster, "crew_no");
		assertFalse(result.isOk());
//		List<ValidationResult> errors = results.getErrors();
//		assertTrue(containsMaxCountError(errors, "crew_no"));
	}

	@Test
	public void testMultipleAttributeLessThanMax() {
		updateMaxCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "A");
		EntityBuilder.addValue(cluster, "map_sheet", "B");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "map_sheet");
		assertTrue(result.isOk());
	}

	@Test
	public void testMultipleAttributeMax() {
		updateMaxCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "A");
		EntityBuilder.addValue(cluster, "map_sheet", "B");
		EntityBuilder.addValue(cluster, "map_sheet", "C");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "map_sheet");
		assertTrue(result.isOk());
	}

	@Test
	// (expected=ArrayIndexOutOfBoundsException.class)
	public void testMultipleAttributeMoreThanMax() {
		updateMaxCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "A");
		EntityBuilder.addValue(cluster, "map_sheet", "B");
		EntityBuilder.addValue(cluster, "map_sheet", "C");
		EntityBuilder.addValue(cluster, "map_sheet", "D");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "map_sheet");
		assertFalse(result.isOk());
	}

	@Test
	// (expected=ArrayIndexOutOfBoundsException.class)
	public void testMultipleSingleEntity() {
		Entity plot = EntityBuilder.addEntity(cluster, "plot");
		updateMaxCount(plot, "centre");
		EntityBuilder.addEntity(plot, "centre");
		EntityBuilder.addEntity(plot, "centre");
		ValidationResultFlag result = validator.validateMaxCount(plot, "centre");
		assertFalse(result.isOk());
	}

	@Test
	public void testMultipleEntityLessThanMax() {
		updateMaxCount(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "time_study");
		assertTrue(result.isOk());
	}

	@Test
	public void testMultipleEntityMax() {
		updateMaxCount(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "time_study");
		assertTrue(result.isOk());
	}

	@Test
	// (expected=ArrayIndexOutOfBoundsException.class)
	public void testMultipleEntityMoreThanMax() {
		updateMaxCount(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		EntityBuilder.addEntity(cluster, "time_study");
		ValidationResultFlag result = validator.validateMaxCount(cluster, "time_study");
		assertFalse(result.isOk());
	}

//	protected boolean containsMaxCountError(List<ValidationResult> errors, String name) {
//		for (ValidationResult result : errors) {
//			ValidationRule validator = result.getValidator();
//			if (validator instanceof MaxCountValidator) {
//				MaxCountValidator v = (MaxCountValidator) validator;
//				NodeDefinition nodeDefinition = v.getNodeDefinition();
//				if (nodeDefinition.getName().equals(name)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}

	private void updateMaxCount(Entity entity, String childName) {
		NodeDefinition childDef = entity.getDefinition().getChildDefinition(childName);
		try {
			String expr = childDef.getMaxCountExpression();
			Integer count = null;
			if (StringUtils.isNotBlank(expr)) {
				Number val = expressionEvaluator.evaluateNumericValue(entity, null, expr);
				count = val.intValue();
			} else if (! childDef.isMultiple()) {
				count = 1;
			}
			entity.setMaxCount(childDef, count);
		} catch (InvalidExpressionException e) {
			throw new RuntimeException(e);
		}
	}

}
