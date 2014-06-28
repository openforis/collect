package org.openforis.collect.designer.form.validator;

import java.util.List;

import org.openforis.collect.designer.viewmodel.CalculatedAttributeVM;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	private static final String FORMULAS_FIELD = "formulas";
	private static final String SHOW_IN_UI_FIELD = "showInUI";
	private static final String INCLUDE_IN_DATA_EXPORT_FIELD = "includeInDataExport";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		
		validateFormulas(ctx);
		validateShowInUIAndIncludeInDataExport(ctx);
	}

	private void validateFormulas(ValidationContext ctx) {
		CalculatedAttributeVM vm = (CalculatedAttributeVM) getVM(ctx);
		List<Formula> formulas = vm.getFormulas();
		validateRequired(ctx, FORMULAS_FIELD, formulas);
	}

	private void validateShowInUIAndIncludeInDataExport(ValidationContext ctx) {
		Boolean includeInDataExport = getValue(ctx, INCLUDE_IN_DATA_EXPORT_FIELD);
		Boolean showInUI = getValue(ctx, SHOW_IN_UI_FIELD);
		if ( ( includeInDataExport == null || ! includeInDataExport.booleanValue() ) &&
			 ( showInUI == null || ! showInUI.booleanValue() ) ) {
			String message = Labels.getLabel("survey.schema.attribute.calculated.error.must_specify_include_in_data_export_or_in_ui");
			addInvalidMessage(ctx, INCLUDE_IN_DATA_EXPORT_FIELD, message);
			addInvalidMessage(ctx, SHOW_IN_UI_FIELD, message);
		}
	}
	
}
