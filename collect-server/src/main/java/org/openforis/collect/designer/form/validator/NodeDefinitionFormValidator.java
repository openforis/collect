package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.designer.form.NodeDefinitionFormObject.MULTIPLE_FIELD;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.NodeDefinitionFormObject.RelevanceType;
import org.openforis.collect.designer.form.NodeDefinitionFormObject.RequirenessType;
import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.designer.viewmodel.NodeDefinitionVM;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.manager.validation.SurveyValidator;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 * 
 */
public abstract class NodeDefinitionFormValidator extends FormValidator {

	protected static final String 	NODE_NAME_ALREADY_DEFINED_MESSAGE_KEY = "survey.schema.node.validation.name_already_defined";
	private static final String 	BACKGROUND_COLOR_FORMAT_ERROR_MESSAGE_KEY = "survey.schema.node.validation.background_color";
			
	protected static final String 	DESCRIPTION_FIELD = "description";
	protected static final String 	NAME_FIELD = "name";
	protected static final String 	KEY_FIELD = "key";
	protected static final String 	MIN_COUNT_EXPRESSION_FIELD = "minCountExpression";
	protected static final String 	MAX_COUNT_EXPRESSION_FIELD = "maxCountExpression";
	protected static final String 	TAB_NAME_FIELD = "tabName";
	protected static final String 	REQUIRENESS_TYPE_FIELD = "requirenessType";
	protected static final String 	REQUIRED_EXPR_FIELD = "requiredWhenExpression";
	protected static final String 	RELEVANCE_TYPE_FIELD = "relevanceType";
	protected static final String 	RELEVANT_EXPR_FIELD = "relevantExpression";
	protected static final String 	COLUMN_FIELD = "column";
	protected static final String 	COLUMN_SPAN_FIELD = "columnSpan";
	protected static final String 	BACKGROUND_COLOR = "backgroundColor";
	protected static final String 	BACKGROUND_TRANSPARENCY = "backgroundTransparency";
	
	protected static final int 		MAX_COUNT_MIN_VALUE = 2;
	
	private static final String 	HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
		
		NodeDefinition contextNode = getEditedNode(ctx);
		
		boolean key = getValueWithDefault(ctx, KEY_FIELD, false);
		String requirenessTypeVal = getValueWithDefault(ctx, REQUIRENESS_TYPE_FIELD, RequirenessType.ALWAYS_REQUIRED.name());
		RequirenessType requirenessType = RequirenessType.valueOf(requirenessTypeVal);
		
		if (!key && requirenessType == RequirenessType.REQUIRED_WHEN) {
			if (validateRequired(ctx, REQUIRED_EXPR_FIELD)) {
				validateBooleanExpressionField(ctx, contextNode, REQUIRED_EXPR_FIELD);
			}
		}
		
		String relevanceTypeVal = getValueWithDefault(ctx, RELEVANCE_TYPE_FIELD, RelevanceType.ALWAYS_RELEVANT.name());
		RelevanceType relevanceType = RelevanceType.valueOf(relevanceTypeVal);
		if (relevanceType == RelevanceType.RELEVANT_WHEN) {
			if (validateRequired(ctx, RELEVANT_EXPR_FIELD)) {
				validateBooleanExpressionField(ctx, contextNode, RELEVANT_EXPR_FIELD);
			}
		}
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if (multiple != null && multiple.booleanValue()) {
			validateValueExpressionField(ctx, contextNode, MIN_COUNT_EXPRESSION_FIELD);
			validateValueExpressionField(ctx, contextNode, MAX_COUNT_EXPRESSION_FIELD);
			validateMaxCount(ctx);
		}
		
		validateColumn(ctx);
		
		validateRegEx(ctx, HEX_PATTERN, BACKGROUND_COLOR, BACKGROUND_COLOR_FORMAT_ERROR_MESSAGE_KEY);
		if (validateRequired(ctx, BACKGROUND_TRANSPARENCY)) {
			validateGreaterThan(ctx, BACKGROUND_TRANSPARENCY, 0, false);
			validateLessThan(ctx, BACKGROUND_TRANSPARENCY, 100, false);
		}
	}

	protected boolean validateName(ValidationContext ctx) {
		boolean valid = validateRequired(ctx, NAME_FIELD);
		if (valid) {
			EntityDefinition parentEntity = getParentEntity(ctx);
			String name = getValue(ctx, NAME_FIELD);
			CollectSurvey survey = getEditedNode(ctx).getSurvey();
			if (survey.getTarget() == SurveyTarget.COLLECT_EARTH) {
				CollectEarthSurveyValidator collectEarthSurveyValidator = new CollectEarthSurveyValidator();
				if (parentEntity == null) {
					valid = collectEarthSurveyValidator.validateRootEntityName(name);
					if (! valid) {
						addInvalidMessage(ctx, NAME_FIELD, Labels.getLabel("survey.validation.collect_earth.invalid_root_entity_name"));
					}
				} else {
					valid = collectEarthSurveyValidator.validateNodeName(name);
					if (! valid) {
						addInvalidMessage(ctx, NAME_FIELD, Labels.getLabel("survey.validation.collect_earth.invalid_node_name"));
					}
				}
			} else {
				valid = super.validateInternalName(ctx, NAME_FIELD);
				if (valid) {
					SurveyValidator surveyValidator = new SurveyValidator();
					valid = surveyValidator.validateNodeNameMaxLength(parentEntity, name);
					if (! valid) {
						String errorMessage = Labels.getLabel("survey.validation.node.name.error.max_length_exceeded", 
								new Object[]{surveyValidator.generateFullInternalName(parentEntity, name).length(), SurveyValidator.MAX_NODE_NAME_LENGTH});
						addInvalidMessage(ctx, NAME_FIELD, errorMessage);
					}
				}
			}
			if (valid) {
				valid = validateNameUniqueness(ctx);
			}
		}
		return valid;
	}

	protected boolean validateNameUniqueness(ValidationContext ctx) {
		NodeDefinition editedNode = getEditedNode(ctx);
		String name = (String) getValue(ctx, NAME_FIELD);
		if (!isNameUnique(ctx, editedNode, name)) {
			String message = Labels.getLabel(NODE_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}

	protected boolean isNameUnique(ValidationContext ctx, NodeDefinition defn, String name) {
		EntityDefinition parentDefn = getParentEntity(ctx);
		NodeDefinition nodeInPath = null;
		try {
			if (parentDefn != null) {
				nodeInPath = parentDefn.getChildDefinition(name);
			} else {
				Schema schema = defn.getSchema();
				nodeInPath = schema.getRootEntityDefinition(name);
			}
		} catch ( IllegalArgumentException e ) {
			//sibling not found
		}
		return nodeInPath == null || nodeInPath.getId() == defn.getId();
	}

	protected void validateDescription(ValidationContext ctx) {
		// TODO
		// Object value = getValue(ctx, DESCRIPTION_FIELD);
	}

	protected void validateMaxCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if (multiple != null && multiple.booleanValue()) {
			NodeDefinition editedNode = getEditedNode(ctx);
			boolean result = true;
			if ( editedNode instanceof AttributeDefinition ) {
				result = validateRequired(ctx, MAX_COUNT_EXPRESSION_FIELD);
			}
			if ( result ) {
				Object maxCountVal = getValue(ctx, MAX_COUNT_EXPRESSION_FIELD);
				if (maxCountVal != null && isNumber(maxCountVal)) {
					String minCountVal = getValue(ctx, MIN_COUNT_EXPRESSION_FIELD);
					if ( StringUtils.isBlank(minCountVal) || isNumber(minCountVal) && Double.parseDouble(minCountVal.toString()) < MAX_COUNT_MIN_VALUE ) {
						validateGreaterThan(ctx, MAX_COUNT_EXPRESSION_FIELD, MAX_COUNT_MIN_VALUE, false);
					} else if (isNumber(minCountVal)) {
						String minCountLabel = Labels.getLabel(LabelKeys.NODE_MIN_COUNT);
						validateGreaterThan(ctx, MAX_COUNT_EXPRESSION_FIELD, MIN_COUNT_EXPRESSION_FIELD, minCountLabel, false);
					}
				}
			}
		}
	}
	
	private void validateColumn(ValidationContext ctx) {
		validateGreaterThan(ctx, COLUMN_FIELD, 1, false);
		validateGreaterThan(ctx, COLUMN_SPAN_FIELD, 1, false);
	}
	
	protected NodeDefinition getEditedNode(ValidationContext ctx) {
		return (NodeDefinition) ((NodeDefinitionVM<?>) getVM(ctx)).getEditedItem();
	}
	
	protected EntityDefinition getParentEntity(ValidationContext ctx) {
		return (EntityDefinition) ctx.getValidatorArg("parentEntity");
	}

}
