package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveySchemaEditVM;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionFormValidator extends FormValidator {

	protected static final String NAME_ALREADY_DEFINED_MESSAGE_KEY = "survey.schema.node.validation.name_already_defined";
	
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
		if ( valid ) {
			valid = validateInternalName(ctx, NAME_FIELD);
			if ( valid ) {
				//valid = validateNameUniqueness(ctx);
			}
		}
		return valid;
	}
	
	protected boolean validateNameUniqueness(ValidationContext ctx) {
		NodeDefinition editedNode = getEditedNode(ctx);
		String name = (String) getValue(ctx, NAME_FIELD);
		NodeDefinition sameNameDefn = getSameNameSibling(editedNode, name);
		if ( ! sameNameDefn.getId().equals(editedNode.getId()) ) {
			String message = Labels.getLabel(NAME_ALREADY_DEFINED_MESSAGE_KEY);
			this.addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}

	private NodeDefinition getSameNameSibling(NodeDefinition editedNode, String name) {
		EntityDefinition parentDefn = (EntityDefinition) editedNode.getParentDefinition();
		if ( parentDefn != null ) {
			NodeDefinition sameNameDefn = parentDefn.getChildDefinition(name);
			return sameNameDefn;
		} else {
			Schema schema = editedNode.getSchema();
			EntityDefinition sameNameDefn = schema.getRootEntityDefinition(name);
			return sameNameDefn;
		}
	}

	protected void validateDescription(ValidationContext ctx) {
		//TODO
		//Object value = getValue(ctx, DESCRIPTION_FIELD);
	}
	
	protected void validateMaxCount(ValidationContext ctx) {
		Boolean multiple = (Boolean) getValue(ctx, MULTIPLE_FIELD);
		if ( multiple != null && multiple.booleanValue() ) {
			if ( validateRequired(ctx, NAME_FIELD) ) {
				validateGreaterThan(ctx, MAX_COUNT_FIELD, 1, true);
			}
		}
	}
	
	protected NodeDefinition getEditedNode(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		Component component = bindContext.getComponent();
		Object vmObject = component.getAttribute("viewModel");
		if ( vmObject instanceof SurveySchemaEditVM ) {
			NodeDefinition editedNode = ((SurveySchemaEditVM) vmObject).getSelectedNode();
			return editedNode;
		} else {
			throw new IllegalArgumentException("Unsupported View Model Type: " + vmObject.getClass().getName());
		}
	}

}
