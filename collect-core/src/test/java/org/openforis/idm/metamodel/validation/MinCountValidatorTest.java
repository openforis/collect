package org.openforis.idm.metamodel.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class MinCountValidatorTest extends ValidationTest {

	@Test
	public void testMissingOptionalSingleAttribute() {
		updateMinCount(cluster, "crew_no");
//		ValidationResults results = validate(cluster);
		ValidationResultFlag result = validator.validateMinCount(cluster, "crew_no");
		assertTrue(result.isOk());
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "crew_no"));
	}

	@Test
	public void testMissingRequiredSingleAttribute() {
		updateMinCount(cluster, "region");
		ValidationResultFlag result = validator.validateMinCount(cluster, "region");
		assertFalse(result.isOk());
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertTrue(containsMinCountError(errors, "region"));
	}

	@Test
	public void testSpecifiedRequiredSingleAttribute() {
		updateMinCount(cluster, "region");
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		ValidationResultFlag result = validator.validateMinCount(cluster, "region");
		assertTrue(result.isOk());
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "region"));
	}

	@Test
	public void testMissingMultipleRequiredAttribute() {
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "map_sheet"));
//		
		updateMinCount(cluster, "map_sheet");
		ValidationResultFlag result = validator.validateMinCount(cluster, "map_sheet");
		assertTrue(result.isOk());
	}

	@Test
	public void testEmptyMultipleRequiredAttribute() {
		updateMinCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "");
		EntityBuilder.addValue(cluster, "map_sheet", "");
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "map_sheet"));
		
		ValidationResultFlag result = validator.validateMinCount(cluster, "map_sheet");
		assertTrue(result.isOk());
	}

	@Test
	public void testTooFewMultipleRequiredAttribute() {
		updateMinCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "");
		EntityBuilder.addValue(cluster, "map_sheet", "567");
		
		ValidationResultFlag result = validator.validateMinCount(cluster, "map_sheet");
		assertTrue(result.isOk());
		
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "map_sheet"));
	}

	@Test
	public void testMultipleRequiredAttribute() {
		updateMinCount(cluster, "map_sheet");
		EntityBuilder.addValue(cluster, "map_sheet", "123");
		EntityBuilder.addValue(cluster, "map_sheet", "567");
		
		ValidationResultFlag result = validator.validateMinCount(cluster, "map_sheet");
		assertTrue(result.isOk());
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "map_sheet"));
	}

	@Test
	public void testMissingRequiredMultipleEntity() {
		updateMinCount(cluster, "time_study");
		ValidationResultFlag result = validator.validateMinCount(cluster, "time_study");
		assertFalse(result.isOk());
		
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertTrue(containsMinCountError(errors, "time_study"));
	}

	@Test
	public void testEmptyRequiredMultipleEntity() {
		updateMinCount(cluster, "time_study");
		Entity timeStudy = EntityBuilder.addEntity(cluster, "time_study");
		EntityBuilder.addValue(timeStudy, "date", (Date) null);
		
		ValidationResultFlag result = validator.validateMinCount(cluster, "time_study");
		assertFalse(result.isOk());
		
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertTrue(containsMinCountError(errors, "time_study"));
	}

	@Test
	public void testSpecifiedRequiredMultipleEntity() {
		updateMinCount(cluster, "time_study");
		Entity timeStudy = EntityBuilder.addEntity(cluster, "time_study");
		EntityBuilder.addValue(timeStudy, "date", new Date(2012, 1, 1));
//		ValidationResults results = validate(cluster);
//		List<ValidationResult> errors = results.getErrors();
//		assertFalse(containsMinCountError(errors, "time_study"));
		
		ValidationResultFlag result = validator.validateMinCount(cluster, "time_study");
		assertTrue(result.isOk());
	}

//	private boolean containsMinCountError(List<ValidationResult> errors, String name) {
//		for (ValidationResult result : errors) {
//			ValidationRule<?> validator = result.getValidator();
//			if (validator instanceof MinCountValidator) {
//				MinCountValidator v = (MinCountValidator) validator;
//				NodeDefinition nodeDefinition = v.getNodeDefinition();
//				if (nodeDefinition.getName().equals(name)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
	private void updateMinCount(Entity entity, String childName) {
		NodeDefinition childDef = entity.getDefinition().getChildDefinition(childName);
		try {
			String expr = childDef.getMinCountExpression();
			int count;
			if (StringUtils.isNotBlank(expr)) {
				Number val = expressionEvaluator.evaluateNumericValue(entity, null, expr);
				count = val.intValue();
			} else {
				count = 0;
			}
			entity.setMinCount(childDef, count);
		} catch (InvalidExpressionException e) {
			throw new RuntimeException(e);
		}
	}
}
