package org.openforis.collect.datacleansing.form.validation;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataCleansingStepValueForm;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.model.CollectSurvey;
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
public class DataCleansingStepValueValidator extends SimpleValidator<DataCleansingStepValueForm> {

	private static final int MAX_FIELD_UPDATE_EXPRESSIONS = 6;
	private static final String FIX_EXPRESSION_FIELD = "fixExpression";
	private static final String FIELD_FIX_EXPRESSIONS_FIELD_PREFIX = "fieldFixExpressions";
	private static final String CONDITION_FIELD = "condition";
	

	@Autowired
	private DataQueryManager dataQueryManager;
	
	@Override
	public void validateForm(DataCleansingStepValueForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		DataQuery query = dataQueryManager.loadById(survey, target.getQueryId());
		AttributeDefinition attrDef = query.getAttributeDefinition();
		int index = target.getIndex();
		
//		if (validateUniqueness(target, errors, step)) {
			validateCondition(target, errors, attrDef);
	
			switch (target.getUpdateType()) {
			case ATTRIBUTE:
				validateUpdateByAttribute(errors, target, attrDef, index);
				break;
			case FIELD:
				validateUpdateByField(errors, target, attrDef, index);
				break;
			}
//		}
	}

	private void validateUpdateByField(Errors errors, DataCleansingStepValueForm updateValue, AttributeDefinition attrDef, int fixIndex) {
		if (StringUtils.isNotBlank(updateValue.getFixExpression())) {
			Object[] args = new Object[] {fixIndex + 1};
			String errorCode = "data_cleansing_step.validation.cannot_specify_attribute_update_expression";
			errors.rejectValue(FIX_EXPRESSION_FIELD, errorCode, args, messageSource.getMessage(errorCode, args, Locale.ENGLISH));
		}
		List<String> fieldFixExpressions = updateValue.getFieldFixExpressions();
		if (isEmpty(fieldFixExpressions)) {
			rejectRequiredFields(errors, getFieldUpdateExpressionFieldNames());
		} else {
			int fieldIndex = 0;
			for (String expr : fieldFixExpressions) {
				if (StringUtils.isNotBlank(expr)) {
					validateValueExpression(errors, attrDef.getParentEntityDefinition(), attrDef, getFieldUpdateExpressionFieldName(fieldIndex), expr);
				}
				fieldIndex ++;
			}
		}
	}

	private void validateUpdateByAttribute(Errors errors, DataCleansingStepValueForm updateValue, AttributeDefinition attrDef,
			int rowIndex) {
		List<String> fieldFixExpressions = updateValue.getFieldFixExpressions();
		if (! isEmpty(fieldFixExpressions)) {
			Object[] args = new Object[] {rowIndex + 1};
			String errorCode = "data_cleansing_step.validation.cannot_specify_field_update_expressions";
			String defaultMessage = messageSource.getMessage(errorCode, args, Locale.ENGLISH);
			int fieldIndex = 0;
			for (String expr : fieldFixExpressions) {
				if (StringUtils.isNotBlank(expr)) {
					errors.rejectValue(getFieldUpdateExpressionFieldName(fieldIndex), errorCode, args, defaultMessage);
				}
				fieldIndex ++;
			}
		}
		String fixExpression = updateValue.getFixExpression();
		if (StringUtils.isBlank(fixExpression)) {
			String errorCode = "data_cleansing_step.validation.required_fix_expression";
			Object[] args = new Object[] {rowIndex + 1};
			String defaultMessage = messageSource.getMessage(errorCode, args, Locale.ENGLISH);
			errors.rejectValue(FIX_EXPRESSION_FIELD, errorCode, args, defaultMessage);
		} else {
			validateValueExpression(errors, attrDef.getParentEntityDefinition(), attrDef, FIX_EXPRESSION_FIELD, fixExpression);
		}
	}

	private void validateCondition(DataCleansingStepValueForm target, Errors errors, AttributeDefinition attrDef) {
		String condition = target.getCondition();
		if (StringUtils.isBlank(condition)) {
			return;
		}
		validateBooleanExpression(errors, attrDef.getParentEntityDefinition(), attrDef, CONDITION_FIELD, condition);
	}
	
	private String[] getFieldUpdateExpressionFieldNames() {
		String[] result = new String[MAX_FIELD_UPDATE_EXPRESSIONS];
		for (int i = 0; i < MAX_FIELD_UPDATE_EXPRESSIONS; i++) {
			result[i] = getFieldUpdateExpressionFieldName(i);
		}
		return result;
	}
	
//	private boolean validateUniqueness(DataCleansingStepValueForm target, Errors errors, DataCleansingStep step) {
//		List<DataCleansingStepValue> updateValues = step.getUpdateValues();
//		int index = 0;
//		for (DataCleansingStepValue stepValue : updateValues) {
//			DataCleansingStepValue targetStepValue = new DataCleansingStepValue();
//			target.copyTo(targetStepValue);
//			if (targetStepValue.deepEquals(stepValue) && target.getIndex() != index) {
//				switch(stepValue.getUpdateType()) {
//				case ATTRIBUTE:
//					rejectDuplicateValues(errors, Arrays.asList(CONDITION_FIELD, FIX_EXPRESSION_FIELD));
//					break;
//				case FIELD:
//					List<String> fields = new ArrayList<String>(Arrays.asList(CONDITION_FIELD));
//					fields.addAll(getFieldUpdateExpressionFieldNames());
//					rejectDuplicateValues(errors, fields);
//					break;
//				}
//			}
//			index ++;
//		}
//		return true;
//	}

	private String getFieldUpdateExpressionFieldName(int fieldIndex) {
		return FIELD_FIX_EXPRESSIONS_FIELD_PREFIX + "[" + fieldIndex + "]";
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

}
