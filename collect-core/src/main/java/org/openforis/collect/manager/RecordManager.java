/**
 * 
 */
package org.openforis.collect.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordLock;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.RecordLockedByActiveUserException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager {
//	private final Log log = LogFactory.getLog(RecordManager.class);
	
	@Autowired
	private RecordDao recordDao;
	private RecordConverter recordConverter;
	private Map<Integer, RecordLock> locks;
	private long lockTimeoutMillis;
	private boolean lockingEnabled;
	
	public RecordManager() {
		this(true);
	}
	
	public RecordManager(boolean recordLockingEnabled) {
		super();
		this.lockingEnabled = recordLockingEnabled;
		lockTimeoutMillis = 60000;
		recordConverter = new RecordConverter();
	}

	protected void init() {
		locks = new HashMap<Integer, RecordLock>();
	}
	
	@Transactional
	public void save(CollectRecord record, String sessionId) throws RecordPersistenceException {
		User user = record.getModifiedBy();
		
		record.updateRootEntityKeyValues();
		checkAllKeysSpecified(record);
		
		record.updateEntityCounts();

		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
			id = record.getId();
			//todo fix: concurrency problem may occur..
			if ( isLockingEnabled() ) {
				lock(id, user, sessionId);
			}
		} else {
			if ( isLockingEnabled() ) {
				checkIsLocked(id, user, sessionId);
			}
			recordDao.update(record);
		}
	}

	@Transactional
	public void delete(int recordId) throws RecordPersistenceException {
		if ( isLockingEnabled() && isLocked(recordId) ) {
			RecordLock lock = getLock(recordId);
			User lockUser = lock.getUser();
			throw new RecordLockedException(lockUser.getName());
		} else {
			recordDao.delete(recordId);
		}
	}

	/**
	 * Returns a record and lock it
	 * 
	 * @param survey
	 * @param user
	 * @param recordId
	 * @param step
	 * @param sessionId
	 * @param forceUnlock
	 * @return
	 * @throws RecordLockedException
	 * @throws MultipleEditException
	 */
	@Transactional
	public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step, String sessionId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		if ( isLockingEnabled() ) {
			isLockAllowed(user, recordId, sessionId, forceUnlock);
			lock(recordId, user, sessionId, forceUnlock);
		}
		return load(survey, recordId, step);
	}
	
	@Transactional
	public CollectRecord load(CollectSurvey survey, int recordId, int step) {
		CollectRecord record = recordDao.load(survey, recordId, step);
		recordConverter.convertToLatestVersion(record);
		return record;
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity) {
		return loadSummaries(survey, rootEntity, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return recordDao.loadSummaries(survey, rootEntity, keys);
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, List<RecordSummarySortField> sortFields, String... keyValues) {
		List<CollectRecord> recordsSummary = recordDao.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, sortFields, keyValues);
		return recordsSummary;
	}

	@Transactional
	public int getRecordCount(CollectSurvey survey, String rootEntity, String... keyValues) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntity);
		int count = recordDao.countRecords(survey.getId(), rootEntityDefinition.getId(), keyValues);
		return count;
	}
	
	@Transactional
	public boolean hasAssociatedRecords(int userId) {
		return recordDao.hasAssociatedRecords(userId);
	}

	@Transactional
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName, String sessionId) throws RecordPersistenceException {
		CollectRecord record = new CollectRecord(survey, modelVersionName);
		record.createRootEntity(rootEntityDefinition.getName());
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		record.setStep(Step.ENTRY);
		return record;
	}

	@Transactional
	public void promote(CollectRecord record, User user) throws RecordPromoteException, MissingRecordKeyException {
		Integer errors = record.getErrors();
		Integer skipped = record.getSkipped();
		Integer missing = record.getMissingErrors();
		int totalErrors = errors + skipped + missing;
		if( totalErrors > 0 ){
			throw new RecordPromoteException("Record cannot be promoted becuase it contains errors.");
		}
		record.updateRootEntityKeyValues();
		checkAllKeysSpecified(record);
		record.updateEntityCounts();

		Integer id = record.getId();
		// before save record in current step
		if( id == null ) {
			recordDao.insert( record );
		} else {
			recordDao.update( record );
		}

		record.applyDefaultValues();
		
		//change step and update the record
		Step currentStep = record.getStep();
		Step nextStep = currentStep.getNext();
		Date now = new Date();
		record.setModifiedBy( user );
		record.setModifiedDate( now );
		record.setState( null );
		
		/**
		 * 1. clear node states
		 * 2. update record step
		 * 3. update all validation states
		 */
		record.clearNodeStates();
		record.setStep( nextStep );
		record.updateDerivedStates();
		
		recordDao.update( record );
	}

	@Transactional
	public void demote(CollectSurvey survey, int recordId, Step currentStep, User user) throws RecordPersistenceException {
		Step prevStep = currentStep.getPrevious();
		CollectRecord record = recordDao.load( survey, recordId, prevStep.getStepNumber() );
		record.setModifiedBy( user );
		record.setModifiedDate( new Date() );
		record.setStep( prevStep );
		record.setState( State.REJECTED );
		record.updateDerivedStates();
		recordDao.update( record );
	}
	
	@Transactional
	public void validate(CollectSurvey survey, User user, String sessionId, int recordId, Step step) throws RecordLockedException, MultipleEditException {
		if ( isLockingEnabled() ) {
			isLockAllowed(user, recordId, sessionId, true);
			lock(recordId, user, sessionId, true);
		}
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber());
		Entity rootEntity = record.getRootEntity();
		record.addEmptyNodes(rootEntity);
		record.updateDerivedStates();
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		recordDao.update(record);
		if ( isLockingEnabled() ) {
			releaseLock(recordId);
		}
	}
	
	public void moveNode(CollectRecord record, int nodeId, int index) {
		Node<?> node = record.getNodeByInternalId(nodeId);
		Entity parent = node.getParent();
		String name = node.getName();
		List<Node<?>> siblings = parent.getAll(name);
		int oldIndex = siblings.indexOf(node);
		parent.move(name, oldIndex, index);
	}
	
	private void checkAllKeysSpecified(CollectRecord record) throws MissingRecordKeyException {
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		List<AttributeDefinition> keyAttributeDefns = rootEntityDefn.getKeyAttributeDefinitions();
		for (int i = 0; i < keyAttributeDefns.size(); i++) {
			AttributeDefinition keyAttrDefn = keyAttributeDefns.get(i);
			if ( rootEntity.isRequired(keyAttrDefn.getName()) ) {
				String keyValue = rootEntityKeyValues.get(i);
				if ( StringUtils.isBlank(keyValue) ) {
					throw new MissingRecordKeyException();
				}
			}
		}
	}
	
	/* --- START OF LOCKING METHODS --- */
	
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
					( lUser == user || lUser.getId() == user.getId() ) &&  
					lockSessionId.equals(sessionId) ) {
				lock.keepAlive();
				return true;
			} else {
				User lockUser = lock.getUser();
				lockUserName = lockUser.getName();
			}
		}
		throw new RecordUnlockedException(lockUserName);
	}
	
	private synchronized void lock(int recordId, User user, String sessionId) throws RecordLockedException, MultipleEditException {
		lock(recordId, user, sessionId, false);
	}
	
	private synchronized void lock(int recordId, User user, String sessionId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock oldLock = getLock(recordId);
		if ( oldLock != null ) {
			locks.remove(recordId);
		}
		RecordLock lock = new RecordLock(sessionId, recordId, user, lockTimeoutMillis);
		locks.put(recordId, lock);
	}

	private boolean isForceUnlockAllowed(User user, RecordLock lock) {
		boolean isAdmin = user.hasRole("ROLE_ADMIN");
		Integer userId = user.getId();
		User lockUser = lock.getUser();
		return isAdmin || userId.equals(lockUser.getId());
	}
	
	private synchronized boolean isLockAllowed(User user, int recordId, String sessionId, boolean forceUnlock) throws RecordLockedException, MultipleEditException {
		RecordLock uLock = getLockBySessionId(sessionId);
		if ( uLock != null ) {
			throw new MultipleEditException("User is editing another record: " + uLock.getRecordId());
		}
		RecordLock lock = getLock(recordId);
		if ( lock == null || ( forceUnlock && isForceUnlockAllowed(user, lock) ) ) {
			return true;
		} else if ( lock.getUser().getId().equals(user.getId()) ) {
			throw new RecordLockedByActiveUserException(user.getName());
		} else {
			String lockingUserName = lock.getUser().getName();
			throw new RecordLockedException("Record already locked", lockingUserName);
		}
	}
	
	private synchronized boolean isLocked(int recordId) {
		RecordLock lock = getLock(recordId);
		return lock != null;	
	}
	
	private synchronized RecordLock getLock(int recordId) {
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

	/* --- END OF LOCKING METHODS --- */

	public long getLockTimeoutMillis() {
		return lockTimeoutMillis;
	}

	public void setLockTimeoutMillis(long timeoutMillis) {
		this.lockTimeoutMillis = timeoutMillis;
	}
	
	public boolean isLockingEnabled() {
		return lockingEnabled;
	}
	
	public void setLockingEnabled(boolean lockingEnabled) {
		this.lockingEnabled = lockingEnabled;
	}
	
	public RecordDao getRecordDao() {
		return recordDao;
	}
	
	public void setRecordDao(RecordDao recordDao) {
		this.recordDao = recordDao;
	}
}

