package org.openforis.collect.web.controller;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openforis.collect.web.controller.UserControllerTest.user;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.web.controller.UserGroupController.UserGroupForm;
import org.openforis.collect.web.controller.UserGroupController.UserGroupsDeleteParameters;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class UserGroupControllerTest {

	private static final int RELATED_GROUP_ID = 10;
	private static final int OTHER_GROUP_ID = 20;

	private final SessionManager sessionManager = mock(SessionManager.class);
	private final UserGroupManager userGroupManager = mock(UserGroupManager.class);
	private final UserGroupController controller = new UserGroupController();
	private final UserGroupForm form = new UserGroupForm();
	private final BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
	private final UserGroupsDeleteParameters deleteParameters = new UserGroupsDeleteParameters();

	@Before
	public void init() {
		AccessControl accessControl = new AccessControl();
		ReflectionTestUtils.setField(accessControl, "sessionManager", sessionManager);
		ReflectionTestUtils.setField(accessControl, "userGroupManager", userGroupManager);
		ReflectionTestUtils.setField(controller, "accessControl", accessControl);
		ReflectionTestUtils.setField(controller, "sessionManager", sessionManager);
		ReflectionTestUtils.setField(controller, "surveyManager", mock(SurveyManager.class));
		controller.setItemManager(userGroupManager);
		when(userGroupManager.loadById(any())).thenAnswer(invocation -> group(invocation.getArgument(0)));
		when(userGroupManager.findAllRelatedUserGroups(any())).thenReturn(Arrays.asList(group(RELATED_GROUP_ID)));
		deleteParameters.setUserGroupIds(Arrays.asList(OTHER_GROUP_ID));
	}

	@Test
	public void userCanReadOnlyRelatedGroups() {
		login(UserRole.DESIGN);
		controller.load(RELATED_GROUP_ID);
		assertThrows(AccessDeniedException.class, () -> controller.load(OTHER_GROUP_ID));
	}

	@Test
	public void onlyAdminCanManageGroups() {
		login(UserRole.DESIGN);
		assertThrows(AccessDeniedException.class, () -> controller.insert(form, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.validate(form, bindingResult));
		assertThrows(AccessDeniedException.class, () -> controller.delete(RELATED_GROUP_ID));
		assertThrows(AccessDeniedException.class, () -> controller.delete(deleteParameters));
		assertThrows(AccessDeniedException.class,
				() -> controller.associateToResource(RELATED_GROUP_ID, "survey", "survey_1"));
		assertThrows(AccessDeniedException.class,
				() -> controller.disassociateResource(RELATED_GROUP_ID, "survey", "survey_1"));
		verify(userGroupManager, never()).associateResource(anyInt(), any(), any());
		verify(userGroupManager, never()).disassociateResource(anyInt(), any(), any());
		verify(userGroupManager, never()).deleteById(any());

		login(UserRole.ADMIN);
		controller.load(OTHER_GROUP_ID);
		controller.validate(form, bindingResult);
		controller.associateToResource(RELATED_GROUP_ID, "survey", "survey_1");
		controller.disassociateResource(RELATED_GROUP_ID, "survey", "survey_1");
		controller.delete(deleteParameters);
		verify(userGroupManager).associateResource(RELATED_GROUP_ID, "survey", "survey_1");
		verify(userGroupManager).disassociateResource(RELATED_GROUP_ID, "survey", "survey_1");
		verify(userGroupManager).deleteById(OTHER_GROUP_ID);
	}

	private void login(UserRole role) {
		when(sessionManager.getLoggedUser()).thenReturn(user(1, role));
	}

	private static UserGroup group(int id) {
		UserGroup group = new UserGroup();
		group.setId(id);
		group.setSystemDefined(false);
		return group;
	}
}
