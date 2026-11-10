package org.openforis.collect.web.controller;

import java.util.function.BiPredicate;

import org.openforis.collect.manager.RecordAccessControlManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Authorization checks on the user logged in the current session.
 * A failed check throws {@link AccessDeniedException} (translated into HTTP 403 by Spring Security).
 */
@Component
public class AccessControl {

	private final SessionManager sessionManager;
	private final UserGroupManager userGroupManager;
	private final RecordManager recordManager;
	private final RecordAccessControlManager recordAccessControlManager = new RecordAccessControlManager();

	public AccessControl(SessionManager sessionManager, UserGroupManager userGroupManager, RecordManager recordManager) {
		this.sessionManager = sessionManager;
		this.userGroupManager = userGroupManager;
		this.recordManager = recordManager;
	}

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

	/**
	 * The user must belong to the survey user group (or to one of its children); admins can access every survey.
	 * 
	 * @return the role of the user in the survey user group (null for admins not belonging to it)
	 */
	public UserGroupRole checkCanAccessSurvey(CollectSurvey survey) {
		return checkCanAccessSurveyGroup(survey.getUserGroupId());
	}

	public UserGroupRole checkCanAccessSurveyGroup(Integer surveyUserGroupId) {
		User user = getLoggedUser();
		UserInGroup userInGroup = surveyUserGroupId == null ? null
				: userGroupManager.findUserInGroupOrDescendants(surveyUserGroupId, user.getId());
		if (userInGroup == null && !isAdmin()) {
			throw new AccessDeniedException(String.format("User %s cannot access surveys in user group %d",
					user.getUsername(), surveyUserGroupId));
		}
		return userInGroup == null ? null : userInGroup.getRole();
	}

	/**
	 * Survey designer operations (role DESIGN + survey access)
	 */
	public void checkCanEditSurvey(CollectSurvey survey) {
		checkRole(UserRole.DESIGN);
		checkCanAccessSurvey(survey);
	}

	public void checkSurveyPermission(CollectSurvey survey, BiPredicate<User, UserGroupRole> permission) {
		checkSurveyGroupPermission(survey.getUserGroupId(), permission);
	}

	public void checkSurveyGroupPermission(Integer surveyUserGroupId, BiPredicate<User, UserGroupRole> permission) {
		UserGroupRole role = checkCanAccessSurveyGroup(surveyUserGroupId);
		User user = getLoggedUser();
		if (!isAdmin() && !permission.test(user, role)) {
			throw new AccessDeniedException(String.format("Operation not allowed to user %s", user.getUsername()));
		}
	}

	/**
	 * Checks that the record belongs to the survey and that the user can access it
	 */
	public CollectRecordSummary checkCanViewRecord(CollectSurvey survey, int recordId) {
		return checkRecordAccess(survey, recordId, false);
	}

	public CollectRecordSummary checkCanEditRecord(CollectSurvey survey, int recordId) {
		return checkRecordAccess(survey, recordId, true);
	}

	private CollectRecordSummary checkRecordAccess(CollectSurvey survey, int recordId, boolean edit) {
		UserGroupRole role = checkCanAccessSurvey(survey);
		User user = getLoggedUser();
		CollectRecordSummary summary = recordManager.loadUniqueRecordSummary(survey, recordId);
		boolean allowed = summary != null && (isAdmin() || (edit
				? recordAccessControlManager.canEdit(user, role, summary.getStep(), summary.getOwner())
				: recordAccessControlManager.canView(user, role, summary.getOwner())));
		if (!allowed) {
			throw new AccessDeniedException(String.format("User %s cannot %s record %d in survey %s",
					user.getUsername(), edit ? "edit" : "access", recordId, survey.getName()));
		}
		return summary;
	}
}
