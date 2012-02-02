package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.UserAccount.USER_ACCOUNT;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.jooq.DataLoader;
import org.openforis.collect.persistence.jooq.DataPersister;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.RecordRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RecordDAO extends JooqDaoSupport {
	
	public static final String DATE_CREATED_ALIAS = "creationDate";
	public static final String DATE_MODIFIED_ALIAS = "modifiedDate";
	
	public static final String KEY_ALIAS_PREFIX = "key_";
	public static final String COUNT_ALIAS_PREFIX = "count_";
	
	@Transactional
	public CollectRecord load(Survey survey, int recordId) throws DataInconsistencyException, NonexistentIdException {
		CollectRecord record = loadRecord(survey, recordId);
		loadData(record);

		return record;
	}

	@Transactional
	public void saveOrUpdate(CollectRecord record) {
		if (record.getId() == null) {
			insertRecord(record);
		} else {
			updateRecord(record);
			deleteData(record.getId());
		}
		insertData(record);
	}

	@Transactional
	public void delete(CollectRecord record) {
		Integer id = record.getId();
		delete(id);
	}
	
	@Transactional
	public void delete(Integer id) {
		deleteData(id);
		deleteRecord(id);
	}
	
	@Transactional
	public int getCountRecords(EntityDefinition rootEntityDefinition) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(RECORD).where(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId())).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public void lock(Integer recordId, User user) throws RecordLockedException, AccessDeniedException, MultipleEditException {
		Factory jf = getJooqFactory();
		//check if user has already locked another record
		checkLock(user);
		Record result = jf.select(RECORD.LOCKED_BY_ID, USER_ACCOUNT.USERNAME).from(RECORD)
				.leftOuterJoin(USER_ACCOUNT).on(RECORD.LOCKED_BY_ID.equal(USER_ACCOUNT.ID))
				.where(RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = result.getValueAsInteger(RECORD.LOCKED_BY_ID);
		if (lockedById == null || lockedById.equals(user.getId())) {
			jf.update(RECORD).set(RECORD.LOCKED_BY_ID, user.getId()).where(RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = result.getValueAsString(org.openforis.collect.persistence.jooq.tables.UserAccount.USER_ACCOUNT.USERNAME);
			throw new RecordLockedException("Record already locked", userName);
		}
	}

	public void checkLock(User user) throws MultipleEditException  {
		Factory jf = getJooqFactory();
		Integer recordId = getLockedRecordId(jf, user);
		if(recordId != null) {
			throw new MultipleEditException("User has locked another record " + recordId);
		}
	}
	
	private Integer getLockedRecordId(Factory jf, User user) {
		Record r = jf.select(RECORD.ID).from(RECORD).where(RECORD.LOCKED_BY_ID.equal(user.getId())).fetchAny();
		if(r != null){
			return r.getValueAsInteger(RECORD.ID);
		} else {
			return null;
		}
	}
	
	@Transactional
	public void unlock(Integer recordId, User user) throws RecordLockedException {
		Factory jf = getJooqFactory();
		Record selectResult = jf.select(RECORD.LOCKED_BY_ID, USER_ACCOUNT.USERNAME).from(RECORD)
				.leftOuterJoin(USER_ACCOUNT).on(RECORD.LOCKED_BY_ID.equal(USER_ACCOUNT.ID))
				.where(RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = selectResult.getValueAsInteger(RECORD.LOCKED_BY_ID);
		if (lockedById != null && lockedById.equals(user.getId())) {
			jf.update(RECORD).set(RECORD.LOCKED_BY_ID, (Integer)null).where(RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = selectResult.getValueAsString(USER_ACCOUNT.USERNAME);
			throw new RecordLockedException("Record locked by another user", userName);
		}
	}

	@Transactional
	public void unlockAll() {
		Factory jf = getJooqFactory();
		jf.update(RECORD).set(RECORD.LOCKED_BY_ID, (Integer)null).execute();
	}

	private CollectRecord loadRecord(Survey survey, int recordId) throws NonexistentIdException {
		Factory jf = getJooqFactory();
		Record r = jf.select().from(RECORD).where(RECORD.ID.equal(recordId)).fetchOne();
		int rootEntityId = r.getValueAsInteger(RECORD.ROOT_ENTITY_ID);
		String version = r.getValueAsString(RECORD.MODEL_VERSION);
		
		Integer id = r.getValueAsInteger(RECORD.ID);
		if(id==null){
			throw new NonexistentIdException();
		}
		Schema schema = survey.getSchema();
		NodeDefinition rootEntityDefn = schema.getById(rootEntityId);
		if (rootEntityDefn == null) {
			throw new NullPointerException("Unknown root entity id " + rootEntityId);
		}
		String rootEntityName = rootEntityDefn.getName();

		CollectRecord record = new CollectRecord(survey, rootEntityName, version);
		record.setId(recordId);
		record.setCreationDate(r.getValueAsDate(RECORD.DATE_CREATED));
		// record.setCreatedBy(r.getValueAsString(RECORD.CREATED_BY));
		record.setModifiedDate(r.getValueAsDate(RECORD.DATE_MODIFIED));
		// record.setModifiedBy(r.getValueAsString(RECORD.MODIFIED_BY));
		record.setMissing(r.getValueAsInteger(RECORD.MISSING));
		record.setSkipped(r.getValueAsInteger(RECORD.SKIPPED));
		record.setErrors(r.getValueAsInteger(RECORD.ERRORS));
		record.setWarnings(r.getValueAsInteger(RECORD.WARNINGS));
			
		return record;
	}

	@Transactional
	public List<RecordSummary> loadSummaries(Survey survey, String rootEntity, List<EntityDefinition> countable, int offset, int maxRecords, String orderByField, String filter) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinition(rootEntity);
		List<AttributeDefinition> keyDefs = rootEntityDef.getKeyAttributeDefinitions();
		
		Factory jf = getJooqFactory();
		org.openforis.collect.persistence.jooq.tables.Record r = RECORD.as("r");
		
		SelectQuery q = jf.selectQuery();
		
		q.addFrom(r);
		
		q.addSelect(
				r.DATE_CREATED.as(DATE_CREATED_ALIAS), 
				r.DATE_MODIFIED.as(DATE_MODIFIED_ALIAS), 
				r.ERRORS, 
				r.ID, 
				r.LOCKED_BY_ID, 
				r.MISSING,  
				r.MODEL_VERSION, 
				r.MODIFIED_BY_ID, 
				r.ROOT_ENTITY_ID, 
				r.SKIPPED, 
				r.STATE, 
				r.STEP, 
				r.WARNINGS
				);
		{
			//add keys to select with an alias like KEY_ALIAS_PREFIX + ATTRIBUTE_NAME
			int position = 1;
			for (AttributeDefinition def : keyDefs) {
				String alias = KEY_ALIAS_PREFIX + def.getName();
				Field<?> field = getKeyField(r, position).as(alias);
				q.addSelect(field);
				position ++;
			}
		}
		{
			//add count columns to select with an alias like COUNT_ALIAS_PREFIX + ENTITY_NAME
			int position = 1;
			for (EntityDefinition def : countable) {
				String alias = COUNT_ALIAS_PREFIX + def.getName();
				Field<?> field = getCountField(r, position).as(alias);
				q.addSelect(field);
				position ++;
			}
		}
		//add order by condition
		Field<?> orderBy = null;
		if(orderByField != null) {
			List<Field<?>> selectFields = q.getSelect();
			for (Field<?> field : selectFields) {
				if(orderByField.equals(field.getName())) {
					orderBy = field;
					break;
				}
			}
		}
		if(orderBy != null) {
			q.addOrderBy(orderBy);
		}
		//always order by ID to avoid pagination issues
		q.addOrderBy(r.ID);
		
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> records = q.fetch();
		
		List<RecordSummary> result = mapRecordsToSummaries(records, keyDefs, countable);
		return result;
	}

	private List<RecordSummary> mapRecordsToSummaries(List<Record> records, List<AttributeDefinition> keyDefs, List<EntityDefinition> countable) {
		List<RecordSummary> result = new ArrayList<RecordSummary>();
		
		for (Record record : records) {
			Integer id = record.getValueAsInteger(RECORD.ID);
			//String createdBy = r.getValueAsString(USER_MODIFIED_BY_ALIAS);
			String createdBy = null;
			Date dateCreated = record.getValueAsDate(DATE_CREATED_ALIAS);
			//String modifiedBy = r.getValueAsString(USER_CREATED_BY_ALIAS);
			String modifiedBy = null;
			Date modifiedDate = record.getValueAsDate(DATE_MODIFIED_ALIAS);
			Integer step = record.getValueAsInteger(RECORD.STEP);
			Integer warnings = record.getValueAsInteger(RECORD.WARNINGS);
			Integer errors = record.getValueAsInteger(RECORD.ERRORS);
			Integer skipped = record.getValueAsInteger(RECORD.SKIPPED);
			Integer missing = record.getValueAsInteger(RECORD.MISSING);
			//create count map
			Map<String, Integer> entityCounts = new HashMap<String, Integer>();
			for (EntityDefinition def : countable) {
				String alias = COUNT_ALIAS_PREFIX + def.getName();
				String key = def.getName();
				Integer value = record.getValueAsInteger(alias);
				entityCounts.put(key, value);
			}
			//create key attributes map
			Map<String, String> keyAttributes = new HashMap<String, String>();
			for (AttributeDefinition attributeDefinition : keyDefs) {
				String projectionAlias = KEY_ALIAS_PREFIX + attributeDefinition.getName();
				String key = attributeDefinition.getName();
				Object value = record.getValue(projectionAlias);
				String valueStr = value != null ? value.toString(): "";
				keyAttributes.put(key, valueStr);
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, createdBy, dateCreated, modifiedBy, modifiedDate, step,
					skipped, missing, errors, warnings);
			result.add(recordSummary);
		}
		return result;
	}
	private void loadData(CollectRecord record) throws DataInconsistencyException {
		DataLoader loader = new DataLoader(getJooqFactory());
		loader.load(record);
	}

	@SuppressWarnings("unchecked")
	private void insertRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer rootEntityId = rootEntityDefinition.getId();
		if (rootEntityId == null) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		int recordId = jf.nextval(RECORD_ID_SEQ).intValue();
		InsertSetMoreStep<RecordRecord> setStep = jf.insertInto(RECORD)
				.set(RECORD.ID, recordId)
				.set(RECORD.ROOT_ENTITY_ID, rootEntityId)
				.set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
				.set(RECORD.CREATED_BY_ID, record.getCreatedBy() != null ? record.getCreatedBy().getId(): null)
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				.set(RECORD.MODIFIED_BY_ID, record.getModifiedBy() != null ? record.getModifiedBy().getId(): null)
				.set(RECORD.MODEL_VERSION, record.getVersion().getName())
				.set(RECORD.STEP, record.getStep().getStepNumber())
				.set(RECORD.SKIPPED, record.getSkipped())
				.set(RECORD.MISSING, record.getMissing())
				.set(RECORD.ERRORS, record.getErrors())
				.set(RECORD.WARNINGS, record.getWarnings())
				;
		//set counts
		Collection<Integer> counts = record.getCounts().values();
		int position = 1;
		for (Integer count : counts) {
			TableField<RecordRecord,Integer> countField = getCountField(RECORD, position);
			setStep.set(countField, count);
			position ++;
		}
		//set keys
		position = 1;
		Collection<String> keys = record.getKeys().values();
		for (String key : keys) {
			@SuppressWarnings("rawtypes")
			Field field = getKeyField(RECORD, position);
			setStep.set(field, key);
		}
		setStep.execute();
		record.setId(recordId);
	}

	@SuppressWarnings("unchecked")
	private void updateRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer recordId = record.getId();
		if (recordId == null) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Integer rootEntityId = rootEntityDefinition.getId();
		if (rootEntityId == null) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		UpdateSetMoreStep<RecordRecord> updateStep = jf.update(RECORD)
				.set(RECORD.ROOT_ENTITY_ID, rootEntityId)
				.set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
				.set(RECORD.CREATED_BY_ID, record.getCreatedBy() != null ? record.getCreatedBy().getId(): null)
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				.set(RECORD.MODIFIED_BY_ID, record.getModifiedBy() != null ? record.getModifiedBy().getId(): null)
				.set(RECORD.MODEL_VERSION, record.getVersion().getName())
				.set(RECORD.STEP, record.getStep().getStepNumber())
				.set(RECORD.SKIPPED, record.getSkipped())
				.set(RECORD.MISSING, record.getMissing())
				.set(RECORD.ERRORS, record.getErrors())
				.set(RECORD.WARNINGS, record.getWarnings())
				;
		//set counts
		Collection<Integer> counts = record.getCounts().values();
		int position = 1;
		for (Integer count : counts) {
			TableField<RecordRecord,Integer> countField = getCountField(RECORD, position);
			updateStep.set(countField, count);
			position ++;
		}
		//set keys
		position = 1;
		Collection<String> keys = record.getKeys().values();
		for (String key : keys) {
			@SuppressWarnings("rawtypes")
			Field field = getKeyField(RECORD, position);
			updateStep.set(field, key);
		}
		updateStep.where(RECORD.ID.equal(recordId))
			.execute();
	}
	
	private void deleteRecord(Integer recordId) {
		if (recordId == null) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Factory jf = getJooqFactory();
		jf.delete(RECORD).where(RECORD.ID.equal(recordId)).execute();
	}
	
	private void deleteData(int recordId) {
		Factory jf = getJooqFactory();
		jf.delete(DATA).where(DATA.RECORD_ID.equal(recordId)).execute();
	}

	private void insertData(final CollectRecord record) {
		// N.B.: traversal order matters; dfs so that parent id's are assigned before children
		Entity root = record.getRootEntity();
		root.traverse(new NodeVisitor() {
			DataPersister persister = new DataPersister(getJooqFactory());

			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				persister.persist(node, idx);
			}
		});
	}
	
	private static TableField<RecordRecord, ?> getKeyField( org.openforis.collect.persistence.jooq.tables.Record r, int position) {
		switch(position) {
			case 1:
				return r.KEY1;
			case 2:
				return r.KEY2;
			case 3:
				return r.KEY3;
			default:
				throw new RuntimeException("Exceeded maximum number of supported keys for this root entity");
		}
	}
	
	private static TableField<RecordRecord, Integer> getCountField( org.openforis.collect.persistence.jooq.tables.Record r, int position) {
		switch(position) {
			case 1:
				return r.COUNT1;
			case 2:
				return r.COUNT2;
			case 3:
				return r.COUNT3;
			case 4:
				return r.COUNT4;
			case 5:
				return r.COUNT5;
			default:
				throw new RuntimeException("Exceeded maximum number of countable entities");
		}
	}
}
