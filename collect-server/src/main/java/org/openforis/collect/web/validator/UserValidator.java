package org.openforis.collect.web.validator;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.controller.UserController.UsersDeleteParameters;
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
	private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_\\-\\.]{3,14}$");
	private static final String ROLE_FIELD = "role";
	private static final String RAW_PASSWORD_FIELD = "rawPassword";
	private static final String RETYPED_PASSWORD_FIELD = "retypedPassword";
	private static final String INVALID_USERNAME_MESSAGE_KEY = "user.validation.invalid_username";
	private static final String PASSWORD_PATTERN_MESSAGE_KEY = "user.validation.wrong_password_pattern";
	private static final String WRONG_RETYPED_PASSWORD_MESSAGE_KEY = "user.validation.wrong_retyped_password";
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public void validate(Object target, Errors errors) {
		super.validate(target, errors);
		if (target instanceof UsersDeleteParameters) {
//			List<Integer> userIds = ((UsersDeleteParameters) target).getUserIds();
		}
	}
	
	@Override
	public void validateForm(UserForm target, Errors errors) {
		String rawPassword = (String) errors.getFieldValue(RAW_PASSWORD_FIELD);
		String retypedPassword = (String) errors.getFieldValue(RETYPED_PASSWORD_FIELD);
		
		if (validateRequiredFields(errors, USERNAME_FIELD)) {
			if (target.getId() == null) {
				//skip username validation for already existing users (username will be readonly)
				if (validateRegEx(errors, USERNAME_FIELD, USERNAME_PATTERN, INVALID_USERNAME_MESSAGE_KEY)) {
					validateUniqueness(target, errors);
				}
				//password can be blank for already existing users (will remain unchanged)
				validateRequiredField(errors, RAW_PASSWORD_FIELD);
			}
			if (StringUtils.isNotBlank(rawPassword)) {
				validatePassword(errors, rawPassword);
			}
		}
		validateRequiredField(errors, ROLE_FIELD);
		
		if (! StringUtils.equals(rawPassword, retypedPassword)) {
			errors.rejectValue(RETYPED_PASSWORD_FIELD, WRONG_RETYPED_PASSWORD_MESSAGE_KEY);
		}
	}

	private boolean validatePassword(Errors errors, String rawPassword) {
		if (Pattern.matches(UserManager.PASSWORD_PATTERN, rawPassword)) {
			return true;
		} else {
			errors.rejectValue(RAW_PASSWORD_FIELD, PASSWORD_PATTERN_MESSAGE_KEY);
			return false;
		}
	}

	private boolean validateUniqueness(UserForm target, Errors errors) {
		User oldUser = userManager.loadByUserName(target.getUsername());
		if (oldUser != null && (target.getId() == null || ! oldUser.getId().equals(target.getId()))) {
			rejectDuplicateValue(errors, USERNAME_FIELD);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		boolean result = super.supports(clazz);
		return result || UsersDeleteParameters.class.isAssignableFrom(clazz);
	}
}
