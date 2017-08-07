package org.openforis.collect.manager;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.model.CollectRecord.Step;

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

}
