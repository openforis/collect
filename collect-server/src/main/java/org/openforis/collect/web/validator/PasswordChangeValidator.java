package org.openforis.collect.web.validator;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.web.controller.UserController.PasswordChangeParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
@Scope(SCOPE_SESSION)
public class PasswordChangeValidator extends SimpleValidator<PasswordChangeParameters> {

	private static final String WRONG_PASSWORD_SPECIFIED_MESSAGE_KEY = "user.validation.wrongPasswordSpecified";
	private static final String OLD_PASSWORD_FIELD = "oldPassword";
	private static final String NEW_PASSWORD_FIELD = "newPassword";
	private static final String RETYPED_PASSWORD_FIELD = "retypedPassword";
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public void validateForm(PasswordChangeParameters target, Errors errors) {
		validateOldPassword(errors);
		validateNewPassword(errors);
		validateRetypedPassword(errors);
	}

	private void validateOldPassword(Errors errors) {
		if (validateRequiredField(errors, OLD_PASSWORD_FIELD)) {
			String oldPassword = (String) errors.getFieldValue(OLD_PASSWORD_FIELD);
			User loggedUser = sessionManager.getLoggedUser();
			if (! userManager.verifyPassword(loggedUser.getUsername(), oldPassword)) {
				errors.rejectValue(OLD_PASSWORD_FIELD, WRONG_PASSWORD_SPECIFIED_MESSAGE_KEY);
			}
		}
	}
	
	private void validateNewPassword(Errors errors) {
		if (validateRequiredField(errors, NEW_PASSWORD_FIELD)) {
			String newPassword = (String) errors.getFieldValue(NEW_PASSWORD_FIELD);
			UserValidator.validatePassword(errors, newPassword, NEW_PASSWORD_FIELD);
		}
	}

	private void validateRetypedPassword(Errors errors) {
		if (validateRequiredField(errors, RETYPED_PASSWORD_FIELD)) {
			String newPassword = (String) errors.getFieldValue(NEW_PASSWORD_FIELD);
			String retypedPassword = (String) errors.getFieldValue(RETYPED_PASSWORD_FIELD);
			if (! StringUtils.equals(newPassword, retypedPassword)) {
				errors.rejectValue(RETYPED_PASSWORD_FIELD, UserValidator.WRONG_RETYPED_PASSWORD_MESSAGE_KEY);
			}
		}
	}
}
