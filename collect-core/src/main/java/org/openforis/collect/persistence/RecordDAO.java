package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcAttributeValue.OFC_ATTRIBUTE_VALUE;
import static org.openforis.collect.persistence.jooq.tables.OfcEntity.OFC_ENTITY;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.tables.OfcUser.OFC_USER;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.OfcRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.RecordContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordDAO extends JooqDaoSupport {
	
	public static final String ORDER_BY_DATE_CREATED = "creationDate";
	public static final String ORDER_BY_DATE_MODIFIED = "modifiedDate";
	
	@Autowired
	private DataDao dataDao;
	
	@Transactional
	public CollectRecord load(CollectSurvey survey, RecordContext recordContext, int recordId) throws DataInconsistencyException, NonexistentIdException {
		CollectRecord record = loadRecord(survey, recordContext, recordId);
		loadData(record);

		return record;
	}

	@Transactional
	public void insert(CollectRecord record) {
		insertRecord(record);
		dataDao.insertData(record);
	}
	
	@Transactional
	public void update(CollectRecord record) {
		updateRecord(record);
		deleteData(record.getId());
		dataDao.insertData(record);
	}
	
	@Transactional
	public void saveOrUpdate(CollectRecord record) {
		if (record.getId() == null) {
			insertRecord(record);
		} else {
			updateRecord(record);
			deleteData(record.getId());
		}
		dataDao.insertData(record);
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
	public int getRecordCount(EntityDefinition rootEntityDefinition) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(OFC_RECORD).where(OFC_RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId())).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public void lock(Integer recordId, User user) throws RecordLockedException, AccessDeniedException, MultipleEditException {
		Factory jf = getJooqFactory();
		//check if user has already locked another record
		checkLock(user);
		Record result = jf.select(OFC_RECORD.LOCKED_BY_ID, OFC_USER.USERNAME).from(OFC_RECORD)
				.leftOuterJoin(OFC_USER).on(OFC_RECORD.LOCKED_BY_ID.equal(OFC_USER.ID))
				.where(OFC_RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = result.getValueAsInteger(OFC_RECORD.LOCKED_BY_ID);
		if (lockedById == null || lockedById.equals(user.getId())) {
			jf.update(OFC_RECORD).set(OFC_RECORD.LOCKED_BY_ID, user.getId()).where(OFC_RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = result.getValueAsString(OFC_USER.USERNAME);
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
		Record r = jf.select(OFC_RECORD.ID).from(OFC_RECORD).where(OFC_RECORD.LOCKED_BY_ID.equal(user.getId())).fetchAny();
		if(r != null){
			return r.getValueAsInteger(OFC_RECORD.ID);
		} else {
			return null;
		}
	}
	
	@Transactional
	public void unlock(Integer recordId, User user) throws RecordLockedException {
		Factory jf = getJooqFactory();
		Record selectResult = jf.select(OFC_RECORD.LOCKED_BY_ID, OFC_USER.USERNAME).from(OFC_RECORD)
				.leftOuterJoin(OFC_USER).on(OFC_RECORD.LOCKED_BY_ID.equal(OFC_USER.ID))
				.where(OFC_RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = selectResult.getValueAsInteger(OFC_RECORD.LOCKED_BY_ID);
		if (lockedById != null && lockedById.equals(user.getId())) {
			jf.update(OFC_RECORD).set(OFC_RECORD.LOCKED_BY_ID, (Integer)null).where(OFC_RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = selectResult.getValueAsString(OFC_USER.USERNAME);
			throw new RecordLockedException("Record locked by another user", userName);
		}
	}

	@Transactional
	public void unlockAll() {
		Factory jf = getJooqFactory();
		jf.update(OFC_RECORD).set(OFC_RECORD.LOCKED_BY_ID, (Integer)null).execute();
	}

	private CollectRecord loadRecord(CollectSurvey survey, RecordContext recordContext, int recordId) throws NonexistentIdException {
		Factory jf = getJooqFactory();
		Record r = jf.select().from(OFC_RECORD).where(OFC_RECORD.ID.equal(recordId)).fetchOne();
		int rootEntityId = r.getValueAsInteger(OFC_RECORD.ROOT_ENTITY_ID);
		String version = r.getValueAsString(OFC_RECORD.MODEL_VERSION);
		
		Integer id = r.getValueAsInteger(OFC_RECORD.ID);
		if(id==null){
			throw new NonexistentIdException();
		}
		Schema schema = survey.getSchema();
		NodeDefinition rootEntityDefn = schema.getById(rootEntityId);
		if (rootEntityDefn == null) {
			throw new NullPointerException("Unknown root entity id " + rootEntityId);
		}

		CollectRecord record = new CollectRecord(recordContext, survey, version);
		
		mapRecordToCollectRecord(r, record);
		
		return record;
	}
	
	/**
	 * Load a list of record summaries that match the primary keys
	 * 
	 * @param survey
	 * @param recordContext
	 * @param rootEntity
	 * @param keys
	 * @return
	 */
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, RecordContext recordContext, String rootEntity, String... keys) {
		Factory jf = getJooqFactory();
		SelectQuery q = jf.selectQuery();
		q.addFrom(OFC_RECORD);
		q.addSelect(OFC_RECORD.getFields());
		int i = 0;
		for (String key : keys) {
			String keyColumnName = "key" + (++i);
			@SuppressWarnings("unchecked")
			Field<String> keyField = (Field<String>) OFC_RECORD.getField(keyColumnName);
			q.addConditions(keyField.equal(key));
		}
		Result<Record> records = q.fetch();
		List<CollectRecord> recordSummaries = mapRecordsToSummaries(recordContext, records, survey, rootEntity);
		return recordSummaries;
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, RecordContext recordContext, String rootEntity, int offset, int maxRecords, String orderByField, String filter) {
		Factory jf = getJooqFactory();
		OfcRecord r = OFC_RECORD.as("r");
		
		SelectQuery q = jf.selectQuery();
		
		q.addFrom(r);
		
		q.addSelect(
				r.DATE_CREATED, 
				r.DATE_MODIFIED, 
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
				r.WARNINGS,
				r.KEY1,
				r.KEY2,
				r.KEY3,
				r.COUNT1,
				r.COUNT2,
				r.COUNT3,
				r.COUNT4,
				r.COUNT5,
				r.SUBMITTED_ID
				);
		
		//add condition on submitted_id: always find records not yet submitted
		Condition notExistsPromoted = Factory.notExists(jf.select(OFC_RECORD.ID).from(OFC_RECORD).where(OFC_RECORD.SUBMITTED_ID.equal(r.ID)));
		q.addConditions(notExistsPromoted);
		
		//add order by condition
		Field<?> orderBy = null;
		if(orderByField != null) {
			if(ORDER_BY_DATE_CREATED.equals(orderByField)) {
				orderBy = r.DATE_CREATED;
			} else if(ORDER_BY_DATE_MODIFIED.equals(orderByField)) {
				orderBy = r.DATE_MODIFIED;
			} else {
				//try to find a field matching the orderByField passed
				List<Field<?>> selectFields = q.getSelect();
				for (Field<?> field : selectFields) {
					if(orderByField.equals(field.getName())) {
						orderBy = field;
						break;
					}
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
		
		List<CollectRecord> result = mapRecordsToSummaries(recordContext, records, survey, rootEntity);
		return result;
	}
	
	private List<CollectRecord> mapRecordsToSummaries(RecordContext recordContext, List<Record> records, CollectSurvey survey, String rootEntity) {
		List<CollectRecord> result = new ArrayList<CollectRecord>();
		
		for (Record r : records) {
			String versionName = r.getValue(OFC_RECORD.MODEL_VERSION);
			
			CollectRecord record = new CollectRecord(recordContext, survey, versionName);
			
			mapRecordToCollectRecord(r, record);
			
			result.add(record);
		}
		return result;
	}
	
	private void loadData(CollectRecord record) throws DataInconsistencyException {
		dataDao.load(record);
	}

	private void insertRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer rootEntityId = rootEntityDefinition.getId();
		if (rootEntityId == null) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		Factory jf = getJooqFactory();
		int recordId = jf.nextval(OFC_RECORD_ID_SEQ).intValue();
		InsertSetMoreStep<OfcRecordRecord> setStep = jf.insertInto(OFC_RECORD)
				.set(OFC_RECORD.ID, recordId)
				.set(OFC_RECORD.ROOT_ENTITY_ID, rootEntityId)
				.set(OFC_RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
				.set(OFC_RECORD.CREATED_BY_ID, record.getCreatedBy() != null ? record.getCreatedBy().getId(): null)
				.set(OFC_RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				.set(OFC_RECORD.MODIFIED_BY_ID, record.getModifiedBy() != null ? record.getModifiedBy().getId(): null)
				.set(OFC_RECORD.MODEL_VERSION, record.getVersion().getName())
				.set(OFC_RECORD.STEP, record.getStep().getStepNumber())
				.set(OFC_RECORD.SKIPPED, record.getSkipped())
				.set(OFC_RECORD.MISSING, record.getMissing())
				.set(OFC_RECORD.ERRORS, record.getErrors())
				.set(OFC_RECORD.WARNINGS, record.getWarnings())
				.set(OFC_RECORD.SUBMITTED_ID, record.getSubmittedId())
				;
		//set keys
		List<String> keys = record.getRootEntityKeys();
		switch(keys.size()) {
			case 3:
				setStep.set(OFC_RECORD.KEY3, keys.get(2));
			case 2:
				setStep.set(OFC_RECORD.KEY2, keys.get(1));
			case 1:
				setStep.set(OFC_RECORD.KEY1, keys.get(0));
				break;
		}
		//set counts
		List<Integer> counts = record.getEntityCounts();
		switch(counts.size()) {
			case 5:
				setStep.set(OFC_RECORD.COUNT5, counts.get(4));
			case 4:
				setStep.set(OFC_RECORD.COUNT4, counts.get(3));
			case 3:
				setStep.set(OFC_RECORD.COUNT3, counts.get(2));
			case 2:
				setStep.set(OFC_RECORD.COUNT2, counts.get(1));
			case 1:
				setStep.set(OFC_RECORD.COUNT1, counts.get(0));
				break;
		}
		setStep.execute();
		record.setId(recordId);
	}

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
		Factory jf = getJooqFactory();
		UpdateSetMoreStep<OfcRecordRecord> setStep = jf.update(OFC_RECORD)
				.set(OFC_RECORD.ROOT_ENTITY_ID, rootEntityId)
				.set(OFC_RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
				.set(OFC_RECORD.CREATED_BY_ID, record.getCreatedBy() != null ? record.getCreatedBy().getId(): null)
				.set(OFC_RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				.set(OFC_RECORD.MODIFIED_BY_ID, record.getModifiedBy() != null ? record.getModifiedBy().getId(): null)
				.set(OFC_RECORD.MODEL_VERSION, record.getVersion().getName())
				.set(OFC_RECORD.STEP, record.getStep().getStepNumber())
				.set(OFC_RECORD.SKIPPED, record.getSkipped())
				.set(OFC_RECORD.MISSING, record.getMissing())
				.set(OFC_RECORD.ERRORS, record.getErrors())
				.set(OFC_RECORD.WARNINGS, record.getWarnings())
				.set(OFC_RECORD.SUBMITTED_ID, record.getSubmittedId())
				;
		//set keys
		List<String> keys = record.getRootEntityKeys();
		switch(keys.size()) {
			case 3:
				setStep.set(OFC_RECORD.KEY3, keys.get(2));
			case 2:
				setStep.set(OFC_RECORD.KEY2, keys.get(1));
			case 1:
				setStep.set(OFC_RECORD.KEY1, keys.get(0));
				break;
		}
		//set counts
		List<Integer> counts = record.getEntityCounts();
		switch(counts.size()) {
			case 5:
				setStep.set(OFC_RECORD.COUNT5, counts.get(4));
			case 4:
				setStep.set(OFC_RECORD.COUNT4, counts.get(3));
			case 3:
				setStep.set(OFC_RECORD.COUNT3, counts.get(2));
			case 2:
				setStep.set(OFC_RECORD.COUNT2, counts.get(1));
			case 1:
				setStep.set(OFC_RECORD.COUNT1, counts.get(0));
				break;
		}
		setStep.where(OFC_RECORD.ID.equal(recordId))
			.execute();
	}
	
	private void deleteRecord(Integer recordId) {
		if (recordId == null) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Factory jf = getJooqFactory();
		jf.delete(OFC_RECORD).where(OFC_RECORD.ID.equal(recordId)).execute();
	}
	
	private void deleteData(int recordId) {
		Factory jf = getJooqFactory();
		jf.delete(OFC_ATTRIBUTE_VALUE).where(OFC_ATTRIBUTE_VALUE.RECORD_ID.equal(recordId)).execute();
		jf.delete(OFC_ENTITY).where(OFC_ENTITY.RECORD_ID.equal(recordId)).execute();
	}

	//TODO move to a Mapper class
	private void mapRecordToCollectRecord(Record r, CollectRecord collectRecord) {
		collectRecord.setId(r.getValue(OFC_RECORD.ID));
		collectRecord.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
		// record.setCreatedBy(r.getValueAsString(OFC_RECORD.CREATED_BY));
		collectRecord.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
		// record.setModifiedBy(r.getValueAsString(OFC_RECORD.MODIFIED_BY));
		
		collectRecord.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
		collectRecord.setErrors(r.getValue(OFC_RECORD.ERRORS));
		collectRecord.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
		collectRecord.setMissing(r.getValue(OFC_RECORD.MISSING));
		collectRecord.setSubmittedId(r.getValue(OFC_RECORD.SUBMITTED_ID));
		
		Integer step = r.getValue(OFC_RECORD.STEP);
		if(step != null) {
			collectRecord.setStep(Step.valueOf(step));
		}
		
		//create list of entity counts
		List<Integer> counts = new ArrayList<Integer>();
		Integer count;
		count = r.getValue(OFC_RECORD.COUNT1);
		counts.add(count);
		count = r.getValue(OFC_RECORD.COUNT2);
		counts.add(count);
		count = r.getValue(OFC_RECORD.COUNT3);
		counts.add(count);
		count = r.getValue(OFC_RECORD.COUNT4);
		counts.add(count);
		count = r.getValue(OFC_RECORD.COUNT5);
		counts.add(count);
		
		collectRecord.setEntityCounts(counts);
		
		//create list of keys
		List<String> keys = new ArrayList<String>();
		String key;
		key = r.getValue(OFC_RECORD.KEY1);
		keys.add(key);
		key = r.getValue(OFC_RECORD.KEY2);
		keys.add(key);
		key = r.getValue(OFC_RECORD.KEY3);
		keys.add(key);
		
		collectRecord.setKeys(keys);
	}

	public DataDao getDataDao() {
		return dataDao;
	}

	public void setDataDao(DataDao dataDao) {
		this.dataDao = dataDao;
	}
}
