package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Batch;
import org.jooq.Configuration;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao.RecordDSLContext;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@SuppressWarnings("rawtypes")
public class RecordDao extends MappingJooqDaoSupport<CollectRecord, RecordDSLContext> {
	
	private static final TableField[] KEY_FIELDS = 
		{OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3};
	private static final TableField[] COUNT_FIELDS = 
		{OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};
	private static final TableField[] SUMMARY_FIELDS = 
		{OFC_RECORD.DATE_CREATED, OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.ERRORS, OFC_RECORD.ID, 
	     OFC_RECORD.MISSING, OFC_RECORD.MODEL_VERSION, OFC_RECORD.MODIFIED_BY_ID, OFC_RECORD.OWNER_ID, 
	     OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.SKIPPED, OFC_RECORD.STATE, OFC_RECORD.STEP, OFC_RECORD.SURVEY_ID, 
	     OFC_RECORD.WARNINGS, OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3, 
	     OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5,
	     OFC_RECORD.APP_VERSION};

	public RecordDao() {
		super(RecordDSLContext.class);
	}
	
	public CollectRecord load(CollectSurvey survey, int id, int step) {
		return load(survey, id, step, true);
	}
	
	public CollectRecord load(CollectSurvey survey, int id, int step, boolean toBeUpdated) {
		RecordDSLContext jf = createDSLContext(survey, step, toBeUpdated);
		SelectQuery query = jf.selectRecordQuery(id);
		Record r = query.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}
	
	public byte[] loadBinaryData(CollectSurvey survey, int id, int step) {
		RecordDSLContext jf = createDSLContext(survey, step);
		SelectQuery query = jf.selectRecordQuery(id);
		Record r = query.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			byte[] result = r.getValue(jf.dataAlias);
			return result;
		}
	}

	private RecordDSLContext createDSLContext(CollectSurvey survey) {
		return new RecordDSLContext(getConfiguration(), survey);
	}

	private RecordDSLContext createDSLContext(CollectSurvey survey, int step) {
		return createDSLContext(survey, step, true);
	}

	private RecordDSLContext createDSLContext(CollectSurvey survey, int step, boolean recordToBeUpdated) {
		return new RecordDSLContext(getConfiguration(), survey, step, recordToBeUpdated);
	}
	
	@Deprecated
	public void saveOrUpdate(CollectRecord record) {
		if ( record.getId() == null ) {
			insert(record);
		} else {
			update(record);
		}
	}

	public int loadSurveyId(int recordId) {
		OfcRecordRecord record = dsl().selectFrom(OFC_RECORD).where(OFC_RECORD.ID.eq(recordId)).fetchAny();
		return record.getSurveyId();
	}

	public boolean hasAssociatedRecords(int userId) {
		SelectQuery q = dsl().selectCountQuery();
		q.addConditions(OFC_RECORD.CREATED_BY_ID.equal(userId)
				.or(OFC_RECORD.MODIFIED_BY_ID.equal(userId)));
		Record r = q.fetchOne();
		Integer count = (Integer) r.getValue(0);
		return count > 0;
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity) {
		return loadSummaries(survey, rootEntity, (String[]) null);
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step) {
		return loadSummaries(survey, rootEntity, step, null, null, null, null);
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keyValues) {
		return loadSummaries(survey, rootEntity, true, keyValues);
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, boolean caseSensitiveKeys, String... keyValues) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntity);
		
		RecordFilter filter = new RecordFilter(survey, rootEntityDefn.getId());
		filter.setCaseSensitiveKeyValues(caseSensitiveKeys);
		filter.setKeyValues(keyValues);
		return loadSummaries(filter, null);
	}
	
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxRecords, 
			List<RecordSummarySortField> sortFields, String... keyValues) {
		return loadSummaries(survey, rootEntity, (Step) null, (Date) null, offset, maxRecords, sortFields, keyValues);
	}
	
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Date modifiedSince) {
		return loadSummaries(survey, rootEntity, (Step) null, modifiedSince, (Integer) null, (Integer) null, (List<RecordSummarySortField>) null, (String[]) null);
	}

	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step, Date modifiedSince, Integer offset, Integer maxRecords, 
			List<RecordSummarySortField> sortFields, String... keyValues) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntity);
		
		RecordFilter filter = new RecordFilter(survey, rootEntityDefn.getId());
		filter.setStep(step);
		filter.setModifiedSince(modifiedSince);
		filter.setOffset(offset);
		filter.setMaxNumberOfRecords(maxRecords);
		filter.setKeyValues(keyValues);
		return loadSummaries(filter, sortFields);
	}
	
	public List<CollectRecord> loadSummaries(RecordFilter filter) {
		return loadSummaries(filter, null);
	}
	
	public List<CollectRecord> loadSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		CollectSurvey survey = filter.getSurvey();
		
		RecordDSLContext jf = createDSLContext(survey);
		SelectQuery<Record> q = createQuery(jf, filter, sortFields);

		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
	}

	public void visitSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields, 
			Visitor<CollectRecord> visitor) {
		CollectSurvey survey = filter.getSurvey();
		
		RecordDSLContext jf = createDSLContext(survey);
		SelectQuery<Record> q = createQuery(jf, filter, sortFields);

		Cursor<Record> cursor = null;
		try {
			cursor = q.fetchLazy();
			while (cursor.hasNext()) {
				Record r = cursor.fetchOne();
				CollectRecord record = jf.fromRecord(r);
				visitor.visit(record);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	private SelectQuery<Record> createQuery(DSLContext jf, RecordFilter filter,
			List<RecordSummarySortField> sortFields) {
		SelectQuery<Record> q = jf.selectQuery();	
		
		q.addSelect(SUMMARY_FIELDS);
		Field<String> ownerNameField = OFC_USER.USERNAME.as(RecordSummarySortField.Sortable.OWNER_NAME.name());
		q.addSelect(ownerNameField);
		q.addFrom(OFC_RECORD);
		//join with user table to get owner name
		q.addJoin(OFC_USER, JoinType.LEFT_OUTER_JOIN, OFC_RECORD.OWNER_ID.equal(OFC_USER.ID));

		addFilterConditions(q, filter);
		
		//add limit
		if (filter.getOffset() != null && filter.getMaxNumberOfRecords() != null) {
			q.addLimit(filter.getOffset(), filter.getMaxNumberOfRecords());
		}
		
		//add ordering fields
		if ( sortFields != null ) {
			for (RecordSummarySortField sortField : sortFields) {
				addOrderBy(q, sortField, ownerNameField);
			}
		}
		//always order by ID to avoid pagination issues
		q.addOrderBy(OFC_RECORD.ID);
		return q;
	}
	
	private void addFilterConditions(SelectQuery<?> q, RecordFilter filter) {
		CollectSurvey survey = filter.getSurvey();
		//survey
		q.addConditions(OFC_RECORD.SURVEY_ID.equal(survey.getId()));
		
		//root entity
		if ( filter.getRootEntityId() != null ) {
			q.addConditions(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID.equal(filter.getRootEntityId()));
		}
		
		//root entity
		if ( filter.getRecordId() != null ) {
			q.addConditions(OFC_RECORD.ID.equal(filter.getRecordId()));
		}
		
		//step
		if ( filter.getStep() != null ) {
			q.addConditions(OFC_RECORD.STEP.equal(filter.getStep().getStepNumber()));
		}
		if ( filter.getStepGreaterOrEqual() != null ) {
			q.addConditions(OFC_RECORD.STEP.greaterOrEqual(filter.getStepGreaterOrEqual().getStepNumber()));
		}
		
		//modified since
		if ( filter.getModifiedSince() != null ) {
			q.addConditions(OFC_RECORD.DATE_MODIFIED.greaterOrEqual(new Timestamp(filter.getModifiedSince().getTime())));
		}
		//owner
		if ( filter.getOwnerId() != null ) {
			q.addConditions(OFC_RECORD.OWNER_ID.equal(filter.getOwnerId()));
		}
		//record keys
		if ( CollectionUtils.isNotEmpty( filter.getKeyValues() ) ) {
			addFilterByKeyConditions(q, filter.isCaseSensitiveKeyValues(), filter.isIncludeNullConditionsForKeyValues(), filter.getKeyValues());
		}
	}
	
	public int countRecords(CollectSurvey survey) {
		return countRecords(survey, null);
	}
	
	public int countRecords(CollectSurvey survey, Integer rootEntityDefinitionId) {
		return countRecords(survey, rootEntityDefinitionId, (Integer) null);
	}
	
	public int countRecords(CollectSurvey survey, Integer rootEntityDefinitionId, Integer dataStepNumber) {
		RecordFilter filter = new RecordFilter(survey, rootEntityDefinitionId);
		if ( dataStepNumber != null ) {
			filter.setStepGreaterOrEqual(Step.valueOf(dataStepNumber));
		}
		return countRecords(filter);
	}
	
	public int countRecords(RecordFilter filter) {
		RecordDSLContext dsl = dsl();
		SelectQuery q = dsl.selectCountQuery();
		addFilterConditions(q, filter);
		Record record = q.fetchOne();
		int result = record.getValue(DSL.count());
		return result;
	}

	public int countRecords(CollectSurvey survey, int rootDefinitionId, String... keyValues) {
		RecordFilter filter = new RecordFilter(survey, rootDefinitionId);
		filter.setKeyValues(keyValues);
		return countRecords(filter);
	}
	
	private void addFilterByKeyConditions(SelectQuery q, boolean caseSensitiveKeyValues, boolean includeNullConditions, List<String> keyValues) {
		addFilterByKeyConditions(q, caseSensitiveKeyValues, includeNullConditions, keyValues.toArray(new String[keyValues.size()]));
	}
	
	private void addFilterByKeyConditions(SelectQuery q, boolean caseSensitiveKeyValues, boolean includeNullConditions, String... keyValues) {
		if ( keyValues != null && keyValues.length > 0 ) {
			for (int i = 0; i < keyValues.length && i < KEY_FIELDS.length; i++) {
				String key = keyValues[i];
				@SuppressWarnings("unchecked")
				Field<String> keyField = (Field<String>) KEY_FIELDS[i];
				if (StringUtils.isNotBlank(key)) {
					if (caseSensitiveKeyValues) {
						q.addConditions(keyField.equal(key));
					} else {
						q.addConditions(keyField.upper().equal(key.toUpperCase()));
					}
				} else if (includeNullConditions) {
					q.addConditions(keyField.isNull());
				}
			}
		}
	}

	private void addOrderBy(SelectQuery<Record> q, RecordSummarySortField sortField, Field<String> ownerNameField) {
		Field<?> orderBy = null;
		if(sortField != null) {
			switch(sortField.getField()) {
			case KEY1:
				orderBy = OFC_RECORD.KEY1;
				break;
			case KEY2:
				orderBy = OFC_RECORD.KEY2;
				break;
			case KEY3:
				orderBy = OFC_RECORD.KEY3;
				break;
			case COUNT1:
				orderBy = OFC_RECORD.COUNT1;
				break;
			case COUNT2:
				orderBy = OFC_RECORD.COUNT2;
				break;
			case COUNT3:
				orderBy = OFC_RECORD.COUNT3;
				break;
			case DATE_CREATED:
				orderBy = OFC_RECORD.DATE_CREATED;
				break;
			case DATE_MODIFIED:
				orderBy = OFC_RECORD.DATE_MODIFIED;
				break;
			case SKIPPED:
				orderBy = OFC_RECORD.SKIPPED;
				break;
			case MISSING:
				orderBy = OFC_RECORD.MISSING;
				break;
			case ERRORS:
				orderBy = OFC_RECORD.ERRORS;
				break;
			case WARNINGS:
				orderBy = OFC_RECORD.WARNINGS;
				break;
			case STEP:
				orderBy = OFC_RECORD.STEP;
				break;
			case OWNER_NAME:
				orderBy = ownerNameField;
				break;
			}
		}
		if(orderBy != null) {
			if(sortField.isDescending()) {
				q.addOrderBy(orderBy.desc());
			} else {
				q.addOrderBy(orderBy.asc());
			}
		}
	}

	@Override
	public void update(CollectRecord record) {
		createUpdateQuery(record, record.getStep()).getInternalQuery().execute();
	}
	
	public RecordStoreQuery createUpdateQuery(CollectRecord record, Step step) {
		Survey survey = record.getSurvey();
		RecordDSLContext dsl = createDSLContext((CollectSurvey) survey, step.getStepNumber());
		UpdateQuery q = dsl.updateQuery(record, step);
		return new RecordStoreQuery(q);
	}

	@Override
	public void insert(CollectRecord record) {
		createInsertQuery(record).getInternalQuery().execute();
	}
	
	public RecordStoreQuery createInsertQuery(CollectRecord record) {
		Survey survey = record.getSurvey();
		RecordDSLContext dsl = createDSLContext((CollectSurvey) survey, Step.ENTRY.getStepNumber());
		InsertQuery q = dsl.insertQuery(record, Step.ENTRY);
		return new RecordStoreQuery(q);
	}
	
	public void execute(List<RecordStoreQuery> queries) {
		List<Query> internalQueries = new ArrayList<Query>(queries.size());
		for (RecordStoreQuery recordStoreQuery : queries) {
			internalQueries.add(recordStoreQuery.getInternalQuery());
		}
		Batch batch = dsl().batch(internalQueries);
		batch.execute();
	}
	
	public int nextId() {
		return dsl().nextId();
	}
	
	public void restartIdSequence(Number value) {
		dsl().restartSequence(value);
	}
	
	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public void assignOwner(int recordId, Integer ownerId) {
		dsl().update(OFC_RECORD)
			.set(OFC_RECORD.OWNER_ID, ownerId)
			.where(OFC_RECORD.ID.eq(recordId))
			.execute();
	}

	public void deleteBySurvey(int id) {
		dsl().delete(OFC_RECORD)
			.where(OFC_RECORD.SURVEY_ID.equal(id))
			.execute();
	}

	public static class RecordDSLContext extends MappingDSLContext<CollectRecord> {

		private static final String DATA_ALIAS_NAME = "DATA";
		private static final long serialVersionUID = 1L;
		private static final int SERIALIZATION_BUFFER_SIZE = 50000;
		private CollectSurvey survey;
		private boolean recordToBeUpdated;
		private Field<byte[]> dataAlias;
		private ModelSerializer modelSerializer;
		
		public RecordDSLContext(Configuration config) {
			this(config, null);
		}

		public RecordDSLContext(Configuration config, CollectSurvey survey) {
			this(config, survey, null);
		}
		
		public RecordDSLContext(Configuration config, CollectSurvey survey, Integer step) {
			this(config, survey, step, true);
		}
		
		public RecordDSLContext(Configuration config, CollectSurvey survey, Integer step, boolean recordToBeUpdated) {
			super(config, OFC_RECORD.ID, OFC_RECORD_ID_SEQ, CollectRecord.class);
			this.survey = survey;
			this.recordToBeUpdated = recordToBeUpdated;
			if ( step != null && (step < 1 || step > 3) ) {
				throw new IllegalArgumentException("Invalid step "+step);
			}
			this.dataAlias = step == null ? null: (step == 1 ? OFC_RECORD.DATA1 : OFC_RECORD.DATA2).as(DATA_ALIAS_NAME);
			this.modelSerializer = step == null ? null : new ModelSerializer(SERIALIZATION_BUFFER_SIZE);
		}

		public UpdateQuery updateQuery(CollectRecord record, Step step) {
			UpdateQuery<?> query = updateQuery(record);
			query.addValue(OFC_RECORD.STEP, step.getStepNumber());
			return query;
		}
		
		public InsertQuery insertQuery(CollectRecord record, Step step) {
			InsertQuery<?> query = insertQuery(record);
			query.addValue(OFC_RECORD.STEP, step.getStepNumber());
			return query;
		}

		public SelectQuery selectRecordQuery(int id) {
			SelectQuery<Record> query = selectQuery();
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
			int rootEntityId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
			String version = r.getValue(OFC_RECORD.MODEL_VERSION);
			Schema schema = survey.getSchema();
			NodeDefinition rootEntityDefn = schema.getDefinitionById(rootEntityId);
			if (rootEntityDefn == null) {
				throw new DataInconsistencyException("Unknown root entity id " + rootEntityId);
			}
			CollectRecord record = new CollectRecord(survey, version, null, recordToBeUpdated);
			fromRecord(r, record);
			return record;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void fromRecord(Record r, CollectRecord c) {
			c.setId(r.getValue(OFC_RECORD.ID));
			c.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
			c.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
			c.setCreatedBy(createDetachedUser(r.getValue(OFC_RECORD.CREATED_BY_ID)));
			c.setModifiedBy(createDetachedUser(r.getValue(OFC_RECORD.MODIFIED_BY_ID)));
			c.setOwner(createDetachedUser(r.getValue(OFC_RECORD.OWNER_ID)));
			c.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
			c.setErrors(r.getValue(OFC_RECORD.ERRORS));
			c.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
			c.setMissing(r.getValue(OFC_RECORD.MISSING));
			c.setApplicationVersion(new Version(r.getValue(OFC_RECORD.APP_VERSION)));

			Integer step = r.getValue(OFC_RECORD.STEP);
			if (step != null) {
				c.setStep(Step.valueOf(step));
			}
			String state = r.getValue(OFC_RECORD.STATE);
			if (state != null) {
				c.setState(State.fromCode(state));
			}
			
			// create list of entity counts
			List<Integer> counts = new ArrayList<Integer>(COUNT_FIELDS.length);
			for (TableField tableField : COUNT_FIELDS) {
				counts.add((Integer) r.getValue(tableField));
			}
			c.setEntityCounts(counts);

			int rootEntityDefId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
			
			if (dataAlias == null) {
				c.setRootEntityDefinitionId(rootEntityDefId);
			} else {
				byte[] data = r.getValue(dataAlias);
				ModelSerializer modelSerializer = getSerializer();
				Entity rootEntity = c.createRootEntity(rootEntityDefId);
				modelSerializer.mergeFrom(data, rootEntity);
			}

			// create list of keys
			EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinition(rootEntityDefId);
			List<AttributeDefinition> keyDefs = rootEntityDef.getKeyAttributeDefinitions();
			List<String> keys = new ArrayList<String>(keyDefs.size());
			for (int i = 0; i < keyDefs.size(); i++) {
				TableField tableField = KEY_FIELDS[i];
				keys.add((String) r.getValue(tableField));
			}
			c.setRootEntityKeyValues(keys);
		}

		private User createDetachedUser(Integer userId) {
			if ( userId == null ) {
				return null;
			}
			User user = new User();
			user.setId(userId);
			return user;
		}

		@SuppressWarnings({"unchecked"})
		@Override
		protected void fromObject(CollectRecord record, StoreQuery<?> q) {
			int id = record.getId();
			q.addValue(OFC_RECORD.ID, id);
			Entity rootEntity = record.getRootEntity();
			EntityDefinition rootEntityDefn = rootEntity.getDefinition();
			int rootEntityDefnId = rootEntityDefn.getId();
			q.addValue(OFC_RECORD.SURVEY_ID, survey.getId());
			q.addValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, rootEntityDefnId);
			q.addValue(OFC_RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()));
			if (record.getCreatedBy() != null) {
				q.addValue(OFC_RECORD.CREATED_BY_ID, record.getCreatedBy().getId());
			}
			q.addValue(OFC_RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()));
			if (record.getModifiedBy() != null) {
				q.addValue(OFC_RECORD.MODIFIED_BY_ID, record.getModifiedBy().getId());
			}
			Integer ownerId = record.getOwner() == null ? null: record.getOwner().getId();
			q.addValue(OFC_RECORD.OWNER_ID, ownerId);

			ModelVersion version = record.getVersion();
			String versionName = version != null ? version.getName(): null;
			q.addValue(OFC_RECORD.MODEL_VERSION, versionName);
			q.addValue(OFC_RECORD.STEP, record.getStep().getStepNumber());
			q.addValue(OFC_RECORD.STATE, record.getState() != null ? record.getState().getCode(): null);
			q.addValue(OFC_RECORD.SKIPPED, record.getSkipped());
			q.addValue(OFC_RECORD.MISSING, record.getMissing());
			q.addValue(OFC_RECORD.ERRORS, record.getErrors());
			q.addValue(OFC_RECORD.WARNINGS, record.getWarnings());
			q.addValue(OFC_RECORD.APP_VERSION, record.getApplicationVersion().toString());

			// set keys
			List<String> keys = record.getRootEntityKeyValues();
			for (int i = 0; i < keys.size(); i++) {
				q.addValue(KEY_FIELDS[i], keys.get(i));
			}

			// set counts
			List<Integer> counts = record.getEntityCounts();
			for (int i = 0; i < counts.size(); i++) {
				q.addValue(COUNT_FIELDS[i], counts.get(i));
			}
			
			// store data
			byte[] data;
			switch (record.getStep()) {
			case ENTRY:
				data = getSerializer().toByteArray(rootEntity);
				q.addValue(OFC_RECORD.DATA1, data);
				break;
			case CLEANSING:
				data = getSerializer().toByteArray(rootEntity);
				q.addValue(OFC_RECORD.DATA2, data);
				break;
			case ANALYSIS:
				// no-op; do not overwrite data
				break;
			default:
				break;
			}
		}

		private ModelSerializer getSerializer() {
			return modelSerializer;
		}
		
		@Override
		public int nextId() {
			return super.nextId();
		}
		
		@Override
		public void restartSequence(Number value) {
			super.restartSequence(value);
		}
		
	}

	public static class RecordStoreQuery {
		
		private Query internalQuery;

		public RecordStoreQuery(Query internalQuery) {
			super();
			this.internalQuery = internalQuery;
		}
		
		public Query getInternalQuery() {
			return internalQuery;
		}
		
	}

}
