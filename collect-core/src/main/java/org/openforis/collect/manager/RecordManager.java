/**
 * 
 */
package org.openforis.collect.manager;

import static org.openforis.collect.model.CollectRecord.APPROVED_MISSING_POSITION;
import static org.openforis.collect.model.CollectRecord.CONFIRMED_ERROR_POSITION;
import static org.openforis.collect.model.CollectRecord.DEFAULT_APPLIED_POSITION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.NodeChangeMap;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordLock;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordNotOwnedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.persistence.RecordValidationInProgressException;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager {
//	private final Log log = LogFactory.getLog(RecordManager.class);
	private static final int DEFAULT_LOCK_TIMEOUT_MILLIS = 60000;
	
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SurveyManager surveyManager;
	
	private RecordConverter recordConverter;
	private long lockTimeoutMillis;
	private boolean lockingEnabled;
	private RecordLockManager lockManager;
	
	public RecordManager() {
		this(true);
	}
	
	public RecordManager(boolean lockingEnabled) {
		super();
		this.lockingEnabled = lockingEnabled;
		lockTimeoutMillis = DEFAULT_LOCK_TIMEOUT_MILLIS;
		recordConverter = new RecordConverter();
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
		if ( isLockingEnabled() ) {
			checkSurveyRecordValidationNotInProgress(survey);
			lockManager.isLockAllowed(user, recordId, sessionId, forceUnlock);
			lockManager.lock(recordId, user, sessionId, forceUnlock);
		}
		CollectRecord record = load(survey, recordId, step);
		if ( step == Step.ENTRY && record.getOwner() != null && ! record.getOwner().getId().equals(user.getId())
				&& ! ( user.hasRole("ROLE_ADMIN") || user.hasRole("ROLE_ANALYSIS") || user.hasRole("ROLE_CLEANSING")) ) {
			if ( isLockingEnabled() ) {
				releaseLock(recordId);
			}
			throw new RecordNotOwnedException(record.getOwner().getName());
		}
		addEmptyNodes(record);
		validate(record);
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
		return load(survey, recordId, (Step) null);
	}
	
	public CollectRecord load(CollectSurvey survey, int recordId, Step step) {
		if ( step == null ) {
			//fetch last record step
			RecordFilter filter = new RecordFilter(survey);
			filter.setRecordId(recordId);
			List<CollectRecord> summaries = recordDao.loadSummaries(filter, null);
			if ( ! summaries.isEmpty() ) {
				CollectRecord summary = summaries.get(0);
				step = summary.getStep();
			}
		}
		CollectRecord record = recordDao.load(survey, recordId, step.getStepNumber());
		recordConverter.convertToLatestVersion(record);
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
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName) throws RecordPersistenceException {
		return create(survey, rootEntityName, user, modelVersionName, (String) null);
	}
	
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName, String sessionId) throws RecordPersistenceException {
		return create(survey, rootEntityDefinition.getName(), user, modelVersionName, sessionId);
	}
	
	public CollectRecord create(CollectSurvey survey, String rootEntityName, User user, String modelVersionName, String sessionId) throws RecordPersistenceException {
		if ( lockingEnabled && sessionId == null ) {
			throw new IllegalArgumentException("Lock session id not specified");
		}
		CollectRecord record = new CollectRecord(survey, modelVersionName);
		record.createRootEntity(rootEntityName);
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		addEmptyNodes(record);
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

		applyDefaultValues(record);
		
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
		clearNodeStates(record);
		record.setStep( nextStep );
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
		validate(record);
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
		beforeAttributeUpdate(attribute);
		attribute.setValue(value);
		return afterAttributeUpdate(attribute);
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
		beforeAttributeUpdate(attribute);
		attribute.setValue(null);
		setSymbolOnFields(attribute, symbol);
		return afterAttributeUpdate(attribute);
	}
	
	protected <V extends Value> void beforeAttributeUpdate(
			Attribute<? extends NodeDefinition, V> attribute) {
		Entity parentEntity = attribute.getParent();
		setErrorConfirmed(attribute, false);
		setMissingValueApproved(parentEntity, attribute.getName(), false);
		setDefaultValueApplied(attribute, false);
	}

	protected <V extends Value> NodeChangeSet afterAttributeUpdate(
			Attribute<?, V> attribute) {
		NodeChangeMap changeMap = new NodeChangeMap();
		AttributeChange change = changeMap.prepareAttributeChange(attribute);
		Map<Integer, Object> updatedFieldValues = createFieldValuesMap(attribute);
		change.setUpdatedFieldValues(updatedFieldValues);
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}

	protected <V extends Value> NodeChangeSet afterAttributeInsertOrUpdate(
			NodeChangeMap changeMap,
			Attribute<? extends NodeDefinition, V> attribute) {
		Set<NodePointer> relevanceRequiredDependencies = clearRelevanceRequiredDependencies(attribute);
		relevanceRequiredDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
		Set<Attribute<?, ?>> checkDependencies = clearValidationResults(attribute);
		checkDependencies.add(attribute);
		List<NodePointer> cardinalityDependencies = createCardinalityNodePointers(attribute);
		prepareChange(changeMap, relevanceRequiredDependencies, checkDependencies, cardinalityDependencies);
		return new NodeChangeSet(changeMap.getChanges());
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
		Attribute<?, ?> attribute = field.getAttribute();

		beforeAttributeUpdate(attribute);

		field.setValue(value);
		
		return afterAttributeUpdate(attribute);
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
		Attribute<?, ?> attribute = field.getAttribute();

		beforeAttributeUpdate(attribute);

		field.setValue(null);
		setFieldSymbol(field, symbol);
		
		return afterAttributeUpdate(attribute);
	}
	
	/**
	 * Adds a new entity to a the record.
	 * 
	 * @param parentEntity
	 * @param nodeName
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addEntity(
			Entity parentEntity, String nodeName) {
		Entity createdNode = performEntityAdd(parentEntity, nodeName);
		
		setMissingValueApproved(parentEntity, nodeName, false);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAddEntityChange(createdNode);
		
		Set<NodePointer> relevanceRequiredDependencies = clearRelevanceRequiredDependencies(createdNode);
		Set<Attribute<?, ?>> checkDependencies = null;
		relevanceRequiredDependencies.add(new NodePointer(createdNode.getParent(), nodeName));
		List<NodePointer> cardinalityDependencies = createCardinalityNodePointers(createdNode);
		prepareChange(changeMap, relevanceRequiredDependencies, checkDependencies, cardinalityDependencies);
		return new NodeChangeSet(changeMap.getChanges());
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
	public NodeChangeSet addAttribute(
			Entity parentEntity, String attributeName, Value value, FieldSymbol symbol, 
			String remarks) {
		Attribute<?, ?> attribute = performAttributeAdd(parentEntity, attributeName, value, symbol, remarks);
		
		setMissingValueApproved(parentEntity, attributeName, false);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAddAttributeChange(attribute);
		
		return afterAttributeInsertOrUpdate(changeMap, attribute);
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
		field.setRemarks(remarks);
		NodeChangeMap changeMap = new NodeChangeMap();
		Attribute<?, ?> attribute = field.getAttribute();
		changeMap.prepareAttributeChange(attribute);
		return new NodeChangeSet(changeMap.getChanges());
	}

	public NodeChangeSet approveMissingValue(
			Entity parentEntity, String nodeName) {
		setMissingValueApproved(parentEntity, nodeName, true);
		List<NodePointer> cardinalityNodePointers = createCardinalityNodePointers(parentEntity);
		cardinalityNodePointers.add(new NodePointer(parentEntity, nodeName));
		NodeChangeMap changeMap = new NodeChangeMap();
		validateAll(changeMap, cardinalityNodePointers, false);
		return new NodeChangeSet(changeMap.getChanges());
	}

	public NodeChangeSet confirmError(Attribute<?, ?> attribute) {
		Set<Attribute<?,?>> checkDependencies = new HashSet<Attribute<?,?>>();
		setErrorConfirmed(attribute, true);
		attribute.clearValidationResults();
		checkDependencies.add(attribute);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAttributeChange(attribute);
		validateChecks(changeMap, checkDependencies);
		return new NodeChangeSet(changeMap.getChanges());
	}
	
	/**
	 * Clear all node states
	 * @param record 
	 */
	protected void clearNodeStates(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse( new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
//					if ( step == Step.ENTRY ) {
//						attribute.clearFieldSymbols();
//					}
					attribute.clearFieldStates();
					attribute.clearValidationResults();
				} else if( node instanceof Entity ) {
					Entity entity = (Entity) node;
					entity.clearChildStates();
					
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						String childName = childDefinition.getName();
						entity.clearRelevanceState(childName);
						entity.clearRequiredState(childName);
					}
				}
			} 
		} );
	}
	
	/**
	 * Validate the entire record validating the value of each attribute and 
	 * the min/max count of each child node of each entity
	 * 
	 * @return 
	 */
	public void validate(final CollectRecord record) {
		record.resetValidationInfo();
		Entity rootEntity = record.getRootEntity();
		addEmptyNodes(rootEntity);
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				if ( node instanceof Attribute ) {
					Attribute<?,?> attribute = (Attribute<?, ?>) node;
					attribute.validateValue();
				} else if ( node instanceof Entity ) {
					Entity entity = (Entity) node;
					ModelVersion version = record.getVersion();
					EntityDefinition definition = entity.getDefinition();
					List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
					for (NodeDefinition childDefinition : childDefinitions) {
						if ( version == null || version.isApplicable(childDefinition) ) {
							String childName = childDefinition.getName();
							entity.validateMaxCount(childName);
							entity.validateMinCount(childName);
						}
					}
				}
			}
		});
	}

	/**
	 * Deletes a node from the record.
	 * 
	 * @param node
	 * @return
	 */
	public NodeChangeSet deleteNode(
			Node<?> node) {
		Set<NodePointer> relevantDependencies = new HashSet<NodePointer>();
		Set<NodePointer> requiredDependencies = new HashSet<NodePointer>();
		HashSet<Attribute<?, ?>> checkDependencies = new HashSet<Attribute<?,?>>();
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareDeleteNodeChange(node);
		List<NodePointer> cardinalityNodePointers = createCardinalityNodePointers(node);
		Stack<Node<?>> depthFirstDescendants = getDepthFirstDescendants(node);
		while ( !depthFirstDescendants.isEmpty() ) {
			Node<?> n = depthFirstDescendants.pop();
			relevantDependencies.addAll(n.getRelevantDependencies());
			requiredDependencies.addAll(n.getRequiredDependencies());
			if ( n instanceof Attribute ) {
				checkDependencies.addAll(((Attribute<?, ?>) n).getCheckDependencies());
			}
			performNodeDeletion(n);
		}
		//clear dependencies
		clearRelevantDependencies(relevantDependencies);
		HashSet<NodePointer> relevanceRequiredDependencies = new HashSet<NodePointer>();
		relevanceRequiredDependencies.addAll(relevantDependencies);
		relevanceRequiredDependencies.addAll(requiredDependencies);
		clearRequiredDependencies(relevanceRequiredDependencies);
		clearValidationResults(checkDependencies);
		
		prepareChange(changeMap, relevanceRequiredDependencies, checkDependencies, cardinalityNodePointers);
		return new NodeChangeSet(changeMap.getChanges());
	}
	
	protected Node<?> performNodeDeletion(Node<?> node) {
		if(node.isDetached()) {
			throw new IllegalArgumentException("Unable to delete a node already detached");
		}
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getName(), index);
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		record.removeValidationCache(deletedNode.getInternalId());
		return deletedNode;
	}
	
	protected Stack<Node<?>> getDepthFirstDescendants(Node<?> node) {
		Stack<Node<?>> result = new Stack<Node<?>>();
		Stack<Node<?>> stack = new Stack<Node<?>>();
		stack.push(node);
		while(!stack.isEmpty()){
			Node<?> n = stack.pop();
			result.push(n);
			if(n instanceof Entity){
				Entity entity = (Entity) n;
				List<Node<? extends NodeDefinition>> children = entity.getChildren();
				for (Node<? extends NodeDefinition> child : children) {
					stack.push(child);
				}
			}
		}
		return result;
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
		performDefaultValueApply(attribute);
		return afterAttributeUpdate(attribute);
	}

	/**
	 * Applies the first default value (if any) that is applicable to the attribute.
	 * The condition of the corresponding DefaultValue will be verified.
	 *  
	 * @param attribute
	 */
	protected <V extends Value> void performDefaultValueApply(Attribute<?, V> attribute) {
		AttributeDefinition attributeDefn = (AttributeDefinition) attribute.getDefinition();
		List<AttributeDefault> defaults = attributeDefn.getAttributeDefaults();
		if ( defaults != null && defaults.size() > 0 ) {
			for (AttributeDefault attributeDefault : defaults) {
				try {
					V value = attributeDefault.evaluate(attribute);
					if ( value != null ) {
						attribute.setValue(value);
						setDefaultValueApplied(attribute, true);
						clearRelevanceRequiredDependencies(attribute);
						clearValidationResults(attribute);
						break;
					}
				} catch (InvalidExpressionException e) {
					throw new RuntimeException("Error applying default value for attribute " + attributeDefn.getPath());
				}
			}
		}
	}
	
	protected List<NodePointer> createCardinalityNodePointers(Node<?> node){
		List<NodePointer> nodePointers = new ArrayList<NodePointer>();
		
		Entity parent = node.getParent();
		String childName = node.getName();
		while(parent != null){
			NodePointer nodePointer = new NodePointer(parent, childName );
			nodePointers.add(nodePointer);
			
			childName = parent.getName();
			parent = parent.getParent();
		}
		return nodePointers;
	}

	protected void prepareChange(NodeChangeMap changeMap, Set<NodePointer> relevanceRequiredDependencies, 
			Collection<Attribute<?, ?>> checkDependencies, Collection<NodePointer> cardinalityDependencies) {
		validateAll(changeMap, cardinalityDependencies, false);
		validateAll(changeMap, relevanceRequiredDependencies, true);
		validateChecks(changeMap, checkDependencies);
	}

	protected void validateChecks(NodeChangeMap changeMap,
			Collection<Attribute<?, ?>> attributes) {
		if (attributes != null) {
			for (Attribute<?, ?> attr : attributes) {
				validateAttribute(changeMap, attr);
			}
		}
	}

	protected void validateAttribute(NodeChangeMap changeMap,
			Attribute<?, ?> attr) {
		if ( !attr.isDetached() ) {
			attr.clearValidationResults();
			ValidationResults results = attr.validateValue();
			AttributeChange change = changeMap.prepareAttributeChange(attr);
			change.setValidationResults(results);
		}
	}

	protected void validateChecks(NodeChangeMap changeMap,
			Entity entity, String childName) {
		List<Node<?>> children = entity.getAll(childName);
		for ( Node<?> node : children ) {
			if ( node instanceof Attribute ){
				validateAttribute(changeMap, (Attribute<?, ?>) node);
			}
		}
	}
	
	protected void validateAll(NodeChangeMap changeMap,
			Collection<NodePointer> nodePointers, boolean validateChecks) {
		if (nodePointers != null) {
			for (NodePointer nodePointer : nodePointers) {
				Entity parent = nodePointer.getEntity();
				if ( parent != null && ! parent.isDetached()) {
					validateCardinality(changeMap, nodePointer);
					validateRelevanceState(changeMap, nodePointer);
					validateRequirenessState(changeMap, nodePointer);
					if ( validateChecks ) {
						validateChecks(changeMap, parent, nodePointer.getChildName());
					}
				}
			}
		}
	}

	protected void validateCardinality(NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenMinCountValidation(childName, entity.validateMinCount(childName));
		change.setChildrenMaxCountValidation(childName, entity.validateMaxCount(childName));
	}

	protected void validateRelevanceState(
			NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenRelevance(childName, entity.isRelevant(childName));
	}

	protected void validateRequirenessState(
			NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenRequireness(childName, entity.isRequired(childName));
	}

	protected Attribute<?, ?> performAttributeAdd(Entity parentEntity, String nodeName, Value value, 
			FieldSymbol symbol, String remarks) {
		if ( value != null && symbol != null ) {
			throw new IllegalArgumentException("Cannot specify both value and symbol");
		}
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		AttributeDefinition def = (AttributeDefinition) parentEntityDefn.getChildDefinition(nodeName);
		@SuppressWarnings("unchecked")
		Attribute<?, Value> attribute = (Attribute<?, Value>) def.createNode();
		parentEntity.add(attribute);
		if ( value != null ) {
			attribute.setValue(value);
		} else if ( symbol != null ) {
			setSymbolOnFields(attribute, symbol);
		}
		if ( remarks != null ) {
			setRemarksOnFirstField(attribute, remarks);
		}
		return attribute;
	}
	
	protected Entity performEntityAdd(Entity parentEntity, String nodeName) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName);
		addEmptyNodes(entity);
		return entity;
	}

	protected Entity performEntityAdd(Entity parentEntity, String nodeName, int idx) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName, idx);
		addEmptyNodes(entity);
		return entity;
	}
	
	protected void addEmptyNodes(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		addEmptyNodes(rootEntity);
	}
	
	protected void addEmptyNodes(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		addEmptyEnumeratedEntities(entity);
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if(version == null || version.isApplicable(childDefn)) {
				String childName = childDefn.getName();
				if(entity.getCount(childName) == 0) {
					int toBeInserted = entity.getEffectiveMinCount(childName);
					if ( toBeInserted <= 0 && childDefn instanceof AttributeDefinition || ! childDefn.isMultiple() ) {
						//insert at least one node
						toBeInserted = 1;
					}
					addEmptyChildren(entity, childDefn, toBeInserted);
				} else {
					List<Node<?>> children = entity.getAll(childName);
					for (Node<?> child : children) {
						if(child instanceof Entity) {
							addEmptyNodes((Entity) child);
						}
					}
				}
			}
		}
	}

	protected void addEmptyEnumeratedEntities(Entity parentEntity) {
		Record record = parentEntity.getRecord();
		CollectSurvey survey = (CollectSurvey) parentEntity.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		ModelVersion version = record.getVersion();
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		List<NodeDefinition> childDefinitions = parentEntityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if ( childDefn instanceof EntityDefinition && (version == null || version.isApplicable(childDefn)) ) {
				EntityDefinition childEntityDefn = (EntityDefinition) childDefn;
				boolean tableLayout = uiOptions == null || uiOptions.getLayout(childEntityDefn) == Layout.TABLE;
				if(childEntityDefn.isMultiple() && childEntityDefn.isEnumerable() && tableLayout) {
					addEmptyEnumeratedEntities(parentEntity, childEntityDefn);
				}
			}
		}
	}

	protected void addEmptyEnumeratedEntities(Entity parentEntity, EntityDefinition enumerableEntityDefn) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = enumerableEntityDefn.getEnumeratingKeyCodeAttribute(version);
		if(enumeratingCodeDefn != null) {
			String enumeratedEntityName = enumerableEntityDefn.getName();
			CodeList list = enumeratingCodeDefn.getList();
			List<CodeListItem> items = codeListManager.loadRootItems(list);
			for (int i = 0; i < items.size(); i++) {
				CodeListItem item = items.get(i);
				if(version == null || version.isApplicable(item)) {
					String code = item.getCode();
					Entity enumeratedEntity = getEnumeratedEntity(parentEntity, enumerableEntityDefn, enumeratingCodeDefn, code);
					if( enumeratedEntity == null ) {
						Entity addedEntity = performEntityAdd(parentEntity, enumeratedEntityName, i);
						//set the value of the key CodeAttribute
						CodeAttribute addedCode = (CodeAttribute) addedEntity.get(enumeratingCodeDefn.getName(), 0);
						addedCode.setValue(new Code(code));
					} else {
						parentEntity.move(enumeratedEntityName, enumeratedEntity.getIndex(), i);
					}
				}
			}
		}
	}

	protected Entity getEnumeratedEntity(Entity parentEntity, EntityDefinition childEntityDefn, 
			CodeAttributeDefinition enumeratingCodeAttributeDef, String value) {
		List<Node<?>> children = parentEntity.getAll(childEntityDefn.getName());
		for (Node<?> child : children) {
			Entity entity = (Entity) child;
			Code code = getCodeAttributeValue(entity, enumeratingCodeAttributeDef);
			if(code != null && value.equals(code.getCode())) {
				return entity;
			}
		}
		return null;
	}
	
	private Code getCodeAttributeValue(Entity entity, CodeAttributeDefinition def) {
		Node<?> node = entity.get(def.getName(), 0);
		if(node != null) {
			return ((CodeAttribute)node).getValue();
		} else {
			return null;
		}
	}
	
	protected int addEmptyChildren(Entity entity, NodeDefinition childDefn, int toBeInserted) {
		String childName = childDefn.getName();
		CollectSurvey survey = (CollectSurvey) entity.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		int count = 0;
		boolean multipleEntityFormLayout = childDefn instanceof EntityDefinition && childDefn.isMultiple() && 
				uiOptions != null && uiOptions.getLayout((EntityDefinition) childDefn) == Layout.FORM;
		if ( ! multipleEntityFormLayout ) {
			while(count < toBeInserted) {
				if(childDefn instanceof AttributeDefinition) {
					Node<?> createNode = childDefn.createNode();
					entity.add(createNode);
				} else if(childDefn instanceof EntityDefinition ) {
					performEntityAdd(entity, childName);
				}
				count ++;
			}
		}
		return count;
	}
	
	/**
	 * Applies default values on each descendant attribute of a record in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * @param record 
	 * 
	 * @throws InvalidExpressionException 
	 */
	protected void applyDefaultValues(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		applyDefaultValues(rootEntity);
	}

	/**
	 * Applies default values on each descendant attribute of an Entity in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * 
	 * @param entity
	 * @throws InvalidExpressionException 
	 */
	protected void applyDefaultValues(Entity entity) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child: children) {
			if ( child instanceof Attribute ) {
				Attribute<?, ?> attribute = (Attribute<?, ?>) child;
				if ( attribute.isEmpty() ) {
					performDefaultValueApply(attribute);
				}
			} else if ( child instanceof Entity ) {
				applyDefaultValues((Entity) child);
			}
		}
	}
	
	protected <V> void setFieldSymbol(Field<V> field, FieldSymbol symbol){
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	protected Set<NodePointer> clearRelevanceRequiredDependencies(Node<?> node){
		Set<NodePointer> relevantDependencies = node.getRelevantDependencies();
		clearRelevantDependencies(relevantDependencies);
		Set<NodePointer> requiredDependencies = node.getRequiredDependencies();
		requiredDependencies.addAll(relevantDependencies);
		clearRequiredDependencies(requiredDependencies);
		return requiredDependencies;
	}
	
	protected void clearRelevantDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRelevanceState(nodePointer.getChildName());
		}
	}
	
	protected void clearRequiredDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRequiredState(nodePointer.getChildName());
		}
	}
	
	protected Set<Attribute<?, ?>> clearValidationResults(Attribute<?,?> attribute){
		Set<Attribute<?,?>> checkDependencies = attribute.getCheckDependencies();
		clearValidationResults(checkDependencies);
		return checkDependencies;
	}

	protected void clearValidationResults(Set<Attribute<?, ?>> checkDependencies) {
		for (Attribute<?, ?> attr : checkDependencies) {
			attr.clearValidationResults();
		}
	}
	
	protected Map<Integer, Object> createFieldValuesMap(
			Attribute<?, ?> attribute) {
		Map<Integer, Object> fieldValues = new HashMap<Integer, Object>();
		int fieldCount = attribute.getFieldCount();
		for (int idx = 0; idx < fieldCount; idx ++) {
			Field<?> field = attribute.getField(idx);
			fieldValues.put(idx, field.getValue());
		}
		return fieldValues;
	}

	protected <V extends Value> void setSymbolOnFields(
			Attribute<? extends NodeDefinition, V> attribute,
			FieldSymbol symbol) {
		for (Field<?> field : attribute.getFields()) {
			setFieldSymbol(field, symbol);
		}
	}
	
	protected <V extends Value> void setRemarksOnFirstField(
			Attribute<? extends NodeDefinition, V> attribute,
			String remarks) {
		Field<?> field = attribute.getField(0);
		field.setRemarks(remarks);
	}
	
	protected void setErrorConfirmed(Attribute<?,?> attribute, boolean confirmed){
		int fieldCount = attribute.getFieldCount();
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(CONFIRMED_ERROR_POSITION, confirmed);
		}
	}
	
	protected void setMissingValueApproved(Entity parentEntity, String childName, boolean approved) {
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		childState.set(APPROVED_MISSING_POSITION, approved);
	}
	
	protected void setDefaultValueApplied(Attribute<?, ?> attribute, boolean applied) {
		int fieldCount = attribute.getFieldCount();
		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(DEFAULT_APPLIED_POSITION, applied);
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
	
	//END OF RECORD UPDATE METHODS
	
	private void checkAllKeysSpecified(CollectRecord record) throws MissingRecordKeyException {
		CollectSurvey survey = (CollectSurvey) record.getSurvey();
		Schema schema = survey.getSchema();
		Entity rootEntity = record.getRootEntity();
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		List<KeyAttributeDefinition> keyDefns = schema.getKeyAttributeDefinitions(rootEntity.getDefinition());
		for (int i = 0; i < keyDefns.size(); i++) {
			NodeDefinition keyDefn = (NodeDefinition) keyDefns.get(i);
			boolean required = keyDefn.getMinCount() != null && keyDefn.getMinCount() > 0;
			if ( required ) {
				String path = keyDefn.getPath();
				Node<?> keyNode = record.getNodeByPath(path);
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
