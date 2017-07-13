package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.CodeAttributeVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	private static final String PARENT_CODE_ATTRIBUTE_DEFINITION_PATH_FIELD = "parentCodeAttributeDefinitionPath";
	protected static final String LIST_FIELD = "list";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateRequired(ctx, LIST_FIELD);
		validateParentAttributeDefinition(ctx);
	}

	private void validateParentAttributeDefinition(ValidationContext ctx) {
		String parentCodeAttributeDefPath = getValue(ctx, PARENT_CODE_ATTRIBUTE_DEFINITION_PATH_FIELD);
		CodeAttributeVM vm = (CodeAttributeVM) getVM(ctx);
		CollectSurvey survey = vm.getSurvey();
		CodeAttributeDefinition parentDef = (CodeAttributeDefinition) survey.getSchema().getDefinitionByPath(parentCodeAttributeDefPath);
		if (parentDef != null) {
			CodeList list = getValue(ctx, LIST_FIELD);
			if (list.isHierarchical()) {
				try {
					Integer parentHierarchicalLevelIdx = parentDef.getListLevelIndex();
					if (parentHierarchicalLevelIdx + 1 >= list.getHierarchy().size()) {
						addInvalidMessage(ctx, PARENT_CODE_ATTRIBUTE_DEFINITION_PATH_FIELD, 
								Labels.getLabel("survey.validation.attribute.code.invalid_parent_attribute_relation"));
					}
				} catch (Exception e) {
					addInvalidMessage(ctx, PARENT_CODE_ATTRIBUTE_DEFINITION_PATH_FIELD, 
							Labels.getLabel("survey.validation.attribute.code.invalid_parent_attribute_relation_in_referenced_parent_attribute"));
				}
			} else {
				addInvalidMessage(ctx, PARENT_CODE_ATTRIBUTE_DEFINITION_PATH_FIELD, 
						Labels.getLabel("survey.validation.attribute.code.parent_attribute_specified_for_a_flat_list"));
			}
		}
	}
}
