/**
 * 
 */
package org.openforis.idm.metamodel.expression;

import static org.junit.Assert.assertTrue;
import static org.openforis.idm.testfixture.SurveyBuilder.attributeDef;
import static org.openforis.idm.testfixture.SurveyBuilder.entityDef;
import static org.openforis.idm.testfixture.SurveyBuilder.survey;

import org.junit.Test;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;

/**
 * @author S. Ricci
 *
 */
public class ExpressionValidatorTest {
	
//	@Test
//	@Ignore
//	public void testCircularDependencies() {
//		Schema schema = survey.getSchema();
//		EntityDefinition clusterDefn = schema.getRootEntityDefinition("cluster");
//		NodeDefinition regionDefn = clusterDefn.getChildDefinition("region");
//		
//		assertTrue(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "region").isError());
//		assertTrue(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "region_district").isError());
//		assertTrue(validator.validateCircularReferenceAbsence(clusterDefn, regionDefn, "district").isOk());
//	}
	
	@Test
	public void testInvalidExpression() {
		Survey survey = survey(
				attributeDef("region")
		);
		assertInvalidExpression(survey, "root/region", "1++1/^fhdj)(_");
	}
	
	@Test
	public void testIndependentValidCoreFunction() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertValidExpression(survey, "root/region", "true()");
	}

	@Test
	public void testIndependentNonExistingCoreFunction() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertInvalidExpression(survey, "root/region", "wrong()");
	}

	@Test
	public void testConstantNumber() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertValidExpression(survey, "root/region", "10");
	}

	@Test
	public void testConstantString() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertValidExpression(survey, "root/region", "'test'");
	}

	@Test
	public void testContextVariable() {
		Survey survey = survey(
			entityDef("plot", 
				attributeDef("accessible"),
				entityDef("tree", 
					attributeDef("status"),
					attributeDef("species")
				).multiple()
			),
			entityDef("enumerated",
				attributeDef("status"),
				attributeDef("count")
			).multiple()
		);
		assertValidExpression(survey, "root/enumerated/count", "count(parent()/plot/tree[status=$this/parent()/status])");
		assertValidExpression(survey, "root/enumerated/count", "count(parent()/plot/tree[status=$context/status])");
	}
	
	@Test
	public void testExistingPath() {
		Survey survey = survey(
			attributeDef("region"),
			entityDef("time_study", 
					attributeDef("date"),
					attributeDef("time")
			)
		);
		assertValidExpression(survey, "root/region", "time_study/date");
	}


	@Test
	public void testInvalidPath() {
		Survey survey = survey(
			attributeDef("region"),
			entityDef("time_study", 
					attributeDef("date"),
					attributeDef("time")
			)
		);
		assertInvalidExpression(survey, "root/region", "non_existing/path");
	}
	
	@Test
	public void testIndependentCustomFunction() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertValidExpression(survey, "root/region", "idm:currentDate()");
	}
	
	@Test
	public void testCustomFunctionWrongArgumentCount() {
		Survey survey = survey(
			attributeDef("region")
		);
		assertInvalidExpression(survey, "root/region", "math:max(1, 2)");
	}
	
	@Test
	public void testMissingSpatialReferenceSystemForLatLongFunction() {
		Survey survey = survey(
			attributeDef("location")
		);
		assertInvalidExpression(survey, "root/location", "idm:latlong($this)");
	}
	
	@Test
	public void testValidAttributeInSampingPointDataFunction() {
		Survey survey = survey(
			attributeDef("location")
		);
		samplingPointDataAttribute(survey, "valid_attribute");
		assertValidExpression(survey, "root/location", "idm:samplingPointData('valid_attribute', '1')");
	}
	
	@Test
	public void testInvalidAttributeInSampingPointDataFunction() {
		Survey survey = survey(
			attributeDef("location")
		);
		samplingPointDataAttribute(survey, "valid_attribute");
		assertInvalidExpression(survey, "root/location", "idm:samplingPointData('invalid_attribute', '1')");
	}
	
	private void assertValidExpression(Survey survey, String contextNodeDefPath, String expression) {
		assertTrue(validate(survey, contextNodeDefPath, expression).isOk());
	}

	private void assertInvalidExpression(Survey survey, String contextNodeDefPath, String expression) {
		assertTrue(validate(survey, contextNodeDefPath, expression).isError());
	}

	private ExpressionValidationResult validate(Survey survey, String contextNodeDefPath, String expression) {
		ExpressionValidator validator = new ExpressionValidator(survey.getContext().getExpressionFactory());
		ExpressionValidationResult result = validator.validateValueExpression(survey.getSchema().getDefinitionByPath(contextNodeDefPath), expression);
		return result;
	}
	
	private void samplingPointDataAttribute(Survey survey, String name) {
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		if (referenceDataSchema == null) {
			referenceDataSchema = new ReferenceDataSchema();
			survey.setReferenceDataSchema(referenceDataSchema);
		}
		SamplingPointDefinition samplingPointDefinition = referenceDataSchema.getSamplingPointDefinition();
		samplingPointDefinition.addAttribute(name);
	}
	

}
