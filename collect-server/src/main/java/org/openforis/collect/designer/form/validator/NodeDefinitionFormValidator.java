package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveySchemaEditVM;
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

	protected static final String DESCRIPTION_FIELD = "description";
	protected static final String NAME_FIELD = "name";
	protected static final String MAX_COUNT_FIELD = "maxCount";
	protected static final String MULTIPLE_FIELD = "multiple";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
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
		if (!isNameUnique(editedNode, name)) {
			String message = Labels.getLabel(NODE_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			this.addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}

	protected boolean isNameUnique(NodeDefinition editedNode, String name) {
		if (existsHomonymousSibling(editedNode, name)) {
			return false;
		} else {
			return true;
		}
	}

	protected boolean existsHomonymousSibling(NodeDefinition defn, String name) {
		EntityDefinition parentDefn = (EntityDefinition) defn.getParentDefinition();
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
		return nodeInPath != null && nodeInPath.getId() != defn.getId();
	}

	protected void validateDescription(ValidationContext ctx) {
		// TODO
		// Object value = getValue(ctx, DESCRIPTION_FIELD);
	}

	protected void validateMaxCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if (multiple != null && multiple.booleanValue()) {
			if (validateRequired(ctx, NAME_FIELD)) {
				validateGreaterThan(ctx, MAX_COUNT_FIELD, 1, true);
			}
		}
	}

	protected NodeDefinition getEditedNode(ValidationContext ctx) {
		Object vmObject = getVM(ctx);
		if (vmObject instanceof SurveySchemaEditVM) {
			NodeDefinition editedNode = ((SurveySchemaEditVM) vmObject).getEditedNode();
			return editedNode;
		} else {
			throw new IllegalArgumentException("Unsupported View Model Type: " +
					vmObject.getClass().getName());
		}
	}

}
