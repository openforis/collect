package org.openforis.collect.model;

import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordUnlockedException;

public interface RecordLocker {

	public abstract void lock(int recordId, User user, String lockId) throws RecordLockedException, MultipleEditException;

	public abstract void lock(int recordId, User user, String lockId, boolean forceUnlock) throws RecordLockedException, MultipleEditException;

	public abstract void release(int recordId);

	public abstract boolean isLocked(int recordId);

	/**
	 * Checks if the record is locked by the specified and 
	 * throws an exception it is has been unlocked by another user.
	 * 
	 * @param record
	 * @param userId
	 * @param lockId
	 * @throws RecordUnlockedException
	 */
	public abstract boolean checkIsLocked(int recordId, User user, String lockId) throws RecordUnlockedException;

	public abstract User getLockUser(int recordId);

	public abstract int getLockRecordId(String lockId);

	/**
	 * Returns the lock of the record with the given id
	 * 
	 * @param recordId
	 * @return
	 */
	public abstract RecordLock getLock(String lockId);

}