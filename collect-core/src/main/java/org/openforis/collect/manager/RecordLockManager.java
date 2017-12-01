package org.openforis.collect.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openforis.collect.model.RecordLock;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordLockedByActiveUserException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordUnlockedException;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordLockManager {
	
	private long timeoutMillis;
	private Map<Integer, RecordLock> locks;
	
	public RecordLockManager(long lockTimeoutMillis) {
		this.timeoutMillis = lockTimeoutMillis;
		locks = new HashMap<Integer, RecordLock>();
	}
	
	public synchronized void releaseLock(int recordId) {
		RecordLock lock = getLock(recordId);
		if ( lock != null ) {
			locks.remove(recordId);
		}
	}
	
	public synchronized boolean checkIsLocked(int recordId, User user, String sessionId) throws RecordUnlockedException {
		RecordLock lock = getLock(recordId);
		String lockUserName = null;
		if ( lock != null) {
			String lockSessionId = lock.getSessionId();
			int lockRecordId = lock.getRecordId();
			User lUser = lock.getUser();
			if( recordId == lockRecordId  && 
					( lUser == user || lUser.getId().equals(user.getId()) ) &&  
						lockSessionId.equals(sessionId) ) {
				lock.keepAlive();
				return true;
			} else {
				User lockUser = lock.getUser();
				lockUserName = lockUser.getUsername();
			}
		}
		throw new RecordUnlockedException(lockUserName);
	}
	
	public synchronized void lock(int recordId, User user, String sessionId) throws RecordLockedException, MultipleEditException {
		lock(recordId, user, sessionId, false);
	}
	
	public synchronized void lock(int recordId, User user, String sessionId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock oldLock = getLock(recordId);
		if ( oldLock != null ) {
			locks.remove(recordId);
		}
		RecordLock lock = new RecordLock(sessionId, recordId, user, timeoutMillis);
		locks.put(recordId, lock);
	}

	private boolean isForceUnlockAllowed(User user, RecordLock lock) {
		boolean isAdmin = user.hasRole(UserRole.ADMIN);
		Integer userId = user.getId();
		User lockUser = lock.getUser();
		return isAdmin || userId.equals(lockUser.getId());
	}
	
	public synchronized boolean isLockAllowed(User user, int recordId, String sessionId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock uLock = getLockBySessionId(sessionId);
		if ( uLock != null ) {
			throw new MultipleEditException("User is editing another record: " + uLock.getRecordId());
		}
		RecordLock lock = getLock(recordId);
		if ( lock == null || ( forceUnlock && isForceUnlockAllowed(user, lock) ) ) {
			return true;
		} else if ( lock.getUser().getId().equals(user.getId()) ) {
			throw new RecordLockedByActiveUserException(user.getUsername());
		} else {
			String lockingUserName = lock.getUser().getUsername();
			throw new RecordLockedException("Record already locked", lockingUserName);
		}
	}
	
	public synchronized boolean isLocked(int recordId) {
		RecordLock lock = getLock(recordId);
		return lock != null;	
	}
	
	public synchronized RecordLock getLock(int recordId) {
		clearInactiveLocks();
		RecordLock lock = locks.get(recordId);
		return lock;
	}
	
	private synchronized RecordLock getLockBySessionId(String sessionId) {
		clearInactiveLocks();
		Collection<RecordLock> lcks = locks.values();
		for (RecordLock l : lcks) {
			if ( l.getSessionId().equals(sessionId) ) {
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
}
