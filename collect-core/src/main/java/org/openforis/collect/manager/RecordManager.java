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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
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
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
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
	private final Log log = LogFactory.getLog(RecordManager.class);
	
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
		record.clearNodeStates();
		record.setStep( nextStep );
		record.updateDerivedStates();
		
		recordDao.update( record );
	}

	/**
	 * Applies default values on each descendant attribute of a record in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * 
	 * @param record
	 * @throws InvalidExpressionException 
	 */
	public void applyDefaultValues(CollectRecord record) {
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
					applyDefaultValue(attribute);
				}
			} else if ( child instanceof Entity ) {
				applyDefaultValues((Entity) child);
			}
		}
	}

	public <V extends Value> void applyDefaultValue(Attribute<?, V> attribute) {
		AttributeDefinition attributeDefn = (AttributeDefinition) attribute.getDefinition();
		List<AttributeDefault> defaults = attributeDefn.getAttributeDefaults();
		if ( defaults != null && defaults.size() > 0 ) {
			for (AttributeDefault attributeDefault : defaults) {
				try {
					V value = attributeDefault.evaluate(attribute);
					if ( value != null ) {
						attribute.setValue(value);
						CollectRecord record = (CollectRecord) attribute.getRecord();
						record.setDefaultValueApplied(attribute, true);
						clearRelevanceRequiredStates(attribute);
						clearValidationResults(attribute);
					}
				} catch (InvalidExpressionException e) {
					log.warn("Error applying default value for attribute " + attributeDefn.getPath());
				}
			}
		}
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
		addEmptyNodes(rootEntity);
		record.updateDerivedStates();
		record.updateRootEntityKeyValues();
		record.updateEntityCounts();
		recordDao.update(record);
		if ( isLockingEnabled() ) {
			releaseLock(recordId);
		}
	}
	
	public Entity addEntity(Entity parentEntity, String nodeName) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName);
		addEmptyNodes(entity);
		return entity;
	}

	public Entity addEntity(Entity parentEntity, String nodeName, int idx) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName, idx);
		addEmptyNodes(entity);
		return entity;
	}
	
	public void moveNode(CollectRecord record, int nodeId, int index) {
		Node<?> node = record.getNodeByInternalId(nodeId);
		Entity parent = node.getParent();
		String name = node.getName();
		List<Node<?>> siblings = parent.getAll(name);
		int oldIndex = siblings.indexOf(node);
		parent.move(name, oldIndex, index);
	}
	
	public void addEmptyNodes(Entity entity) {
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
					addEntity(entity, childName);
				}
				count ++;
			}
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setFieldValue(Attribute<?,?> attribute, Object value, String remarks, FieldSymbol symbol, int fieldIdx){
		if(fieldIdx < 0){
			fieldIdx = 0;
		}
		Field<V> field = (Field<V>) attribute.getField(fieldIdx);
		field.setValue((V)value);
		field.setRemarks(remarks);
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
		CollectRecord record = (CollectRecord) attribute.getRecord();
		record.setDefaultValueApplied(attribute, false);
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Value> void setAttributeValue(Attribute<?,V> attribute, Object value, String remarks){
		attribute.setValue((V)value);
		Field<V> field = (Field<V>) attribute.getField(0);
		field.setRemarks(remarks);
		field.setSymbol(null);
		CollectRecord record = (CollectRecord) attribute.getRecord();
		record.setDefaultValueApplied(attribute, false);
	}
	
	public Set<Attribute<?, ?>> clearValidationResults(Attribute<?,?> attribute){
		Set<Attribute<?,?>> checkDependencies = attribute.getCheckDependencies();
		clearValidationResults(checkDependencies);
		return checkDependencies;
	}

	public void clearValidationResults(Set<Attribute<?, ?>> checkDependencies) {
		for (Attribute<?, ?> attr : checkDependencies) {
			attr.clearValidationResults();
		}
	}
	
	public Set<NodePointer> clearRelevanceRequiredStates(Node<?> node){
		Set<NodePointer> relevantDependencies = node.getRelevantDependencies();
		clearRelevantDependencies(relevantDependencies);
		Set<NodePointer> requiredDependencies = node.getRequiredDependencies();
		requiredDependencies.addAll(relevantDependencies);
		clearRequiredDependencies(requiredDependencies);
		return requiredDependencies;
	}
	
	public void clearRelevantDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRelevanceState(nodePointer.getChildName());
		}
	}
	
	public void clearRequiredDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRequiredState(nodePointer.getChildName());
		}
	}
	
	private void addEmptyEnumeratedEntities(Entity parentEntity) {
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

	private void addEmptyEnumeratedEntities(Entity parentEntity, EntityDefinition enumerableEntityDefn) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = enumerableEntityDefn.getEnumeratingKeyCodeAttribute(version);
		if(enumeratingCodeDefn != null) {
			String enumeratedEntityName = enumerableEntityDefn.getName();
			CodeList list = enumeratingCodeDefn.getList();
			List<CodeListItem> items = list.getItems();
			for (int i = 0; i < items.size(); i++) {
				CodeListItem item = items.get(i);
				if(version == null || version.isApplicable(item)) {
					String code = item.getCode();
					Entity enumeratedEntity = getEnumeratedEntity(parentEntity, enumerableEntityDefn, enumeratingCodeDefn, code);
					if( enumeratedEntity == null ) {
						Entity addedEntity = addEntity(parentEntity, enumeratedEntityName, i);
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

	private Entity getEnumeratedEntity(Entity parentEntity, EntityDefinition childEntityDefn, 
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

