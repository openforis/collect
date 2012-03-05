package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao.JooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@SuppressWarnings("rawtypes")
public class RecordDao extends MappingJooqDaoSupport<CollectRecord, JooqFactory> {
	
	public static final String ORDER_BY_DATE_CREATED = "creationDate";
	public static final String ORDER_BY_DATE_MODIFIED = "modifiedDate";
	
	private static final TableField[] KEY_FIELDS = 
		{OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3};
	private static final TableField[] COUNT_FIELDS = 
		{OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};
	private static final TableField[] SUMMARY_FIELDS = 
		{OFC_RECORD.DATE_CREATED, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.ERRORS, OFC_RECORD.ID, 
	     OFC_RECORD.LOCKED_BY_ID, OFC_RECORD.MISSING, OFC_RECORD.MODEL_VERSION, OFC_RECORD.MODIFIED_BY_ID, 
	     OFC_RECORD.ROOT_ENTITY_ID,	OFC_RECORD.SKIPPED,	OFC_RECORD.STATE, OFC_RECORD.STEP,
	     OFC_RECORD.WARNINGS, OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3, OFC_RECORD.COUNT1,
	     OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};

	public RecordDao() {
		super(JooqFactory.class);
	}
	
	public CollectRecord load(CollectSurvey survey, int id, int step) {
		JooqFactory jf = getMappingJooqFactory(survey, step);
		SelectQuery query = jf.selectRecordQuery(id);
		Record r = query.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}

	private JooqFactory getMappingJooqFactory(CollectSurvey survey) {
		return new JooqFactory(getConnection(), survey);
	}

	private JooqFactory getMappingJooqFactory(CollectSurvey survey, int step) {
		return new JooqFactory(getConnection(), survey, step);
	}
	
	@Deprecated
	public void saveOrUpdate(CollectRecord record) {
		if ( record.getId() == null ) {
			insert(record);
		} else {
			update(record);
		}
	}

	public int countRecords(int rootDefinitionId) {
		JooqFactory f = getMappingJooqFactory();
		SelectQuery q = f.selectCountQuery();
		q.addConditions(OFC_RECORD.ROOT_ENTITY_ID.equal(rootDefinitionId));
		Record r = q.fetchOne();
		return r.getValueAsInteger(0);
	}

	@Deprecated
	@Transactional
	public int getRecordCount(EntityDefinition rootEntityDefinition) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(OFC_RECORD).where(OFC_RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId())).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public void lock(Integer recordId, User user) throws RecordPersistenceException {
		Factory jf = getJooqFactory();
		//check if user has already locked another record
		checkLock(user);
		Record result = jf
				.select(OFC_RECORD.LOCKED_BY_ID, OFC_USER.USERNAME)
				.from(OFC_RECORD)
				.leftOuterJoin(OFC_USER).on(OFC_RECORD.LOCKED_BY_ID.equal(OFC_USER.ID))
				.where(OFC_RECORD.ID.equal(recordId))
				.fetchOne();
		Integer lockedById = result.getValueAsInteger(OFC_RECORD.LOCKED_BY_ID);
		if (lockedById == null || lockedById.equals(user.getId())) {
			jf.update(OFC_RECORD)
			  .set(OFC_RECORD.LOCKED_BY_ID, user.getId())
			  .where(OFC_RECORD.ID.equal(recordId))
			  .execute();
		} else {
			String userName = result.getValueAsString(OFC_USER.USERNAME);
			throw new RecordLockedException("Record already locked", userName);
		}
	}

	@Transactional
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
		Record selectResult = jf
				.select(OFC_RECORD.LOCKED_BY_ID, OFC_USER.USERNAME)
				.from(OFC_RECORD)
				.leftOuterJoin(OFC_USER).on(OFC_RECORD.LOCKED_BY_ID.equal(OFC_USER.ID))
				.where(OFC_RECORD.ID.equal(recordId))
				.fetchOne();
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
		jf.update(OFC_RECORD)
		  .set(OFC_RECORD.LOCKED_BY_ID, (Integer) null)
		  .execute();
	}
	
	
	/**
	 * Load a list of record summaries that match the primary keys
	 * 
	 * @param survey
	 * @param rootEntity
	 * @param keys
	 * @return
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		JooqFactory jf = getMappingJooqFactory(survey);
		SelectQuery q = jf.selectQuery();
		q.addSelect(SUMMARY_FIELDS);
		q.addFrom(OFC_RECORD);
		// build conditions
		int i = 0;
		for (String key : keys) {
			String keyColumnName = "key" + (++i);
			Field<String> keyField = (Field<String>) OFC_RECORD.getField(keyColumnName);
			q.addConditions(keyField.equal(key));
		}
		// fetch
		Result<Record> result = q.fetch();
		return jf.fromResult(result);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxRecords, String orderByField, String filter) {
		JooqFactory jf = getMappingJooqFactory(survey);
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_RECORD);
		q.addSelect(SUMMARY_FIELDS);

		//add order by condition
		Field<?> orderBy = null;
		if(orderByField != null) {
			if(ORDER_BY_DATE_CREATED.equals(orderByField)) {
				orderBy = OFC_RECORD.DATE_CREATED;
			} else if(ORDER_BY_DATE_MODIFIED.equals(orderByField)) {
				orderBy = OFC_RECORD.DATE_MODIFIED;
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
		q.addOrderBy(OFC_RECORD.ID);
		
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
	}

	@Override
	public void update(CollectRecord record) {
		super.update(record);
	}

	@Override
	public void insert(CollectRecord record) {
		super.insert(record);
	}
	
	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public static class JooqFactory extends MappingJooqFactory<CollectRecord> {

		private static final long serialVersionUID = 1L;
		private static final int SERIALIZATION_BUFFER_SIZE = 50000;
		private CollectSurvey survey;
		private Field<byte[]> dataAlias;
		
		public JooqFactory(Connection conn) {
			super(conn, OFC_RECORD.ID, OFC_RECORD_ID_SEQ, CollectRecord.class);
		}

		public JooqFactory(Connection conn, CollectSurvey survey, int step) {
			this(conn, survey);
			if ( step < 1 || step > 3 ) {
				throw new IllegalArgumentException("Invalid step "+step);
			}
			this.dataAlias = (step == 1 ? OFC_RECORD.DATA1 : OFC_RECORD.DATA2).as("DATA");
		}

		public JooqFactory(Connection conn, CollectSurvey survey) {
			this(conn);
			this.survey = survey;
		}

		public SelectQuery selectRecordQuery(int id) {
			SelectQuery query = selectQuery();
			query.addSelect(SUMMARY_FIELDS);
			query.addSelect(dataAlias);
			query.addFrom(OFC_RECORD);
			query.addConditions(OFC_RECORD.ID.equal(id));
			return query;
		}

		@Override
		protected void setId(CollectRecord record, int id) {
			record.setId(id);
		}

		@Override
		protected Integer getId(CollectRecord record) {
			return record.getId();
		}

		@Override
		public CollectRecord fromRecord(Record r) {
			int rootEntityId = r.getValueAsInteger(OFC_RECORD.ROOT_ENTITY_ID);
			String version = r.getValueAsString(OFC_RECORD.MODEL_VERSION);
			Schema schema = survey.getSchema();
			NodeDefinition rootEntityDefn = schema.getById(rootEntityId);
			if (rootEntityDefn == null) {
				throw new DataInconsistencyException("Unknown root entity id " + rootEntityId);
			}
			CollectRecord record = new CollectRecord(survey, version);
			fromRecord(r, record);
			return record;
		}
		
		@Override
		protected void fromRecord(Record r, CollectRecord c) {
			c.setId(r.getValue(OFC_RECORD.ID));
			c.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
			// record.setCreatedBy(r.getValueAsString(OFC_RECORD.CREATED_BY));
			c.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
			// record.setModifiedBy(r.getValueAsString(OFC_RECORD.MODIFIED_BY));

			c.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
			c.setErrors(r.getValue(OFC_RECORD.ERRORS));
			c.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
			c.setMissing(r.getValue(OFC_RECORD.MISSING));

			Integer step = r.getValue(OFC_RECORD.STEP);
			if (step != null) {
				c.setStep(Step.valueOf(step));
			}

			// create list of entity counts
			// TODO
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
			c.setEntityCounts(counts);

			// create list of keys
			List<String> keys = new ArrayList<String>();
			String key;
			key = r.getValue(OFC_RECORD.KEY1);
			keys.add(key);
			key = r.getValue(OFC_RECORD.KEY2);
			keys.add(key);
			key = r.getValue(OFC_RECORD.KEY3);
			keys.add(key);
			c.setKeys(keys);

			int rootEntityId = r.getValue(OFC_RECORD.ROOT_ENTITY_ID);

			if ( dataAlias != null ) {
				byte[] data = r.getValue(dataAlias);
				Entity rootEntity = c.createRootEntity(rootEntityId);
				ModelSerializer modelSerializer = getSerializer();
				modelSerializer.mergeFrom(data, rootEntity);
			}
		}

		@SuppressWarnings({"unchecked"})
		@Override
		protected void toRecord(CollectRecord record, UpdatableRecord<?> r) {
			int id = record.getId();
			r.setValue(OFC_RECORD.ID, id);
			Entity rootEntity = record.getRootEntity();
			EntityDefinition rootEntityDefinition = rootEntity.getDefinition();
			Integer rootEntityId = rootEntityDefinition.getId();
			if (rootEntityId == null) {
				throw new IllegalArgumentException("Null schema object definition id");
			}
			r.setValue(OFC_RECORD.ROOT_ENTITY_ID, rootEntityId);
			r.setValue(OFC_RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()));
			if (record.getCreatedBy() != null) {
				r.setValue(OFC_RECORD.CREATED_BY_ID, record.getCreatedBy().getId());
			}
			r.setValue(OFC_RECORD.DATE_MODIFIED,
					toTimestamp(record.getModifiedDate()));
			if (record.getModifiedBy() != null) {
				r.setValue(OFC_RECORD.MODIFIED_BY_ID, record.getModifiedBy().getId());
			}
			r.setValue(OFC_RECORD.MODEL_VERSION, record.getVersion().getName());
			r.setValue(OFC_RECORD.STEP, record.getStep().getStepNumber());
			r.setValue(OFC_RECORD.SKIPPED, record.getSkipped());
			r.setValue(OFC_RECORD.MISSING, record.getMissing());
			r.setValue(OFC_RECORD.ERRORS, record.getErrors());
			r.setValue(OFC_RECORD.WARNINGS, record.getWarnings());

			// set keys
			List<String> keys = record.getRootEntityKeys();
			for (int i = 0; i < keys.size(); i++) {
				r.setValue(KEY_FIELDS[i], keys.get(i));
			}

			// set counts
			List<Integer> counts = record.getEntityCounts();
			for (int i = 0; i < counts.size(); i++) {
				r.setValue(COUNT_FIELDS[i], counts.get(i));
			}
			// store data
			ModelSerializer modelSerializer = getSerializer();
			byte[] data = modelSerializer.toByteArray(rootEntity);
			if ( record.getStep() == Step.ENTRY ) {
				r.setValue(OFC_RECORD.DATA1, data);
			} else {
				r.setValue(OFC_RECORD.DATA2, data);
			}
		}

		private ModelSerializer getSerializer() {
			return new ModelSerializer(SERIALIZATION_BUFFER_SIZE);
		}
	}
}
