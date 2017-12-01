package org.openforis.collect.web.validator;

import java.util.List;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.controller.UserGroupController.UserInGroupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class UserGroupValidator extends SimpleValidator<UserGroupForm> {

	private static final String NAME_FIELD = "name";
	private static final String LABEL_FIELD = "label";
	
	@Autowired
	private UserGroupManager userGroupManager;
	
	@Override
	public void validateForm(UserGroupForm target, Errors errors) {
		if (validateRequiredField(errors, NAME_FIELD)) {
			if (validateInternalName(errors, NAME_FIELD)) {
				validateUniqueness(target, errors);
			}
		}
		validateRequiredField(errors, LABEL_FIELD);
		
		validateUsers(target, errors);
	}
	
	private boolean validateUsers(UserGroupForm target, Errors errors) {
		if (hasOwner(target)) {
			return true;
		} else {
			errors.reject("usergroup.validation.missing_owner");
			return false;
		}
	}

	private boolean hasOwner(UserGroupForm target) {
		List<UserInGroupForm> users = target.getUsers();
		if (users != null && !users.isEmpty()) {
			for (UserInGroupForm user : users) {
				if (user.getRole() == UserGroupRole.OWNER) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean validateUniqueness(UserGroupForm target, Errors errors) {
		UserGroup duplicateGroup = userGroupManager.findByName(target.getName());
		if (duplicateGroup != null && (target.getId() == null || ! duplicateGroup.getId().equals(target.getId()))) {
			rejectDuplicateValue(errors, NAME_FIELD);
			return false;
		}
		return true;
	}

}
