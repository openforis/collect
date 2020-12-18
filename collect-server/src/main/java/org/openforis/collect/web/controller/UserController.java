package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.text.Normalizer.Form;
import java.util.List;

import javax.validation.Valid;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.validator.PasswordChangeValidator;
import org.openforis.collect.web.validator.UserValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/user")
@Scope(SCOPE_SESSION)
public class UserController extends AbstractPersistedObjectEditFormController<Integer, User, UserForm, UserManager> {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private UserValidator userValidator;
	@Autowired
	private PasswordChangeValidator passwordChangeValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		if (binder.getTarget() instanceof UserForm) {
			binder.setValidator(userValidator);
		} else if (binder.getTarget() instanceof PasswordChangeParameters) {
			binder.setValidator(passwordChangeValidator);
		}
	}
	
	@RequestMapping(value = "{userId}/surveys/summaries.json", method=GET)
	public @ResponseBody
	List<SurveySummary> loadSummariesByUser(@PathVariable int userId) {
		User user = itemManager.loadById(userId);
		return surveyManager.getSurveySummaries(user);
	}
	
	@Override
	protected List<User> loadAllItems() {
		User loggedUser = sessionManager.getSessionState().getUser();
		return itemManager.loadAllAvailableUsers(loggedUser);
	}
	
	@Override
	protected User getLoggedUser() {
		return sessionManager.getLoggedUser();
	}
	
	@Transactional
	@RequestMapping(method=DELETE)
	public @ResponseBody
	Response delete(@Valid UsersDeleteParameters parameters) {
		for (int userId : parameters.getUserIds()) {
			itemManager.deleteById(userId);
		}
		return new Response();
	}
	
	@RequestMapping(value="validatepasswordchange", method=POST)
	public @ResponseBody Response validatePasswordChangeParameters(@Valid PasswordChangeParameters params, 
			BindingResult result) {
		return generateFormValidationResponse(result);
	}
	
	@Transactional
	@RequestMapping(value="changepassword", method=POST)
	public @ResponseBody Response changePassword(@Valid PasswordChangeParameters params,
			BindingResult bindingResult) throws Exception {
		String username = getLoggedUser().getUsername();
		itemManager.changePassword(username, params.getOldPassword(), params.getNewPassword());
		return new Response();
	}
	

	@Override
	protected UserForm createFormInstance(User item) {
		return new UserForm(item);
	}
	
	@Override
	protected User createItemInstance() {
		return new User();
	}
	
	protected void copyFormIntoItem(UserForm form, User item) {
		super.copyFormIntoItem(form, item);
		if (form.getRole() != null) {
			item.setRole(form.getRole());
		}
	};
	
	public static class UsersDeleteParameters {
		
		private int loggedUserId;
		private List<Integer> userIds;

		public int getLoggedUserId() {
			return loggedUserId;
		}

		public void setLoggedUserId(int loggedUserId) {
			this.loggedUserId = loggedUserId;
		}

		public List<Integer> getUserIds() {
			return userIds;
		}

		public void setUserIds(List<Integer> userIds) {
			this.userIds = userIds;
		}
	}
	
	public static class UserForm extends PersistedObjectForm<Integer, User> {
		
		private boolean enabled = true;
		private String username;
		private UserRole role;
		private List<UserRole> roles;
		private String rawPassword;
		private String retypedPassword;
		
		public UserForm() {
		}
		
		public UserForm(User user) {
			super(user);
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public UserRole getRole() {
			return role;
		}
		
		public void setRole(UserRole role) {
			this.role = role;
		}
		
		public List<UserRole> getRoles() {
			return roles;
		}
		
		public void setRoles(List<UserRole> roles) {
			this.roles = roles;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getRawPassword() {
			return rawPassword;
		}
		
		public void setRawPassword(String rawPassword) {
			this.rawPassword = rawPassword;
		}

		public String getRetypedPassword() {
			return retypedPassword;
		}

		public void setRetypedPassword(String retypedPassword) {
			this.retypedPassword = retypedPassword;
		}
	}
	
	public static class PasswordChangeParameters {
		
		private String oldPassword;
		private String newPassword;
		private String retypedPassword;

		public String getOldPassword() {
			return oldPassword;
		}

		public void setOldPassword(String oldPassword) {
			this.oldPassword = oldPassword;
		}

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		public String getRetypedPassword() {
			return retypedPassword;
		}
		
		public void setRetypedPassword(String retypedPassword) {
			this.retypedPassword = retypedPassword;
		}
	}

}
