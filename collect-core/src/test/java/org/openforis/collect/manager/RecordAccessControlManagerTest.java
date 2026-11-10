package org.openforis.collect.manager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openforis.collect.model.CollectRecord.Step.ANALYSIS;
import static org.openforis.collect.model.CollectRecord.Step.CLEANSING;
import static org.openforis.collect.model.CollectRecord.Step.ENTRY;
import static org.openforis.collect.model.UserInGroup.UserGroupRole.DATA_CLEANER_LIMITED;
import static org.openforis.collect.model.UserInGroup.UserGroupRole.OPERATOR;
import static org.openforis.collect.model.UserInGroup.UserGroupRole.OWNER;
import static org.openforis.collect.model.UserInGroup.UserGroupRole.VIEWER;

import org.junit.Test;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;

public class RecordAccessControlManagerTest {

	private final RecordAccessControlManager manager = new RecordAccessControlManager();
	private final User other = user(99, UserRole.ENTRY);

	@Test
	public void entryUserCanEditOnlyOwnedOrUnownedEntryRecordsWithEditingGroupRole() {
		User entry = user(1, UserRole.ENTRY);
		assertTrue(manager.canEdit(entry, OPERATOR, ENTRY, entry));
		assertTrue(manager.canEdit(entry, OPERATOR, ENTRY, null));
		assertFalse(manager.canEdit(entry, OPERATOR, ENTRY, other));
		assertFalse(manager.canEdit(entry, VIEWER, ENTRY, entry));
		assertFalse(manager.canEdit(entry, null, ENTRY, entry));
		assertFalse(manager.canEdit(entry, OPERATOR, CLEANSING, null));
	}

	@Test
	public void limitedUsersCanAccessOnlyOwnedRecords() {
		User entryLimited = user(1, UserRole.ENTRY_LIMITED);
		assertTrue(manager.canView(entryLimited, OPERATOR, entryLimited));
		assertFalse(manager.canView(entryLimited, OPERATOR, other));
		assertTrue(manager.canEdit(entryLimited, OPERATOR, ENTRY, entryLimited));
		assertFalse(manager.canEdit(entryLimited, OPERATOR, ENTRY, null));
		assertFalse(manager.canCreateRecords(entryLimited, OPERATOR));

		User limitedCleaner = user(2, UserRole.CLEANSING);
		assertFalse(manager.canView(limitedCleaner, DATA_CLEANER_LIMITED, other));
		assertFalse(manager.canEdit(limitedCleaner, DATA_CLEANER_LIMITED, CLEANSING, null));
	}

	@Test
	public void cleanserCanEditRecordsOfOthersButNotInAnalysis() {
		User cleanser = user(1, UserRole.CLEANSING);
		assertTrue(manager.canEdit(cleanser, OPERATOR, ENTRY, other));
		assertTrue(manager.canEdit(cleanser, OPERATOR, CLEANSING, other));
		assertFalse(manager.canEdit(cleanser, OPERATOR, ANALYSIS, null));
		assertFalse(manager.canDemote(cleanser, OPERATOR, ANALYSIS, null));
		assertTrue(manager.canDemote(cleanser, OPERATOR, CLEANSING, other));
		assertTrue(manager.canDemote(user(2, UserRole.ANALYSIS), OPERATOR, ANALYSIS, null));
	}

	@Test
	public void viewUserCannotChangeRecords() {
		User view = user(1, UserRole.VIEW);
		assertTrue(manager.canView(view, OWNER, other));
		assertFalse(manager.canEditRecords(view, OWNER));
		assertFalse(manager.canCreateRecords(view, OWNER));
		assertFalse(manager.canChangeRecordOwner(view, OWNER));
	}

	@Test
	public void ownerChangeAndImportRequireSupervisingGroupRole() {
		User cleanser = user(1, UserRole.CLEANSING);
		assertTrue(manager.canChangeRecordOwner(cleanser, OWNER));
		assertFalse(manager.canChangeRecordOwner(cleanser, OPERATOR));
		assertFalse(manager.canChangeRecordOwner(user(2, UserRole.ENTRY), OWNER));
		assertTrue(manager.canImportRecords(OWNER));
		assertFalse(manager.canImportRecords(OPERATOR));
		assertFalse(manager.canImportRecords(null));
	}

	private static User user(int id, UserRole role) {
		User user = new User(id, "user" + id);
		user.setRole(role);
		return user;
	}
}
