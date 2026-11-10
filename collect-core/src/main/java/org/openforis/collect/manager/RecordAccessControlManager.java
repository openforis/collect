package org.openforis.collect.manager;

import java.util.Collection;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordAccessControlManager {

	public boolean canEdit(User user, CollectRecord record) {
		return record.getOwner() == null 
				|| record.getOwner().getId().equals(user.getId())
				|| user.hasEffectiveRole(UserRole.CLEANSING);
	}
	
	public boolean isOwnerToBeResetAfterPromoting(User user, Step step) {
		return step != Step.ENTRY || ! user.hasEffectiveRole(UserRole.CLEANSING);
	}
	
	// The following rules mirror the ones in the web UI (collect-webapp/frontend/src/model/User.js)

	/**
	 * Users with role ENTRY_LIMITED or DATA_CLEANER_LIMITED in the survey group can access only their own records
	 */
	public boolean canView(User user, UserGroupRole roleInSurveyGroup, User recordOwner) {
		boolean limited = user.getRole() == UserRole.ENTRY_LIMITED || roleInSurveyGroup == UserGroupRole.DATA_CLEANER_LIMITED;
		return !limited || isOwner(user, recordOwner);
	}

	public boolean canEditRecords(User user, UserGroupRole roleInSurveyGroup) {
		if (user.getRole() == UserRole.VIEW || roleInSurveyGroup == null) {
			return false;
		}
		switch (roleInSurveyGroup) {
		case OWNER, ADMINISTRATOR, SUPERVISOR, DATA_CLEANER_LIMITED, DATA_ANALYZER, OPERATOR:
			return true;
		default:
			return false;
		}
	}

	public boolean canCreateRecords(User user, UserGroupRole roleInSurveyGroup) {
		return user.getRole() != UserRole.ENTRY_LIMITED && canEditRecords(user, roleInSurveyGroup);
	}

	public boolean canEdit(User user, UserGroupRole roleInSurveyGroup, Step step, User recordOwner) {
		if (!canEditRecords(user, roleInSurveyGroup) || !canView(user, roleInSurveyGroup, recordOwner)
				|| !(recordOwner == null || isOwner(user, recordOwner) || user.hasEffectiveRole(UserRole.CLEANSING))) {
			return false;
		}
		switch (step) {
		case ENTRY:
			return user.hasEffectiveRole(UserRole.ENTRY) || isOwner(user, recordOwner);
		case CLEANSING:
			return user.hasEffectiveRole(UserRole.CLEANSING);
		default:
			return false;
		}
	}

	public boolean canDemote(User user, UserGroupRole roleInSurveyGroup, Step step, User recordOwner) {
		return step == Step.ANALYSIS ? user.hasEffectiveRole(UserRole.ANALYSIS)
				: canEdit(user, roleInSurveyGroup, step, recordOwner);
	}

	/**
	 * Same permission is required to promote or demote records in bulk
	 */
	public boolean canChangeRecordOwner(User user, UserGroupRole roleInSurveyGroup) {
		return user.hasEffectiveRole(UserRole.CLEANSING) && canImportRecords(roleInSurveyGroup);
	}

	public boolean canImportRecords(UserGroupRole roleInSurveyGroup) {
		if (roleInSurveyGroup == null) {
			return false;
		}
		switch (roleInSurveyGroup) {
		case OWNER, ADMINISTRATOR, SUPERVISOR, DATA_ANALYZER:
			return true;
		default:
			return false;
		}
	}

	private boolean isOwner(User user, User recordOwner) {
		return recordOwner != null && user.getId().equals(recordOwner.getId());
	}

	public boolean canDeleteRecords(final User user, UserGroupRole roleInSurveyGroup, Collection<CollectRecordSummary> recordSummaries) {
		switch(user.getRole()) {
		case VIEW:
		case ENTRY_LIMITED:
			return false;
		case ENTRY:
			return containsOnlyEntryOwnedRecords(recordSummaries, user);
		default:
			switch (roleInSurveyGroup) {
			case VIEWER:
				return false;
			case OPERATOR:
			case DATA_CLEANER_LIMITED:
				return containsOnlyEntryOwnedRecords(recordSummaries, user);
			default:
				return true;
			}
		}
	}

	private boolean containsOnlyEntryOwnedRecords(Collection<CollectRecordSummary> recordSummaries, final User user) {
		return CollectionUtils.find(recordSummaries, new Predicate<CollectRecordSummary>() {
			public boolean evaluate(CollectRecordSummary s) {
				return s.getStep() != Step.ENTRY || (s.getOwner() != null && s.getOwner().getId() != user.getId());
			}
		}) == null;
	}

}
