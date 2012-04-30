package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordAlreadyLockedException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordUnlockedException;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordLocker {
	
	private Map<String, RecordLock> locks;
	
	private long timeoutMillis;
	
	public RecordLocker(long timeoutMillis) {
		super();
		this.timeoutMillis = timeoutMillis;
		this.locks = new HashMap<String, RecordLock>();
	}

	public synchronized void lock(CollectRecord record, User user, String lockId) throws RecordLockedException, MultipleEditException {
		lock(record, user, lockId, false);
	}
	
	public synchronized void lock(CollectRecord record, User user, String lockId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		checkCanLock(user, record, lockId, forceUnlock);
		
		RecordLock oldLock = getLock(record);
		if ( oldLock != null ) {
			locks.remove(oldLock.getId());
		}
		RecordLock lock = new RecordLock(lockId, record, user, timeoutMillis);
		locks.put(lockId, lock);
	}

	public synchronized void release(CollectRecord record) {
		RecordLock lock = getLock(record);
		if ( lock != null ) {
			locks.remove(lock.getId());
		}
	}

	private boolean canForceUnlock(User user, RecordLock lock) {
		boolean isAdmin = user.hasRole("ROLE_ADMIN");
		Integer userId = user.getId();
		Integer lUserId = lock.getUser().getId();
		return isAdmin || userId.equals(lUserId);
	}
	
	/**
	 * Verify if the user can lock a record.
	 * If there is no lock on the record or forceUnlock is true and the user has role "ROLE_ADMIN" 
	 * or the user is the owner of the lock, than the method returns true, otherwise:
	 * - throws MultipleEditException if a user is editing another record using the same lockId.
	 * - throws RecordLockedException if a record is locked by another user.
	 * - throws RecordAlreadyLockedException if a record is locked by the specified user.
	 * 
	 * @param user
	 * @param record
	 * @param lockId
	 * @param forceUnlock
	 * @return
	 * @throws RecordLockedException
	 * @throws MultipleEditException
	 */
	private synchronized boolean checkCanLock(User user, CollectRecord record, String lockId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock uLock = getLock(lockId);
		if ( uLock != null ) {
			throw new MultipleEditException("User is editing another record: " + uLock.getRecord().getId());
		}
		RecordLock lock = getLock(record);
		if ( lock == null || ( canForceUnlock(user, lock) && forceUnlock ) ) {
			return true;
		} else if ( lock.getUser().getId().equals(user.getId()) ) {
			throw new RecordAlreadyLockedException(user.getName());
		} else {
			String lockingUserName = lock.getUser().getName();
			throw new RecordLockedException("Record already locked", lockingUserName);
		}
	}
	
	/**
	 * Check if the record is locked by the specified and 
	 * throws an exception it is has been unlocked by another user.
	 * 
	 * @param record
	 * @param userId
	 * @param lockId
	 * @throws RecordUnlockedException
	 */
	public synchronized void checkIsLocked(CollectRecord record, User user, String lockId)
			throws RecordUnlockedException {
		if ( record == null || ! isLocked(record, user, lockId) ) {
			String lockingUserName = null;
			if (record != null) {
				User lUser = getLockingUser(record);
				if ( lUser != null ) {
					lockingUserName = lUser.getName();
				}
			}
			throw new RecordUnlockedException(lockingUserName);
		} else {
			RecordLock lock = getLock(record);
			lock.keepAlive();
		}
	}
	
	public synchronized boolean isLocked(int recordId) {
		RecordLock lock = getLockByRecord(recordId);
		return lock != null;	
	}
	
	public synchronized boolean isLocked(CollectRecord record, User user, String lockId) {
		RecordLock lock = getLock(record);
		if ( lock != null) {
			String lId = lock.getId();
			CollectRecord lRecord = lock.getRecord();
			User lUser = lock.getUser();
			return ( record == lRecord || record.getId() == lRecord.getId() ) && 
					( lUser == user || lUser.getId() == user.getId() ) &&  
					lId.equals(lockId);
		} else {
			return false;
		}
	}
	
	public synchronized User getLockingUser(int recordId) {
		RecordLock lock = getLockByRecord(recordId);
		return lock != null ? lock.getUser(): null;
	}
	
	public synchronized User getLockingUser(CollectRecord record) {
		RecordLock lock = getLock(record);
		return lock != null ? lock.getUser(): null;
	}
	
	public synchronized CollectRecord getLockedRecord(String lockId) {
		RecordLock lock = getLock(lockId);
		return lock != null ? lock.getRecord(): null;
	}
	
	private synchronized RecordLock getLock(String lockId) {
		RecordLock lock = locks.get(lockId);
		return checkLockIsActive(lock) ? lock: null;
	}
	
	private synchronized RecordLock getLock(CollectRecord record) {
		clearInactive();
		Collection<RecordLock> lcks = locks.values();
		for (RecordLock l : lcks) {
			CollectRecord lRecord = l.getRecord();
			if ( lRecord == record || lRecord.getId() == record.getId() ) {
				return l;
			}
		}
		return null;
	}
	
	private synchronized RecordLock getLockByRecord(int recordId) {
		clearInactive();
		Collection<RecordLock> lcks = locks.values();
		for (RecordLock l : lcks) {
			CollectRecord lRecord = l.getRecord();
			if ( lRecord.getId() == recordId ) {
				return l;
			}
		}
		return null;
	}
	
	private synchronized boolean checkLockIsActive(RecordLock lock) {
		if ( lock != null) {
			if ( lock.isActive() ) {
				return true;
			} else {
				locks.remove(lock.getId());
			}
		}
		return false;
	}
	
	private synchronized void clearInactive() {
		Collection<RecordLock> lcks = locks.values();
		Collection<RecordLock> toBeRemoved = new ArrayList<RecordLock>();
		for (RecordLock l : lcks) {
			if ( ! l.isActive() ) {
				toBeRemoved.add(l);
			}
		}
		for (RecordLock l : toBeRemoved) {
			locks.remove(l);
		}
	}
	
}
