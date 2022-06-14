package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.designer.form.NodeDefinitionFormObject.MULTIPLE_FIELD;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionType;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityDefinitionFormValidator extends NodeDefinitionFormValidator {

	private static final String VIRTUAL_FIELD = "virtual";
	private static final String GENERATOR_EXPRESSION_FIELD = "generatorExpression";
	protected static final String LAYOUT_FIELD = "layoutType";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		Object virtual = getValue(ctx, VIRTUAL_FIELD);
		if (Boolean.TRUE.equals(virtual)) {
			if (validateRequired(ctx, GENERATOR_EXPRESSION_FIELD)) {
				validateExpressionField(ctx, ExpressionType.SCHEMA_PATH, GENERATOR_EXPRESSION_FIELD, getEditedNode(ctx));
			}
		}
		validateLayout(ctx);
	}

	protected void validateLayout(ValidationContext ctx) {
		EntityDefinition editedNode = (EntityDefinition) getEditedNode(ctx);
		if (editedNode.isVirtual()) {
			//skip check
			return;
		}
		String field = LAYOUT_FIELD;
		String layoutValue = getValueWithDefault(ctx, field, Layout.FORM.name());
		Layout layout = Layout.valueOf(layoutValue);
		EntityDefinition parentEntity = getParentEntity(ctx);
		CollectSurvey survey = (CollectSurvey) editedNode.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		Boolean multiple = getValue(ctx, MULTIPLE_FIELD);
		UITab tab = uiOptions.getAssignedTab(editedNode);
//		UITab tab = getAssociatedTab(ctx, uiOptions, parentEntity);
		if ( tab != null ) {
			boolean valid = uiOptions.isLayoutSupported(parentEntity, editedNode.getId(), tab, multiple, layout);
			if ( ! valid ) {
				String message = Labels.getLabel(LabelKeys.LAYOUT_NOT_SUPPORTED_MESSAGE_KEY);
				addInvalidMessage(ctx, field, message);
			}
		} else {
			//defining root entity, not yet added to schema...
		}
	}

	protected UITab getAssociatedTab(ValidationContext ctx, UIOptions uiOptions, EntityDefinition parentEntity) {
		String tabName = getValue(ctx, TAB_NAME_FIELD);
		if ( parentEntity == null ) {
			NodeDefinition editedNode = getEditedNode(ctx);
			UITabSet rootTabSet = uiOptions.getAssignedRootTabSet((EntityDefinition) editedNode);
			if ( rootTabSet != null ) {
				return uiOptions.getMainTab(rootTabSet);
			} else {
				return null;
			}
		} else {
			if (StringUtils.isNotBlank(tabName) ) {
				List<UITab> assignableTabs = uiOptions.getTabsAssignableToChildren(parentEntity);
				for (UITab tab : assignableTabs) {
					if ( tab.getName().equals(tabName) ) {
						return tab;
					}
				}
			}
			//inherited tab
			return uiOptions.getAssignedTab(parentEntity);
		}
	}
	
}
