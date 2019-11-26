package org.openforis.collect.datacleansing.form.validation;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStep.DataCleansingStepType;
import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingStepValidator extends SimpleValidator<DataCleansingStepForm> {

	private static final String UPDATE_VALUES_FIELD = "updateValues";
	private static final String TITLE_FIELD = "title";
	private static final String QUERY_ID_FIELD = "queryId";
	private static final String TYPE_CODE_FIELD = "typeCode";
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	
	@Override
	public void validateForm(DataCleansingStepForm target, Errors errors) {
		if (validateRequiredFields(errors, TITLE_FIELD, QUERY_ID_FIELD, TYPE_CODE_FIELD) 
				&& validateUniqueness(target, errors)) {
			String typeCode = (String) errors.getFieldValue(TYPE_CODE_FIELD);
			if (typeCode.length() == 1 && typeCode.charAt(0) == DataCleansingStepType.ATTRIBUTE_UPDATE.getCode()) {
				validateUpdateValues(target, errors);
			}
		}
	}

	private void validateUpdateValues(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		AttributeDefinition attrDef = query.getAttributeDefinition();
		
		List<DataCleansingStepValue> updateValues = target.getUpdateValues();
		int updateValueIndex = 0;
		for (DataCleansingStepValue updateValue : updateValues) {
			validateCondition(target, errors, updateValue, updateValueIndex == updateValues.size() - 1);

			switch (updateValue.getUpdateType()) {
			case ATTRIBUTE:
				validateUpdateByAttribute(errors, updateValue, attrDef, updateValueIndex);
				break;
			case FIELD:
				validateUpdateByField(errors, attrDef, updateValueIndex, updateValue);
				break;
			}
			updateValueIndex ++;
		}
	}

	private void validateUpdateByField(Errors errors, AttributeDefinition attrDef, int fixIndex,
			DataCleansingStepValue updateValue) {
		if (StringUtils.isNotBlank(updateValue.getFixExpression())) {
			Object[] args = new Object[] {fixIndex + 1};
			String errorCode = "data_cleansing_step.validation.cannot_specify_field_update_expressions";
			errors.rejectValue(UPDATE_VALUES_FIELD, errorCode, args, messageSource.getMessage(errorCode, args, Locale.ENGLISH));
		}
		List<String> fieldFixExpressions = updateValue.getFieldFixExpressions();
		if (isEmpty(fieldFixExpressions)) {
			String errorCode = "data_cleansing_step.validation.required_fix_expression";
			Object[] args = new Object[] {fixIndex + 1};
			errors.rejectValue(UPDATE_VALUES_FIELD, errorCode, args, messageSource.getMessage(errorCode, args, Locale.ENGLISH));
		} else {
			for (String expr : fieldFixExpressions) {
				if (StringUtils.isNotBlank(expr)) {
					validateValueExpression(errors, attrDef.getParentEntityDefinition(), attrDef, UPDATE_VALUES_FIELD, expr);
				}
			}
		}
	}

	private void validateUpdateByAttribute(Errors errors, DataCleansingStepValue updateValue, AttributeDefinition attrDef,
			int rowIndex) {
		List<String> values = updateValue.getFieldFixExpressions();
		if (! isEmpty(values)) {
			Object[] args = new Object[] {rowIndex + 1};
			String errorCode = "data_cleansing_step.validation.cannot_specify_field_update_expressions";
			String defaultMessage = messageSource.getMessage(errorCode, args, Locale.ENGLISH);
			errors.rejectValue(UPDATE_VALUES_FIELD, errorCode, args, defaultMessage);
		}
		String fixExpression = updateValue.getFixExpression();
		if (StringUtils.isBlank(fixExpression)) {
			String errorCode = "data_cleansing_step.validation.required_fix_expression";
			Object[] args = new Object[] {rowIndex + 1};
			String defaultMessage = messageSource.getMessage(errorCode, args, Locale.ENGLISH);
			errors.rejectValue(UPDATE_VALUES_FIELD, errorCode, args, defaultMessage);
		} else {
			validateValueExpression(errors, attrDef.getParentEntityDefinition(), attrDef, UPDATE_VALUES_FIELD, fixExpression);
		}
	}

	private void validateCondition(DataCleansingStepForm target, Errors errors, DataCleansingStepValue updateValue, boolean lastRow) {
		String condition = updateValue.getCondition();
		if (StringUtils.isBlank(condition)) {
			return;
		}
		CollectSurvey survey = getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		AttributeDefinition attrDef = query.getAttributeDefinition();
		validateBooleanExpression(errors, attrDef.getParentEntityDefinition(), attrDef, UPDATE_VALUES_FIELD, condition);
	}

	private boolean isEmpty(List<String> values) {
		if (values == null) {
			return true;
		}
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				return false;
			}
		}
		return true;
	}

//	@SuppressWarnings("unchecked")
//	private boolean validateFieldFixExpressions(DataCleansingStepForm target, Errors errors) {
//		CollectSurvey survey = sessionManager.getActiveSurvey();
//		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
//		
//		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
//		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
//		
//		boolean valid = true;
//		List<String> fieldExpressions = (List<String>) ((AbstractBindingResult) errors).getRawFieldValue(FIELD_FIX_EXPRESSIONS_FIELD);
//		for (int fieldIdx = 0; fieldIdx < fieldExpressions.size(); fieldIdx++) {
//			String expression = fieldExpressions.get(fieldIdx);
//			if (StringUtils.isNotBlank(expression)) {
//				valid = valid && validateValueExpression(errors, contextNodeDef, thisNodeDef, FIELD_FIX_EXPRESSIONS_FIELD + "[" + fieldIdx + "]", expression);
//			}
//		}
//		return valid;
//	}
//
//	private boolean validateFixExpression(DataCleansingStepForm target, Errors errors) {
//		CollectSurvey survey = sessionManager.getActiveSurvey();
//		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
//		
//		NodeDefinition contextNodeDef = survey.getSchema().getDefinitionById(query.getEntityDefinitionId());
//		NodeDefinition thisNodeDef = survey.getSchema().getDefinitionById(query.getAttributeDefinitionId());
//		
//		String expression = target.getFixExpression();
//		boolean valid = validateValueExpression(errors, contextNodeDef, thisNodeDef, FIX_EXPRESSION_FIELD, expression);
//		return valid;
//	}

	private boolean validateUniqueness(DataCleansingStepForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataCleansingStep> steps = dataCleansingStepManager.loadBySurvey(survey);
		for (DataCleansingStep step : steps) {
			if (! step.getId().equals(target.getId()) 
					&& step.getQueryId().equals(target.getQueryId())
					&& CollectionUtils.deepEquals(step.getUpdateValues(), target.getUpdateValues())
					) {
				rejectDuplicateValue(errors, QUERY_ID_FIELD);
				return false;
			}
		}
		return true;
	}

}
