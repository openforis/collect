/**
 * 
 */
package org.openforis.collect.manager;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordLock;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordNotOwnedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.persistence.RecordValidationInProgressException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager {

//	private final Log log = LogFactory.getLog(RecordManager.class);
	
	private static final int DEFAULT_LOCK_TIMEOUT_MILLIS = 300000;
	
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SurveyManager surveyManager;
	
	private RecordUpdater updater;
	private RecordConverter recordConverter;
	private long lockTimeoutMillis;
	private boolean lockingEnabled;
	private RecordLockManager lockManager;

	private RecordAccessControlManager accessControlManager;
	
	public RecordManager() {
		this(true);
	}
	
	public RecordManager(boolean lockingEnabled) {
		super();
		this.lockingEnabled = lockingEnabled;
		this.lockTimeoutMillis = DEFAULT_LOCK_TIMEOUT_MILLIS;
		this.updater = new RecordUpdater();
		this.recordConverter = new RecordConverter();
		this.accessControlManager = new RecordAccessControlManager();
	}

	protected void init() {
		lockManager = new RecordLockManager(lockTimeoutMillis);
	}
	
	@Transactional
	public void save(CollectRecord record) {
		try {
			save(record, null);
		} catch (RecordPersistenceException e) {
			//it should never happen when record locking is not being used
			throw new RuntimeException(e);
		}
	}
	
	@Transactional
	public void save(CollectRecord record, String sessionId) throws RecordPersistenceException {
		User lockingUser = record.getModifiedBy();
		save(record, lockingUser, sessionId);
	}
	
	@Transactional
	public void save(CollectRecord record, User lockingUser, String sessionId) throws RecordPersistenceException {
		record.updateRootEntityKeyValues();
		checkAllKeysSpecified(record);
		
		record.updateEntityCounts();

		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
			id = record.getId();
			//todo fix: concurrency problem may occur..
			if ( sessionId != null && isLockingEnabled() ) {
				lockManager.lock(id, lockingUser, sessionId);
			}
		} else {
			if ( sessionId != null && isLockingEnabled() ) {
				lockManager.checkIsLocked(id, lockingUser, sessionId);
			}
			recordDao.update(record);
		}
	}

	@Transactional
	public void delete(int recordId) throws RecordPersistenceException {
		if ( isLockingEnabled() && lockManager.isLocked(recordId) ) {
			RecordLock lock = lockManager.getLock(recordId);
			User lockUser = lock.getUser();
			throw new RecordLockedException(lockUser.getName());
		} else {
			recordDao.delete(recordId);
		}
	}
	
	@Transactional
	public void assignOwner(CollectSurvey survey, int recordId, Integer ownerId, User user, String sessionId) 
			throws RecordLockedException, MultipleEditException {
		if ( isLockingEnabled() ) {
			checkSurveyRecordValidationNotInProgress(survey);
			lockManager.isLockAllowed(user, recordId, sessionId, false);
			lockManager.lock(recordId, user, sessionId, false);
		}
		try {
			recordDao.assignOwner(recordId, ownerId);
		} finally {
			if ( isLockingEnabled() ) {
				releaseLock(recordId);
			}
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
	 * @throws RecordPersistence
	 */
	@Deprecated
	@Transactional
	public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step, String sessionId, boolean forceUnlock) throws RecordPersistenceException {
		return checkout(survey, user, recordId, Step.valueOf(step), sessionId, forceUnlock);
	}
	
	@Transactional
	public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, Step step, String sessionId, boolean forceUnlock) throws RecordPersistenceException {
		if(isLockingEnabled()) {
			checkSurveyRecordValidationNotInProgress(survey);
			lockManager.isLockAllowed(user, recordId, sessionId, forceUnlock);
			lockManager.lock(recordId, user, sessionId, forceUnlock);
		}
		CollectRecord record = load(survey, recordId, step);
		if ( ! accessControlManager.canEdit(user, record) ) {
			if ( isLockingEnabled() ) {
				releaseLock(recordId);
			}
			throw new RecordNotOwnedException(record.getOwner().getName());
		}
		if(isLockingEnabled()) {
			//refresh lock because record loading can be time consuming
			lockManager.lock(recordId, user, sessionId);
		}
		return record;
	}

	private void checkSurveyRecordValidationNotInProgress(CollectSurvey survey) throws RecordValidationInProgressException {
		if ( survey.isPublished() && ! survey.isWork() && surveyManager.isRecordValidationInProgress(survey.getId()) ) {
			throw new RecordValidationInProgressException();
		}
	}

	@Deprecated
	@Transactional
	public CollectRecord load(CollectSurvey survey, int recordId, int step) {
		Step stepEnum = Step.valueOf(step);
		return load(survey, recordId, stepEnum);
	}
	
	public CollectRecord load(CollectSurvey survey, int recordId) {
		Step lastStep = determineLastStep(survey, recordId);
		return load(survey, recordId, lastStep);
	}

	public CollectRecord load(CollectSurvey survey, int recordId, Step step) {
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber());
		recordConverter.convertToLatestVersion(record);
		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.initializeRecord(record);
		return record;
	}
	
	public byte[] loadBinaryData(CollectSurvey survey, int recordId, Step step) {
		byte[] result = recordDao.loadBinaryData(survey, recordId, step.getStepNumber());
		return result;
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity) {
		return loadSummaries(survey, rootEntity, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step) {
		return recordDao.loadSummaries(survey, rootEntity, step);
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return recordDao.loadSummaries(survey, rootEntity, keys);
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, List<RecordSummarySortField> sortFields, String... keyValues) {
		List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, sortFields, keyValues);
		return summaries;
	}

	@Transactional
	public List<CollectRecord> loadSummaries(RecordFilter filter) {
		return loadSummaries(filter, null);
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		List<CollectRecord> recordSummaries = recordDao.loadSummaries(filter, sortFields);
		return recordSummaries;
	}
	
	/**
	 * Returns only the records modified after the specified date.
	 */
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Date modifiedSince) {
		return recordDao.loadSummaries(survey, rootEntity, modifiedSince);
	}
	
	@Transactional
	public int countRecords(CollectSurvey survey) {
		return recordDao.countRecords(survey);
	}
	
	@Transactional
	public int countRecords(CollectSurvey survey, int rootEntityDefinitionId) {
		return recordDao.countRecords(survey, rootEntityDefinitionId);
	}
	
	@Transactional
	public int countRecords(CollectSurvey survey, int rootEntityDefinitionId, int dataStepNumber) {
		return recordDao.countRecords(survey, rootEntityDefinitionId, dataStepNumber);
	}
	
	@Transactional
	public int countRecords(RecordFilter filter) {
		return recordDao.countRecords(filter);
	}
	
	@Transactional
	public int countRecords(CollectSurvey survey, String rootEntity, String... keyValues) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntity);
		RecordFilter filter = new RecordFilter(survey, rootEntityDefn.getId());
		filter.setKeyValues(keyValues);
		return countRecords(filter);
	}

	/**
	 * Returns false if another record with the same root entity key values exists.
	 */
	public boolean isUnique(CollectRecord record) {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		record.updateRootEntityKeyValues();
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		
		Entity rootEntity = record.getRootEntity();
		List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntity.getName(), rootEntityKeyValues.toArray(new String[0]));
		for (CollectRecord collectRecord : summaries) {
			if ( ! collectRecord.getId().equals(record.getId()) ) {
				return false;
			}
		}
		return true;
	}
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName) throws RecordPersistenceException {
		return create(survey, rootEntityName, user, modelVersionName, (String) null);
	}
	
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName, String sessionId) throws RecordPersistenceException {
		return create(survey, rootEntityDefinition.getName(), user, modelVersionName, sessionId);
	}
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName, String sessionId) throws RecordPersistenceException {
		CollectRecord record = survey.createRecord(modelVersionName);
		record.createRootEntity(rootEntityName);
		record.setCreationDate(new Date());
		record.setCreatedBy(user);

		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.initializeRecord(record);
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
		// before promoting record, save it in current step
		if( id == null ) {
			recordDao.insert( record );
		} else {
			recordDao.update( record );
		}

		/**
		 * 1. clear node states
		 * 2. update record step
		 * 3. update all validation states
		 */
		record.setModifiedBy(user);
		record.setModifiedDate(new Date());
		record.setState(null);
		
		record.clearNodeStates();

		//change step and update the record
		Step currentStep = record.getStep();
		Step nextStep = currentStep.getNext();
		
		if ( accessControlManager.isOwnerToBeResetAfterPromoting(user, currentStep) ) {
			record.setOwner(null);
		}
		
		record.setStep( nextStep );

		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.applyDefaultValues(record);

		validate(record);
		
		recordDao.update( record );
	}

	@Transactional
	public void demote(CollectSurvey survey, int recordId, Step currentStep, User user) throws RecordPersistenceException {
		Step prevStep = currentStep.getPrevious();
		CollectRecord record = recordDao.load( survey, recordId, prevStep.getStepNumber() );
		record.setModifiedBy( user );
		record.setModifiedDate( new Date() );
		record.setStep( prevStep );
		record.setOwner(null);
		record.setState( State.REJECTED );
		validate(record);
		recordDao.update( record );
	}
	
	@Transactional
	public void validateAndSave(CollectSurvey survey, User user, String sessionId, int recordId, Step step) throws RecordLockedException, MultipleEditException {
		if ( isLockingEnabled() ) {
			lockManager.isLockAllowed(user, recordId, sessionId, true);
			lockManager.lock(recordId, user, sessionId, true);
		}
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber());
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		recordDao.update(record);
		if ( isLockingEnabled() ) {
			lockManager.releaseLock(recordId);
		}
	}

	//START OF RECORD UPDATE METHODS
	/**
	 * Updates an attribute with a new value
	 * 
	 * @param attribute
	 * @param value
	 * @return
	 */
	public <V extends Value> NodeChangeSet updateAttribute(
			Attribute<? extends NodeDefinition, V> attribute,
			V value) {
		return updater.updateAttribute(attribute, value);
	}
	
	/**
	 * Updates an attribute and sets the specified FieldSymbol on every field
	 * 
	 * @param attribute
	 * @param value
	 * @return
	 */
	public NodeChangeSet updateAttribute(
			Attribute<?, ?> attribute,
			FieldSymbol symbol) {
		return updater.updateAttribute(attribute, symbol);
	}
	
	/**
	 * Updates a field with a new value.
	 * The value will be parsed according to field data type.
	 * 
	 * @param field
	 * @param value 
	 * @return
	 */
	public <V> NodeChangeSet updateField(
			Field<V> field, V value) {
		return updater.updateField(field, value);
	}
	
	/**
	 * Updates a field with a new symbol.
	 * 
	 * @param field
	 * @param symbol 
	 * @return
	 */
	public <V> NodeChangeSet updateField(
			Field<V> field, FieldSymbol symbol) {
		return updater.updateField(field, symbol);
	}
	
	public NodeChangeSet addNode(Entity parentEntity, String nodeName) {
		return updater.addNode(parentEntity, nodeName);
	}
	
	/**
	 * Adds a new entity to a the record.
	 * 
	 * @param parentEntity
	 * @param entityName
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addEntity(Entity parentEntity, String entityName) {
		return updater.addEntity(parentEntity, entityName);
	}

	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName) {
		return updater.addAttribute(parentEntity, attributeName);
	}
	
	/**
	 * Adds a new attribute to a record.
	 * This attribute can be immediately populated with a value or with a FieldSymbol, and remarks.
	 * You cannot specify both value and symbol.
	 * 
	 * @param parentEntity Parent entity of the attribute
	 * @param attributeName Name of the attribute definition
	 * @param value Value to set on the attribute
	 * @param symbol FieldSymbol to set on each field of the attribute
	 * @param remarks Remarks to set on each field of the attribute
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName, Value value, FieldSymbol symbol, String remarks) {
		return updater.addAttribute(parentEntity, attributeName, value, symbol, remarks);
	}

	/**
	 * Updates the remarks of a Field
	 * 
	 * @param field
	 * @param remarks
	 * @return
	 */
	public NodeChangeSet updateRemarks(
			Field<?> field, String remarks) {
		return updater.updateRemarks(field, remarks);
	}

	public NodeChangeSet approveMissingValue(
			Entity parentEntity, String nodeName) {
		return updater.approveMissingValue(parentEntity, nodeName);
	}

	public NodeChangeSet confirmError(Attribute<?, ?> attribute) {
		return updater.confirmError(attribute);
	}
	
	/**
	 * Validate the entire record validating the value of each attribute and 
	 * the min/max count of each child node of each entity
	 * 
	 * @return 
	 */
	public void validate(final CollectRecord record) {
		updater.validate(record);
	}

	/**
	 * Deletes a node from the record.
	 * 
	 * @param node
	 * @return
	 */
	public NodeChangeSet deleteNode(Node<?> node) {
		return updater.deleteNode(node);
	}
	
	/**
	 * Applies the default value to an attribute, if any.
	 * The applied default value will be the first one having verified the "condition".
	 *  
	 * @param attribute
	 * @return
	 */
	public NodeChangeSet applyDefaultValue(
			Attribute<?, ?> attribute) {
		return updater.applyDefaultValue(attribute);
	}

	public void moveNode(CollectRecord record, int nodeId, int index) {
		updater.moveNode(record, nodeId, index);
	}
	
	//END OF RECORD UPDATE METHODS
	
	private void checkAllKeysSpecified(CollectRecord record) throws MissingRecordKeyException {
		Entity rootEntity = record.getRootEntity();
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		List<AttributeDefinition> keyDefns = rootEntity.getDefinition().getKeyAttributeDefinitions();
		for (int i = 0; i < keyDefns.size(); i++) {
			AttributeDefinition keyDefn = keyDefns.get(i);
			boolean required = keyDefn.getMinCount() != null && keyDefn.getMinCount() > 0;
			if ( required ) {
				String path = keyDefn.getPath();
				Node<?> keyNode = record.findNodeByPath(path);
				if ( keyNode == null ) {
					throw new MissingRecordKeyException();
				} else {
					String keyValue = rootEntityKeyValues.get(i);
					if ( StringUtils.isBlank(keyValue) ) {
						throw new MissingRecordKeyException();
					}
				}
			}
		}
	}
	
	private Step determineLastStep(CollectSurvey survey, int recordId) {
		RecordFilter filter = new RecordFilter(survey);
		filter.setRecordId(recordId);
		List<CollectRecord> summaries = recordDao.loadSummaries(filter, null);
		if ( summaries.isEmpty() ) {
			return null;
		} else {
			CollectRecord summary = summaries.get(0);
			Step lastStep = summary.getStep();
			return lastStep;
		}
	}
	
	public void checkIsLocked(int recordId, User user, String lockId) throws RecordUnlockedException {
		if ( lockingEnabled ) {
			lockManager.checkIsLocked(recordId, user, lockId);
		}
	}
	
	public void releaseLock(Integer recordId) {
		if ( lockingEnabled ) {
			lockManager.releaseLock(recordId);
		}
	}


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

    public CodeListManager getCodeListManager() {
        return codeListManager;
    }

    public void setCodeListManager(CodeListManager codeListManager) {
        this.codeListManager = codeListManager;
    }

    public SurveyManager getSurveyManager() {
        return surveyManager;
    }

    public void setSurveyManager(SurveyManager surveyManager) {
        this.surveyManager = surveyManager;
    }

}
