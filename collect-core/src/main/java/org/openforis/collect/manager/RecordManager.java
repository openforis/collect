/**
 * 
 */
package org.openforis.collect.manager;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
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
import org.openforis.collect.persistence.RecordDao.RecordStoreQuery;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordNotOwnedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.persistence.RecordValidationInProgressException;
import org.openforis.collect.utils.Consumer;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
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
@Transactional(readOnly=true, propagation=SUPPORTS)
public class RecordManager {

//	private final Log log = LogFactory.getLog(RecordManager.class);
	
	private static final int DEFAULT_LOCK_TIMEOUT_MILLIS = 300000;
	
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	
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
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void saveAndRun(CollectRecord record, Runnable callback) {
		save(record);
		callback.run();
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void save(CollectRecord record) {
		try {
			save(record, null);
		} catch (RecordPersistenceException e) {
			//it should never happen when record locking is not being used
			throw new RuntimeException(e);
		}
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void save(CollectRecord record, String sessionId) throws RecordPersistenceException {
		User lockingUser = record.getModifiedBy();
		save(record, lockingUser, sessionId);
	}

	@Transactional(readOnly=false, propagation=REQUIRED)
	public void save(CollectRecord record, User lockingUser, String sessionId) throws RecordPersistenceException {
		record.updateSummaryFields();

		if (record.getId() != null) {
			checkAllKeysSpecified(record);
		}
		
		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
			id = record.getId();
			//TODO fix: concurrency problem may occur..
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
	
	public int nextId() {
		return recordDao.nextId();
	}
	
	public void restartIdSequence(Number value) {
		recordDao.restartIdSequence(value);
	}

	public RecordStoreQuery createInsertQuery(CollectRecord record) {
		return recordDao.createInsertQuery(record);
	}
	
	public RecordStoreQuery createUpdateQuery(CollectRecord record, Step step) {
		return recordDao.createUpdateQuery(record, step);	
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void execute(List<RecordStoreQuery> queries) {
		recordDao.execute(queries);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void executeRecordOperations(List<RecordOperations> operationsForRecords) {
		executeRecordOperations(operationsForRecords, null);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void executeRecordOperations(List<RecordOperations> operationsForRecords, Consumer<RecordStepOperation> consumer) {
		int nextId = nextId();
		List<RecordStoreQuery> queries = new ArrayList<RecordStoreQuery>();
		for (RecordOperations recordOperations : operationsForRecords) {
			List<RecordStepOperation> operations = recordOperations.getOperations();
			for (RecordStepOperation operation : operations) {
				CollectRecord record = operation.getRecord();
				record.setStep(operation.getStep());
				if (operation.isInsert()) {
					recordOperations.initializeRecordId(nextId ++);
					queries.add(createInsertQuery(record));
				} else {
					queries.add(createUpdateQuery(record, operation.getStep()));
				}
				if (consumer != null) {
					consumer.consume(operation);
				}
			}
		}
		execute(queries);
		restartIdSequence(nextId);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void deleteByIds(Set<Integer> ids) throws RecordPersistenceException {
		getRecordDao().deleteByIds(ids);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void delete(int recordId) throws RecordPersistenceException {
		if ( isLockingEnabled() && lockManager.isLocked(recordId) ) {
			RecordLock lock = lockManager.getLock(recordId);
			User lockUser = lock.getUser();
			throw new RecordLockedException(lockUser.getUsername());
		} else {
			recordDao.delete(recordId);
		}
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void deleteBySurvey(int surveyId) {
		recordDao.deleteBySurvey(surveyId);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
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
	public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step, String sessionId, boolean forceUnlock) throws RecordPersistenceException {
		return checkout(survey, user, recordId, Step.valueOf(step), sessionId, forceUnlock);
	}
	
	public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, String sessionId, boolean forceUnlock) throws RecordPersistenceException {
		return checkout(survey, user, recordId, determineLastStep(survey, recordId), sessionId, forceUnlock);
	}
	
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
			throw new RecordNotOwnedException(record.getOwner().getUsername());
		}
		if(isLockingEnabled()) {
			//refresh lock because record loading can be time consuming
			lockManager.lock(recordId, user, sessionId);
		}
		return record;
	}

	private void checkSurveyRecordValidationNotInProgress(CollectSurvey survey) throws RecordValidationInProgressException {
		if ( survey.isPublished() && ! survey.isTemporary() && surveyManager.isRecordValidationInProgress(survey.getId()) ) {
			throw new RecordValidationInProgressException();
		}
	}

	@Deprecated
	public CollectRecord load(CollectSurvey survey, int recordId, int step) {
		Step stepEnum = Step.valueOf(step);
		return load(survey, recordId, stepEnum);
	}
	
	public CollectRecord load(CollectSurvey survey, int recordId) {
		Step lastStep = determineLastStep(survey, recordId);
		return load(survey, recordId, lastStep);
	}

	public CollectRecord load(CollectSurvey survey, int recordId, Step step) {
		return load(survey, recordId, step, true);
	}
	
	public CollectRecord load(CollectSurvey survey, int recordId, Step step, boolean validate) {
		if (survey == null) {
			int surveyId = recordDao.loadSurveyId(recordId);
			survey = surveyManager.getOrLoadSurveyById(surveyId);
		}
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber(), validate);
		loadDetachedObjects(record);
		recordConverter.convertToLatestVersion(record);
		RecordUpdater recordUpdater = new RecordUpdater();
		recordUpdater.setValidateAfterUpdate(validate);
		recordUpdater.initializeRecord(record);
		return record;
	}

	private void loadDetachedObjects(List<CollectRecord> summaries) {
		for (CollectRecord summary : summaries) {
			loadDetachedObjects(summary);
		}
	}
	
	private void loadDetachedObjects(CollectRecord record) {
		record.setCreatedBy(loadUser(record.getCreatedBy()));
		record.setModifiedBy(loadUser(record.getModifiedBy()));
		record.setOwner(loadUser(record.getOwner()));
	}
	
	public byte[] loadBinaryData(CollectSurvey survey, int recordId, Step step) {
		return recordDao.loadBinaryData(survey, recordId, step.getStepNumber());
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity) {
		List<CollectRecord> summaries = loadSummaries(survey, rootEntity, (String[]) null);
		return summaries;
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step) {
		RecordFilter filter = new RecordFilter(survey, rootEntity);
		filter.setStep(step);
		return loadSummaries(filter);
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return loadSummaries(survey, rootEntity, true, keys);
	}
	
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, boolean caseSensitiveKeys, String... keys) {
		RecordFilter filter = new RecordFilter(survey, rootEntity);
		filter.setCaseSensitiveKeyValues(caseSensitiveKeys);
		filter.setKeyValues(keys);
		return loadSummaries(filter);
	}
	
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, List<RecordSummarySortField> sortFields, String... keyValues) {
		RecordFilter filter = new RecordFilter(survey, rootEntity);
		filter.setOffset(offset);
		filter.setMaxNumberOfRecords(maxNumberOfRecords);
		filter.setKeyValues(keyValues);
		return loadSummaries(filter, sortFields);
	}
	
	public List<CollectRecord> loadSummaries(RecordFilter filter) {
		return loadSummaries(filter, null);
	}
	
	public List<CollectRecord> loadSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		List<CollectRecord> summaries = recordDao.loadSummaries(filter, sortFields);
		loadDetachedObjects(summaries);
		return summaries;
	}

	public CollectRecordSummary loadUniqueRecordSummaryByKeys(CollectSurvey survey, int rootEntityId, List<String> keyValues) {
		return loadUniqueRecordSummaryByKeys(survey, survey.getSchema().getRootEntityDefinition(rootEntityId).getName(), keyValues);
	}
	
	public CollectRecordSummary loadUniqueRecordSummaryByKeys(CollectSurvey survey, String rootEntityName, List<String> keyValues) {
		return loadUniqueRecordSummaryByKeys(survey, rootEntityName, keyValues.toArray(new String[keyValues.size()]));
	}
	
	public CollectRecordSummary loadUniqueRecordSummaryByKeys(CollectSurvey survey, String rootEntityName, String... keyValues) {
		List<CollectRecord> oldRecords = loadSummaries(survey, rootEntityName, keyValues);
		if (oldRecords == null || oldRecords.isEmpty()) {
			return null;
		} else if (oldRecords.size() == 1) {
			return CollectRecordSummary.fromRecord(oldRecords.get(0));
		} else {
			throw new IllegalStateException(String.format(
					"Multiple records found in survey %s with key(s): %s",
					survey.getName(), keyValues));
		}
	}
	
	@Transactional(readOnly=true)
	public void visitSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields, Visitor<CollectRecord> visitor) {
		recordDao.visitSummaries(filter, sortFields, visitor);
	}
	
	/**
	 * Returns only the records modified after the specified date.
	 */
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Date modifiedSince) {
		RecordFilter filter = new RecordFilter(survey, rootEntity);
		filter.setModifiedSince(modifiedSince);
		return loadSummaries(filter);
	}
	
	public int loadSurveyId(int recordId) {
		return recordDao.loadSurveyId(recordId);
	}

	public int countRecords(CollectSurvey survey) {
		return recordDao.countRecords(survey);
	}
	
	public int countRecords(CollectSurvey survey, int rootEntityDefinitionId) {
		return recordDao.countRecords(survey, rootEntityDefinitionId);
	}
	
	public int countRecords(CollectSurvey survey, int rootEntityDefinitionId, int dataStepNumber) {
		return recordDao.countRecords(survey, rootEntityDefinitionId, dataStepNumber);
	}

	public int countRecords(CollectSurvey survey, String rootEntityDefinition, Step step) {
		EntityDefinition rootDef = survey.getSchema().getRootEntityDefinition(rootEntityDefinition);
		return countRecords(survey, rootDef.getId(), step.getStepNumber());
	}

	public int countRecords(RecordFilter filter) {
		return recordDao.countRecords(filter);
	}
	
	public int countRecords(CollectSurvey survey, String rootEntity, String... keyValues) {
		RecordFilter filter = new RecordFilter(survey, rootEntity);
		filter.setKeyValues(keyValues);
		return countRecords(filter);
	}

	/**
	 * Returns false if another record with the same root entity key values exists.
	 */
	public boolean isUnique(CollectRecord record) {
		record.updateSummaryFields();
		
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		
		RecordFilter filter = new RecordFilter(survey, record.getRootEntityDefinitionId());
		filter.setKeyValues(record.getRootEntityKeyValues());
		filter.setIncludeNullConditionsForKeyValues(true);
		
		List<CollectRecord> summaries = recordDao.loadSummaries(filter);
		for (CollectRecord collectRecord : summaries) {
			if ( ! collectRecord.getId().equals(record.getId()) ) {
				return false;
			}
		}
		return true;
	}
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName) {
		return create(survey, rootEntityName, user, modelVersionName, (String) null);
	}
	
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName, String sessionId) {
		return create(survey, rootEntityDefinition.getName(), user, modelVersionName, sessionId);
	}
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName, String sessionId) {
		return create(survey, rootEntityName, user, modelVersionName, sessionId, Step.ENTRY);
	}
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName, String sessionId, Step step) {
		CollectRecord record = instantiateRecord(survey, rootEntityName, user,
				modelVersionName, step);
		initializeRecord(record);
		return record;
	}

	public NodeChangeSet initializeRecord(CollectRecord record) {
		return updater.initializeNewRecord(record);
	}

	public CollectRecord instantiateRecord(CollectSurvey survey,
			String rootEntityName, User user, String modelVersionName, Step step) {
		CollectRecord record = survey.createRecord(modelVersionName);
		record.createRootEntity(rootEntityName);
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		record.setStep(step);
		return record;
	}

	@Transactional(readOnly=false, propagation=REQUIRED)
	public CollectRecord promote(CollectSurvey survey, int recordId, Step currentStep, User user) throws RecordPromoteException, MissingRecordKeyException {
		CollectRecord record = load(survey, recordId, currentStep);
		performPromote(record, user);
		return record;
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void promote(CollectSurvey survey, int recordId, Step currentStep, User user, RecordCallback callback) throws RecordPromoteException, MissingRecordKeyException {
		CollectRecord record = promote(survey, recordId, currentStep, user);
		callback.run(record);
	}
	
	/**
	 * Saves a record and promotes it to the next phase
	 */
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void promote(CollectRecord record, User user) throws RecordPromoteException, MissingRecordKeyException {
		Integer errors = record.getErrors();
		Integer skipped = record.getSkipped();
		Integer missing = record.getMissingErrors();
		int totalErrors = errors + skipped + missing;
		if( totalErrors > 0 ){
			throw new RecordPromoteException("Record cannot be promoted becuase it contains errors.");
		}
		record.updateSummaryFields();
		checkAllKeysSpecified(record);

		Integer id = record.getId();
		// before promoting record, save it in current step
		if( id == null ) {
			recordDao.insert( record );
		} else {
			recordDao.update( record );
		}
		
		performPromote(record, user);
	}

	private void performPromote(CollectRecord record, User user) {
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

		validate(record);
		
		recordDao.update( record );
	}

	@Transactional(readOnly=false, propagation=REQUIRED)
	public CollectRecord demote(CollectSurvey survey, int recordId, Step currentStep, User user) throws RecordPersistenceException {
		Step prevStep = currentStep.getPrevious();
		CollectRecord record = recordDao.load( survey, recordId, prevStep.getStepNumber() );
		loadDetachedObjects(record);
		record.setModifiedBy( user );
		record.setModifiedDate( new Date() );
		record.setStep( prevStep );
		record.setOwner(null);
		record.setState( State.REJECTED );
		validate(record);
		recordDao.update( record );
		return record;
	}

	@Transactional(readOnly=false, propagation=REQUIRED)
	public void demote(CollectSurvey survey, int recordId, Step currentStep, User user, RecordCallback callback) throws RecordPersistenceException {
		CollectRecord record = demote(survey, recordId, currentStep, user);
		callback.run(record);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void validateAndSave(CollectSurvey survey, User user, String sessionId, int recordId, Step step) throws RecordLockedException, MultipleEditException {
		if ( isLockingEnabled() ) {
			lockManager.isLockAllowed(user, recordId, sessionId, true);
			lockManager.lock(recordId, user, sessionId, true);
		}
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber());

		validate(record);
		
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
			Attribute<?, V> attribute, V value) {
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
	public void validate(CollectRecord record) {
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
			EntityDefinition keyParentDefn = keyDefn.getParentEntityDefinition();
			Entity keyParent = (Entity) record.findNodeByPath(keyParentDefn.getPath());
			if (keyParent == null) {
				throw new MissingRecordKeyException();
			}
			boolean required = keyParent.isRequired(keyDefn);
			if ( required ) {
				Node<?> keyNode = keyParent.getChild(keyDefn);
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
	
	private User loadUser(User user) {
		if (user == null) {
			return null;
		}
		return userManager.loadById(user.getId());
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

    public interface RecordCallback {
    	
    	void run(CollectRecord record);
    	
    }

	public static class RecordStepOperation {
		
		private CollectRecord record;
		private Step step;
		private boolean insert;
		
		public RecordStepOperation(CollectRecord record, Step step, boolean insert) {
			super();
			this.record = record;
			this.step = step;
			this.insert = insert;
		}
		
		public CollectRecord getRecord() {
			return record;
		}
		
		private Step getStep() {
			return step;
		}
		
		public boolean isInsert() {
			return insert;
		}
		
		/**
		 * Returns the size as number of nodes in the record.
		 */
		public int getSize() {
			return record.countNodes();
		}
	}
	
	public static class RecordOperations {
		
		private Integer recordId;
		private Step originalStep;
		private Step lastUpdatedStep;
		private List<RecordStepOperation> operations = new ArrayList<RecordStepOperation>();
		private int operationsSize = 0;
		
		public void initializeRecordId(Integer recordId) {
			this.recordId = recordId;
			for (RecordStepOperation operation : operations) {
				operation.getRecord().setId(recordId);
			}
		}
		
		public boolean isEmpty() {
			return operations.isEmpty();
		}
		
		public boolean hasMissingSteps() {
			return originalStep != null && originalStep.after(lastUpdatedStep);
		}

		public void addUpdate(CollectRecord record, Step step) {
			add(new RecordStepOperation(record, step, false));
		}

		public void addInsert(CollectRecord record, Step step) {
			add(new RecordStepOperation(record, step, true));
		}
		
		private void add(RecordStepOperation operation) {
			operations.add(operation);
			lastUpdatedStep = operation.getStep();
			operationsSize += operation.getSize();
		}
		
		public List<RecordStepOperation> getOperations() {
			return operations;
		}
		
		public Integer getRecordId() {
			return recordId;
		}
		
		public Step getOriginalStep() {
			return originalStep;
		}
		
		public void setOriginalStep(Step originalStep) {
			this.originalStep = originalStep;
		}
		
		public Step getLastUpdatedStep() {
			return lastUpdatedStep;
		}

		public int getOperationsSize() {
			return operationsSize;
		}
		
	}

}
