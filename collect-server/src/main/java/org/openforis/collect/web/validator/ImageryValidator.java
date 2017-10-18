package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.ImageryManager;
import org.openforis.collect.model.Imagery;
import org.openforis.collect.web.controller.ImageryController.ImageryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class ImageryValidator extends SimpleValidator<ImageryForm> {

	private static final String TITLE_FIELD = "title";
	
	@Autowired
	private ImageryManager imageryManager;
	
	@Override
	public void validateForm(ImageryForm target, Errors errors) {
		if (validateRequiredField(errors, TITLE_FIELD)) {
			validateUniqueness(target, errors);
		}
	}
	
	private boolean validateUniqueness(ImageryForm target, Errors errors) {
		Imagery duplicate = imageryManager.findByTitle(target.getTitle());
		if (duplicate != null && (target.getId() == null || ! duplicate.getId().equals(target.getId()))) {
			rejectDuplicateValue(errors, TITLE_FIELD);
			return false;
		}
		return true;
	}

}
