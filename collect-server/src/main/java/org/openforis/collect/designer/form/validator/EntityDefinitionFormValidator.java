package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormValidator extends NodeDefinitionFormValidator {

	protected static final String LAYOUT_FIELD = "layoutType";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateLayout(ctx);
	}

	protected void validateLayout(ValidationContext ctx) {
		String field = LAYOUT_FIELD;
		String layoutValue = getValue(ctx, field);
		Layout layout = Layout.valueOf(layoutValue);
		EntityDefinition editedNode = (EntityDefinition) getEditedNode(ctx);
		EntityDefinition parentEntity = getParentEntity(ctx);
		CollectSurvey survey = (CollectSurvey) editedNode.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		boolean assignable = uiOptions.isAssignableTo(parentEntity, editedNode, layout);
		if ( ! assignable ) {
			String message = Labels.getLabel(LabelKeys.LAYOUT_NOT_SUPPORTED_MESSAGE_KEY);
			addInvalidMessage(ctx, field, message);
		}
	}
	
}
