package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.designer.viewmodel.NodeDefinitionVM;
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
public class NodeDefinitionFormValidator extends FormValidator {

	protected static final String NODE_NAME_ALREADY_DEFINED_MESSAGE_KEY = "survey.schema.node.validation.name_already_defined";

	protected static final String 	DESCRIPTION_FIELD = "description";
	protected static final String 	NAME_FIELD = "name";
	protected static final String 	MULTIPLE_FIELD = "multiple";
	private static final String 	MIN_COUNT_FIELD = "minCount";
	protected static final String 	MAX_COUNT_FIELD = "maxCount";
	protected static final String 	TAB_NAME_FIELD = "tabName";
	
	protected static final int 		MAX_COUNT_MIN_VALUE = 2;

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
		validateMinCount(ctx);
		validateMaxCount(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		boolean valid = validateRequired(ctx, NAME_FIELD);
		if (valid) {
			valid = validateInternalName(ctx, NAME_FIELD);
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

	protected void validateMinCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if (multiple != null && multiple.booleanValue()) {
			validateGreaterThan(ctx, MIN_COUNT_FIELD, 0, false);
		}
	}

	protected void validateMaxCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if (multiple != null && multiple.booleanValue()) {
			NodeDefinition editedNode = getEditedNode(ctx);
			boolean result = true;
			if ( editedNode instanceof AttributeDefinition ) {
				result = validateRequired(ctx, MAX_COUNT_FIELD);
			}
			if ( result ) {
				Integer maxCount = getValue(ctx, MAX_COUNT_FIELD);
				if ( maxCount != null ) {
					Integer minCount = getValue(ctx, MIN_COUNT_FIELD);
					if ( minCount == null || minCount.intValue() < MAX_COUNT_MIN_VALUE ) {
						validateGreaterThan(ctx, MAX_COUNT_FIELD, MAX_COUNT_MIN_VALUE, false);
					} else {
						String minCountLabel = Labels.getLabel(LabelKeys.NODE_MIN_COUNT);
						validateGreaterThan(ctx, MAX_COUNT_FIELD, MIN_COUNT_FIELD, minCountLabel, false);
					}
				}
			}
		}
	}

	protected NodeDefinition getEditedNode(ValidationContext ctx) {
		Object vmObject = getVM(ctx);
		NodeDefinition editedNode;
		if (vmObject instanceof NodeDefinitionVM) {
			editedNode = ((NodeDefinitionVM<?>) vmObject).getEditedItem();
		} else {
			throw new IllegalArgumentException("Unsupported View Model Standard: " +
					vmObject.getClass().getName());
		}
		return editedNode;
	}

	protected EntityDefinition getParentEntity(ValidationContext ctx) {
		return (EntityDefinition) ctx.getValidatorArg("parentEntity");
	}

}
