package org.openforis.collect.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.LocalUserGroupManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

public class AccessControlTest {

	private static final int RELATED_GROUP_ID = 10;
	private static final int OTHER_GROUP_ID = 20;

	private AccessControl accessControl;
	private User loggedUser;
	private final RecordManager recordManager = mock(RecordManager.class);

	@Before
	public void init() {
		accessControl = new AccessControl();
		ReflectionTestUtils.setField(accessControl, "sessionManager", new SessionManager() {
			@Override
			public User getLoggedUser() {
				return loggedUser;
			}
		});
		ReflectionTestUtils.setField(accessControl, "userGroupManager", new LocalUserGroupManager() {
			@Override
			public List<UserGroup> findAllRelatedUserGroups(User user) {
				UserGroup group = new UserGroup();
				group.setId(RELATED_GROUP_ID);
				return Arrays.asList(group);
			}

			@Override
			public UserInGroup findUserInGroupOrDescendants(int userGroupId, int userId) {
				if (userGroupId != RELATED_GROUP_ID) {
					return null;
				}
				UserInGroup userInGroup = new UserInGroup();
				userInGroup.setRole(UserGroupRole.OPERATOR);
				return userInGroup;
			}
		});
		ReflectionTestUtils.setField(accessControl, "recordManager", recordManager);
	}

	@Test
	public void adminCanDoEverything() {
		loggedUser = user(1, UserRole.ADMIN);
		accessControl.checkAdmin();
		accessControl.checkRole(UserRole.DESIGN);
		accessControl.checkAdminOrSelf(2);
		accessControl.checkCanAccessUserGroup(OTHER_GROUP_ID);
	}

	@Test
	public void userCannotAccessOtherUsersOrUnrelatedGroups() {
		loggedUser = user(1, UserRole.DESIGN);
		accessControl.checkRole(UserRole.ENTRY);
		accessControl.checkAdminOrSelf(1);
		accessControl.checkCanAccessUserGroup(RELATED_GROUP_ID);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkAdmin());
		assertThrows(AccessDeniedException.class, () -> accessControl.checkAdminOrSelf(2));
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanAccessUserGroup(OTHER_GROUP_ID));
	}

	@Test
	public void surveyAccessRequiresSurveyGroupMembership() {
		loggedUser = user(1, UserRole.ENTRY);
		assertEquals(UserGroupRole.OPERATOR, accessControl.checkCanAccessSurveyGroup(RELATED_GROUP_ID));
		accessControl.checkSurveyGroupPermission(RELATED_GROUP_ID, (user, role) -> role == UserGroupRole.OPERATOR);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanAccessSurveyGroup(OTHER_GROUP_ID));
		assertThrows(AccessDeniedException.class, 
				() -> accessControl.checkSurveyGroupPermission(RELATED_GROUP_ID, (user, role) -> false));

		loggedUser = user(2, UserRole.ADMIN);
		assertNull(accessControl.checkCanAccessSurveyGroup(OTHER_GROUP_ID));
		accessControl.checkSurveyGroupPermission(OTHER_GROUP_ID, (user, role) -> false);
	}

	@Test
	public void surveyEditingRequiresDesignRoleAndSurveyAccess() {
		loggedUser = user(1, UserRole.DESIGN);
		accessControl.checkCanEditSurvey(survey(RELATED_GROUP_ID));
		accessControl.checkSurveyPermission(survey(RELATED_GROUP_ID), (user, role) -> role == UserGroupRole.OPERATOR);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanEditSurvey(survey(OTHER_GROUP_ID)));

		loggedUser = user(1, UserRole.ENTRY);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanEditSurvey(survey(RELATED_GROUP_ID)));
	}

	@Test
	public void recordAccessDependsOnRecordStepAndOwner() {
		User entryUser = loggedUser = user(1, UserRole.ENTRY);
		CollectSurvey survey = survey(RELATED_GROUP_ID);
		CollectRecordSummary ownedRecord = recordSummary(survey, 1, Step.ENTRY, entryUser);
		recordSummary(survey, 2, Step.CLEANSING, user(2, UserRole.ENTRY));
		assertEquals(ownedRecord, accessControl.checkCanViewRecord(survey, 1));
		assertEquals(ownedRecord, accessControl.checkCanEditRecord(survey, 1));
		accessControl.checkCanViewRecord(survey, 2);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanEditRecord(survey, 2));
		// record not found in the survey
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanViewRecord(survey, 3));

		loggedUser = user(1, UserRole.ENTRY_LIMITED);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanViewRecord(survey, 2));

		loggedUser = user(3, UserRole.ADMIN);
		accessControl.checkCanEditRecord(survey, 2);
		assertThrows(AccessDeniedException.class, () -> accessControl.checkCanEditRecord(survey, 3));
	}

	@Test
	public void notLoggedUserIsDenied() {
		loggedUser = null;
		assertThrows(AccessDeniedException.class, () -> accessControl.checkRole(UserRole.VIEW));
	}

	private CollectRecordSummary recordSummary(CollectSurvey survey, int recordId, Step step, User owner) {
		CollectRecordSummary summary = new CollectRecordSummary();
		summary.setStep(step);
		summary.setOwner(owner);
		when(recordManager.loadUniqueRecordSummary(survey, recordId)).thenReturn(summary);
		return summary;
	}

	private static CollectSurvey survey(int userGroupId) {
		CollectSurvey survey = mock(CollectSurvey.class);
		when(survey.getUserGroupId()).thenReturn(userGroupId);
		return survey;
	}

	private static User user(int id, UserRole role) {
		User user = new User(id, "user" + id);
		user.setRole(role);
		return user;
	}
}
