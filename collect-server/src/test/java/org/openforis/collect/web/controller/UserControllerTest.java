package org.openforis.collect.web.controller;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.UserController.UserForm;
import org.openforis.collect.web.controller.UserController.UsersDeleteParameters;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class UserControllerTest {

	private final SessionManager sessionManager = mock(SessionManager.class);
	private final UserManager userManager = mock(UserManager.class);
	private final UserController controller = new UserController(new AccessControl(sessionManager, null));
	private final UserForm form = new UserForm();
	private final BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
	private final UsersDeleteParameters deleteParameters = new UsersDeleteParameters();

	@Before
	public void init() {
		ReflectionTestUtils.setField(controller, "sessionManager", sessionManager);
		ReflectionTestUtils.setField(controller, "surveyManager", mock(SurveyManager.class));
		controller.setItemManager(userManager);
		when(userManager.loadById(any())).thenAnswer(invocation -> user(invocation.getArgument(0), UserRole.ENTRY));
		deleteParameters.setUserIds(Arrays.asList(2));
	}

	@Test
	public void userCanReadOnlyHisOwnAccount() {
		login(1, UserRole.ENTRY);
		controller.load(1);
		controller.loadSummariesByUser(1);
		assertThrows(AccessDeniedException.class, () -> controller.load(2));
		assertThrows(AccessDeniedException.class, () -> controller.loadSummariesByUser(2));
	}

	@Test
	public void onlyAdminCanEditUsers() {
		login(1, UserRole.DESIGN);
		assertThrows(AccessDeniedException.class, () -> controller.insert(form, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.update(form, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.validate(form, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.duplicate(2, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.delete(2));
		assertThrows(AccessDeniedException.class, () -> controller.delete(deleteParameters));
		verify(userManager, never()).save(any(), any());
		verify(userManager, never()).deleteById(any());

		login(1, UserRole.ADMIN);
		controller.load(2);
		controller.loadSummariesByUser(2);
		controller.validate(form, bindingResult);
		controller.insert(form, bindingResult);
		controller.duplicate(2, bindingResult);
		controller.delete(2);
		controller.delete(deleteParameters);
		verify(userManager).deleteById(2);
	}

	private void login(int id, UserRole role) {
		when(sessionManager.getLoggedUser()).thenReturn(user(id, role));
	}

	static User user(int id, UserRole role) {
		User user = new User(id, "user" + id);
		user.setRole(role);
		user.setEnabled(true);
		return user;
	}
}
