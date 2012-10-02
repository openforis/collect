package org.openforis.collect.designer.form.validator;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
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

	private static final String VM_ID_ATTRIBUTE = "$VM_ID$";

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
				valid = validateNameUniqueness(ctx);
			}
		}
		return valid;
	}
	
	protected boolean validateNameUniqueness(ValidationContext ctx) {
		NodeDefinition editedNode = getEditedNode(ctx);
		String name = (String) getValue(ctx, NAME_FIELD);
		if ( ! isNameUnique(editedNode, name) ) {
			String message = Labels.getLabel(NAME_ALREADY_DEFINED_MESSAGE_KEY);
			this.addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
	
	protected boolean isNameUnique(NodeDefinition editedNode, String name) {
		if ( editedNode instanceof EntityDefinition ) {
			if ( existsHomonymous((EntityDefinition) editedNode, name) ) {
				return false;
			}
		} else if ( existsHomonymousSibling(editedNode, name) ) {
			return false;
		}
		return true;
	}

	protected boolean existsHomonymousSibling(NodeDefinition defn, String name) {
		String parentPath;
		EntityDefinition parentDefn = (EntityDefinition) defn.getParentDefinition();
		if ( parentDefn != null ) {
			parentPath = parentDefn.getPath();
		} else {
			parentPath = "";
		}
		Schema schema = defn.getSchema();
		NodeDefinition nodeInPath = schema.getByPath(parentPath + "/" + name);
		return nodeInPath != null && ! nodeInPath.getId().equals(defn.getId());
	}
	
	protected boolean existsHomonymous(EntityDefinition editedNode, String name) {
		Schema schema = editedNode.getSchema();
		Collection<NodeDefinition> allDefinitions = schema.getAllDefinitions();
		for (NodeDefinition defn : allDefinitions) {
			if ( defn instanceof EntityDefinition && StringUtils.equals(defn.getName(), name) ) {
				return ! defn.getId().equals(editedNode.getId());
			}
		}
		return false;
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
		Object vmObject = getVM(ctx);
		if ( vmObject instanceof SurveySchemaEditVM ) {
			NodeDefinition editedNode = ((SurveySchemaEditVM) vmObject).getSelectedNode();
			return editedNode;
		} else {
			throw new IllegalArgumentException("Unsupported View Model Type: " + vmObject.getClass().getName());
		}
	}

	protected Object getVM(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		Component component = bindContext.getComponent();
		String vmId = (String) component.getAttribute(VM_ID_ATTRIBUTE);
		Object vmObject = component.getAttribute(vmId);
		return vmObject;
	}

}
