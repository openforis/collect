package org.openforis.collect.web.controller;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.manager.LocalUserGroupManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.model.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

public class AccessControlTest {

	private static final int RELATED_GROUP_ID = 10;
	private static final int OTHER_GROUP_ID = 20;

	private AccessControl accessControl;
	private User loggedUser;

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
		});
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
	public void notLoggedUserIsDenied() {
		loggedUser = null;
		assertThrows(AccessDeniedException.class, () -> accessControl.checkRole(UserRole.VIEW));
	}

	private static User user(int id, UserRole role) {
		User user = new User(id, "user" + id);
		user.setRole(role);
		return user;
	}
}
