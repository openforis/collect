package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.validator.UserValidator;
import org.openforis.commons.web.PersistedObjectForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/user")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserController extends AbstractPersistedObjectEditFormController<User, UserForm, UserManager> {
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private UserValidator validator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
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
	protected UserForm createFormInstance(User item) {
		return new UserForm(item);
	}
	
	@Override
	protected User createItemInstance() {
		return new User();
	}
	
	public static class UserForm extends PersistedObjectForm<User> {
		
		private boolean enabled = true;
		private String username;
		private UserRole role;
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

}
