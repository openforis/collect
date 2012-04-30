package org.openforis.collect.remoting.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.model.RecordLock;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordAlreadyLockedException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordUnlockedException;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordLocker implements org.openforis.collect.model.RecordLocker {
	
	private Map<Integer, RecordLock> locks;
	
	private long timeoutMillis = 60000;
	
	public RecordLocker() {
		super();
		this.locks = new HashMap<Integer, RecordLock>();
	}

	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#lock(org.openforis.collect.model.CollectRecord, org.openforis.collect.model.User, java.lang.String)
	 */
	@Override
	public synchronized void lock(int recordId, User user, String lockId) throws RecordLockedException, MultipleEditException {
		lock(recordId, user, lockId, false);
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#lock(org.openforis.collect.model.CollectRecord, org.openforis.collect.model.User, java.lang.String, boolean)
	 */
	@Override
	public synchronized void lock(int recordId, User user, String lockId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		isLockAllowed(user, recordId, lockId, forceUnlock);
		
		RecordLock oldLock = getLock(recordId);
		if ( oldLock != null ) {
			locks.remove(recordId);
		}
		RecordLock lock = new RecordLock(lockId, recordId, user, timeoutMillis);
		locks.put(recordId, lock);
	}

	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#release(org.openforis.collect.model.CollectRecord)
	 */
	@Override
	public synchronized void releaseLock(int recordId) {
		RecordLock lock = getLock(recordId);
		if ( lock != null ) {
			locks.remove(recordId);
		}
	}

	private boolean isForceUnlockAllowed(User user, RecordLock lock) {
		boolean isAdmin = user.hasRole("ROLE_ADMIN");
		Integer userId = user.getId();
		User lockUser = lock.getUser();
		return isAdmin || userId.equals(lockUser.getId());
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
	private synchronized boolean isLockAllowed(User user, int recordId, String lockId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock uLock = getLock(lockId);
		if ( uLock != null ) {
			throw new MultipleEditException("User is editing another record: " + uLock.getRecordId());
		}
		RecordLock lock = getLock(recordId);
		if ( lock == null || ( forceUnlock && isForceUnlockAllowed(user, lock) ) ) {
			return true;
		} else if ( lock.getUser().getId().equals(user.getId()) ) {
			throw new RecordAlreadyLockedException(user.getName());
		} else {
			String lockingUserName = lock.getUser().getName();
			throw new RecordLockedException("Record already locked", lockingUserName);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#isLocked(int)
	 */
	@Override
	public synchronized boolean isLocked(int recordId) {
		RecordLock lock = getLockByRecordId(recordId);
		return lock != null;	
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#isLocked(org.openforis.collect.model.CollectRecord, org.openforis.collect.model.User, java.lang.String)
	 */
	@Override
	public synchronized boolean checkIsLocked(int recordId, User user, String lockId) throws RecordUnlockedException {
		RecordLock lock = getLock(recordId);
		String lockingUserName = null;
		if ( lock != null) {
			String lockInnerId = lock.getId();
			int lockRecordId = lock.getRecordId();
			User lUser = lock.getUser();
			if( recordId == lockRecordId  && 
					( lUser == user || lUser.getId() == user.getId() ) &&  
					lockInnerId.equals(lockId) ) {
				lock.keepAlive();
				return true;
			} else {
				User lockUser = lock.getUser();
				lockingUserName = lockUser.getName();
			}
		}
		throw new RecordUnlockedException(lockingUserName);
	}

	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#getLockUser(int)
	 */
	@Override
	public synchronized User getLockUser(int recordId) {
		RecordLock lock = getLockByRecordId(recordId);
		return lock != null ? lock.getUser(): null;
	}
	
	private synchronized RecordLock getLock(int recordId) {
		clearInactiveLocks();
		RecordLock lock = locks.get(recordId);
		return lock;
	}
	
	/**
	 * Returns the lock of the record with the given id
	 * 
	 * @param recordId
	 * @return
	 */
	private synchronized RecordLock getLockByRecordId(int recordId) {
		clearInactiveLocks();
		Collection<RecordLock> lcks = locks.values();
		for (RecordLock l : lcks) {
			Integer lockRecordId = l.getRecordId();
			if ( lockRecordId != null && lockRecordId == recordId ) {
				return l;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.openforis.collect.web.session.RecordLocker#getLock(java.lang.String)
	 */
	@Override
	public synchronized RecordLock getLock(String lockId) {
		clearInactiveLocks();
		Collection<RecordLock> lcks = locks.values();
		for (RecordLock l : lcks) {
			if ( l.getId().equals(lockId) ) {
				return l;
			}
		}
		return null;
	}
	
	private synchronized void clearInactiveLocks() {
		Set<Entry<Integer, RecordLock>> entrySet = locks.entrySet();
		Iterator<Entry<Integer, RecordLock>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, RecordLock> entry = iterator.next();
			RecordLock lock = entry.getValue();
			if( !lock.isActive() ){
				iterator.remove();
			}
		}
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}
}

