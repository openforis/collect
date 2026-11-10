package org.openforis.collect.web.controller;

import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Authorization checks on the user logged in the current session.
 * A failed check throws {@link AccessDeniedException} (translated into HTTP 403 by Spring Security).
 */
@Component
public class AccessControl {

	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private UserGroupManager userGroupManager;

	public User getLoggedUser() {
		User user = sessionManager.getLoggedUser();
		if (user == null) {
			throw new AccessDeniedException("User not logged in");
		}
		return user;
	}

	public boolean isAdmin() {
		return getLoggedUser().hasEffectiveRole(UserRole.ADMIN);
	}

	public User checkRole(UserRole role) {
		User user = getLoggedUser();
		if (!user.hasEffectiveRole(role)) {
			throw new AccessDeniedException(String.format("User %s does not have role %s", user.getUsername(), role));
		}
		return user;
	}

	public User checkAdmin() {
		return checkRole(UserRole.ADMIN);
	}

	public void checkAdminOrSelf(Integer userId) {
		User user = getLoggedUser();
		if (!user.getId().equals(userId)) {
			checkAdmin();
		}
	}

	/**
	 * Same visibility rule as the user groups list (GET /api/usergroup): admins can access every group.
	 */
	public void checkCanAccessUserGroup(Integer userGroupId) {
		User user = getLoggedUser();
		if (!isAdmin() && userGroupManager.findAllRelatedUserGroups(user).stream()
				.noneMatch(g -> g.getId().equals(userGroupId))) {
			throw new AccessDeniedException(
					String.format("User %s cannot access user group %d", user.getUsername(), userGroupId));
		}
	}

}
