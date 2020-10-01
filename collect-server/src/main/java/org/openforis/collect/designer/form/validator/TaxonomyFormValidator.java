package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.idm.model.species.Taxonomy;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class TaxonomyFormValidator extends FormValidator {

	protected static final String NAME_FIELD = "name";
	
	@WireVariable
	private SpeciesManager speciesManager;

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
	}

	protected boolean validateName(ValidationContext ctx) {
		boolean result = validateRequired(ctx, NAME_FIELD);
		if ( result ) {
			result = validateInternalName(ctx, NAME_FIELD);
			if ( result ) {
				result = validateNameUniqueness(ctx);
			}
		}
		return result;
	}
	
	protected boolean validateNameUniqueness(ValidationContext ctx) {
		SurveyObjectBaseVM<CollectTaxonomy> viewModel = getVM(ctx);
		Taxonomy editedItem = viewModel.getEditedItem();
		CollectSurvey survey = viewModel.getSurvey();
		String name = (String) getValue(ctx, NAME_FIELD);
		Taxonomy existingItem = survey.getContext().getSpeciesListService().loadTaxonomyByName(survey, name);
		if (existingItem != null && existingItem.getId().intValue() != editedItem.getId().intValue()) {
			String message = Labels.getLabel(ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY);
			addInvalidMessage(ctx, NAME_FIELD, message);
			return false;
		} else {
			return true;
		}
	}
}
