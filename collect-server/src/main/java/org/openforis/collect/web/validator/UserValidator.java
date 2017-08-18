package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class UserValidator extends SimpleValidator<UserForm> {

	private static final String USERNAME_FIELD = "username";
	@Autowired
	private UserManager userManager;
	
	@Override
	public void validateForm(UserForm target, Errors errors) {
		if (validateRequiredFields(errors, USERNAME_FIELD, "password", "retypedPassword")) {
			validateUniqueness(target, errors);
		}
	}

	private boolean validateUniqueness(UserForm target, Errors errors) {
		User oldUser = userManager.loadByUserName(target.getUsername());
		if (target.getId() == null || ! oldUser.getId().equals(target.getId())) {
			rejectDuplicateValue(errors, USERNAME_FIELD);
			return false;
		}
		return true;
	}

}
