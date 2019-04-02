package org.openforis.collect.manager;

import java.util.Collection;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserInGroup.UserGroupRole;
import org.openforis.collect.model.UserRole;

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
				return containsOnlyEntryOwnedRecords(recordSummaries, user);
			default:
				return true;
			}
		}
	}

	private boolean containsOnlyEntryOwnedRecords(Collection<CollectRecordSummary> recordSummaries, final User user) {
		return IterableUtils.find(recordSummaries, new Predicate<CollectRecordSummary>() {
			public boolean evaluate(CollectRecordSummary s) {
				return s.getStep() != Step.ENTRY || (s.getOwner() != null && s.getOwner().getId() != user.getId());
			}
		}) == null;
	}

}
