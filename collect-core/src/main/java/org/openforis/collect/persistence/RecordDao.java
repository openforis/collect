package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao.JooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@SuppressWarnings("rawtypes")
@Transactional
public class RecordDao extends MappingJooqDaoSupport<CollectRecord, JooqFactory> {
	
	private static final TableField[] KEY_FIELDS = 
		{OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3};
	private static final TableField[] COUNT_FIELDS = 
		{OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};
	private static final TableField[] SUMMARY_FIELDS = 
		{OFC_RECORD.DATE_CREATED, OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.ERRORS, OFC_RECORD.ID, 
	     OFC_RECORD.MISSING, OFC_RECORD.MODEL_VERSION, OFC_RECORD.MODIFIED_BY_ID, OFC_RECORD.OWNER_ID, 
	     OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.SKIPPED, OFC_RECORD.STATE, OFC_RECORD.STEP, OFC_RECORD.SURVEY_ID, 
	     OFC_RECORD.WARNINGS, OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3, 
	     OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};

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
	
	public byte[] loadBinaryData(CollectSurvey survey, int id, int step) {
		JooqFactory jf = getMappingJooqFactory(survey, step);
		SelectQuery query = jf.selectRecordQuery(id);
		Record r = query.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			byte[] result = r.getValue(jf.dataAlias);
			return result;
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

	@Transactional
	public boolean hasAssociatedRecords(int userId) {
		JooqFactory f = getMappingJooqFactory();
		SelectQuery q = f.selectCountQuery();
		q.addConditions(OFC_RECORD.CREATED_BY_ID.equal(userId)
				.or(OFC_RECORD.MODIFIED_BY_ID.equal(userId)));
		Record r = q.fetchOne();
		Integer count = (Integer) r.getValue(0);
		return count > 0;
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity) {
		return loadSummaries(survey, rootEntity, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step) {
		return loadSummaries(survey, rootEntity, step, (Date) null, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keyValues) {
		return loadSummaries(survey, rootEntity, (Step) null, (Date) null, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, keyValues);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxRecords, 
			List<RecordSummarySortField> sortFields, String... keyValues) {
		return loadSummaries(survey, rootEntity, (Step) null, (Date) null, offset, maxRecords, sortFields, keyValues);
	}
	
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Date modifiedSince) {
		return loadSummaries(survey, rootEntity, (Step) null, modifiedSince, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step, Date modifiedSince, int offset, int maxRecords, 
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
	
	@Transactional
	public List<CollectRecord> loadSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		CollectSurvey survey = filter.getSurvey();
		
		JooqFactory jf = getMappingJooqFactory(survey);
		SelectQuery q = jf.selectQuery();	
		
		q.addSelect(SUMMARY_FIELDS);
		Field<String> ownerNameField = OFC_USER.USERNAME.as(RecordSummarySortField.Sortable.OWNER_NAME.name());
		q.addSelect(ownerNameField);
		q.addFrom(OFC_RECORD);
		//join with user table to get owner name
		q.addJoin(OFC_USER, JoinType.LEFT_OUTER_JOIN, OFC_RECORD.OWNER_ID.equal(OFC_USER.ID));

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
			addFilterByKeyConditions(q, filter.getKeyValues().toArray(new String[0]));
		}

		//add ordering fields
		if ( sortFields != null ) {
			for (RecordSummarySortField sortField : sortFields) {
				addOrderBy(q, sortField, ownerNameField);
			}
		}
		
		//always order by ID to avoid pagination issues
		q.addOrderBy(OFC_RECORD.ID);
		
		//add limit
		q.addLimit(filter.getOffset(), filter.getMaxNumberOfRecords());
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
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
		JooqFactory jf = getMappingJooqFactory();
		SelectQuery q = jf.selectCountQuery();
		
		q.addConditions(OFC_RECORD.SURVEY_ID.equal(filter.getSurveyId()));
		
		if ( filter.getRootEntityId() != null ) {
			q.addConditions(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID.eq(filter.getRootEntityId()));
		}
		if ( filter.getStepGreaterOrEqual() != null ) {
			q.addConditions(OFC_RECORD.STEP.ge(filter.getStepGreaterOrEqual().getStepNumber()));
		}
		if ( CollectionUtils.isNotEmpty( filter.getKeyValues() ) ) {
			addFilterByKeyConditions(q, filter.getKeyValues().toArray(new String[0]));
		}
		Record record = q.fetchOne();
		int result = record.getValue(Factory.count());
		return result;
	}

	@Transactional
	public int countRecords(CollectSurvey survey, int rootDefinitionId, String... keyValues) {
		RecordFilter filter = new RecordFilter(survey, rootDefinitionId);
		filter.setKeyValues(keyValues);
		return countRecords(filter);
	}
	
	private void addFilterByKeyConditions(SelectQuery q, String... keyValues) {
		if ( keyValues != null && keyValues.length > 0 ) {
			for (int i = 0; i < keyValues.length && i < KEY_FIELDS.length; i++) {
				String key = keyValues[i];
				if(StringUtils.isNotBlank(key)) {
					@SuppressWarnings("unchecked")
					Field<String> keyField = (Field<String>) KEY_FIELDS[i];
					q.addConditions(keyField.upper().equal(key.toUpperCase()));
				}
			}
		}
	}

	private void addOrderBy(SelectQuery q, RecordSummarySortField sortField, Field<String> ownerNameField) {
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
				q.addOrderBy(orderBy);
			}
		}
	}

	@Override
	public void update(CollectRecord record) {
		Survey survey = record.getSurvey();
		JooqFactory jf = getMappingJooqFactory((CollectSurvey) survey);
		jf.updateQuery(record).execute();
	}

	@Override
	public void insert(CollectRecord record) {
		Survey survey = record.getSurvey();
		JooqFactory jf = getMappingJooqFactory((CollectSurvey) survey);
		jf.insertQuery(record).execute();
	}
	
	@Override
	public void delete(int id) {
		super.delete(id);
	}

	@Transactional
	public void assignOwner(int recordId, Integer ownerId) {
		JooqFactory jf = getMappingJooqFactory();
		jf.update(OFC_RECORD)
			.set(OFC_RECORD.OWNER_ID, ownerId)
			.where(OFC_RECORD.ID.eq(recordId))
			.execute();
	}

	public void deleteBySurvey(int id) {
		JooqFactory jf = getMappingJooqFactory();
		jf.delete(OFC_RECORD)
			.where(OFC_RECORD.SURVEY_ID.equal(id))
			.execute();
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
			int rootEntityId = r.getValueAsInteger(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
			String version = r.getValueAsString(OFC_RECORD.MODEL_VERSION);
			Schema schema = survey.getSchema();
			NodeDefinition rootEntityDefn = schema.getDefinitionById(rootEntityId);
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
			c.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
			
			c.setCreatedBy(loadUser(r.getValue(OFC_RECORD.CREATED_BY_ID)));
			c.setModifiedBy(loadUser(r.getValue(OFC_RECORD.MODIFIED_BY_ID)));
			c.setOwner(loadUser(r.getValue(OFC_RECORD.OWNER_ID)));
			c.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
			c.setErrors(r.getValue(OFC_RECORD.ERRORS));
			c.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
			c.setMissing(r.getValue(OFC_RECORD.MISSING));

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
				counts.add(r.getValueAsInteger(tableField));
			}
			c.setEntityCounts(counts);

			// create list of keys
			List<String> keys = new ArrayList<String>(KEY_FIELDS.length);
			for (TableField tableField : KEY_FIELDS) {
				keys.add(r.getValueAsString(tableField));
			}
			c.setRootEntityKeyValues(keys);

			int rootEntityId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);

			if ( dataAlias != null ) {
				byte[] data = r.getValue(dataAlias);
				//System.out.println("r.getValue(dataAlias) = " + r.getValue(dataAlias));
				Entity rootEntity = c.createRootEntity(rootEntityId);
				ModelSerializer modelSerializer = getSerializer();
				modelSerializer.mergeFrom(data, rootEntity);
			}
		}

		private User loadUser(Integer userId) {
			if ( userId == null ) {
				return null;
			}
			SimpleSelectQuery<OfcUserRecord> userSelect = selectQuery(OFC_USER);
			userSelect.addConditions(OFC_USER.ID.equal(userId));
			OfcUserRecord userRecord = userSelect.fetchOne();
			User user = new User();
			user.setId(userRecord.getId());
			user.setName(userRecord.getUsername());
			user.setPassword(userRecord.getPassword());
			return user;
		}

		@SuppressWarnings({"unchecked"})
		@Override
		protected void fromObject(CollectRecord record, StoreQuery<?> q) {
			int id = record.getId();
			q.addValue(OFC_RECORD.ID, id);
			Entity rootEntity = record.getRootEntity();
			EntityDefinition rootEntityDefn = rootEntity.getDefinition();
			Integer rootEntityDefnId = rootEntityDefn.getId();
			if (rootEntityDefnId == null) {
				throw new IllegalArgumentException("Null schema object definition id");
			}
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
			ModelSerializer modelSerializer = getSerializer();
			switch (record.getStep()) {
			case ENTRY:
				data = modelSerializer.toByteArray(rootEntity);
				q.addValue(OFC_RECORD.DATA1, data);
				break;
			case CLEANSING:
				data = modelSerializer.toByteArray(rootEntity);
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
			return new ModelSerializer(SERIALIZATION_BUFFER_SIZE);
		}
	}

}
