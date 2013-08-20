package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
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
	     OFC_RECORD.MISSING, OFC_RECORD.MODEL_VERSION, OFC_RECORD.MODIFIED_BY_ID, 
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
	public int countRecords(int surveyId, int rootDefinitionId, String... keyValues) {
		JooqFactory f = getMappingJooqFactory();
		SelectQuery q = f.selectCountQuery();
		q.addConditions(OFC_RECORD.SURVEY_ID.equal(surveyId)
				.and(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID.equal(rootDefinitionId)));
		addFilterByKeyConditions(q, keyValues);
		Record r = q.fetchOne();
		return (Integer) r.getValue(0);
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
		return loadSummaries(survey, rootEntity, step, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, (String[]) null);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, String... keyValues) {
		return loadSummaries(survey, rootEntity, (Step) null, 0, Integer.MAX_VALUE, (List<RecordSummarySortField>) null, keyValues);
	}

	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, int offset, int maxRecords, 
			List<RecordSummarySortField> sortFields, String... keyValues) {
		return loadSummaries(survey, rootEntity, (Step) null, offset, maxRecords, sortFields, keyValues);
	}
	
	@Transactional
	public List<CollectRecord> loadSummaries(CollectSurvey survey, String rootEntity, Step step, int offset, int maxRecords, 
			List<RecordSummarySortField> sortFields, String... keyValues) {
		JooqFactory jf = getMappingJooqFactory(survey);
		SelectQuery q = jf.selectQuery();	
		q.addFrom(OFC_RECORD);
		q.addSelect(SUMMARY_FIELDS);

		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getRootEntityDefinition(rootEntity);
		Integer rootEntityDefnId = rootEntityDefn.getId();
		q.addConditions(OFC_RECORD.SURVEY_ID.equal(survey.getId()));
		q.addConditions(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID.equal(rootEntityDefnId));
		if ( step != null ) {
			q.addConditions(OFC_RECORD.STEP.equal(step.getStepNumber()));
		}
		addFilterByKeyConditions(q, keyValues);
		
		if ( sortFields != null ) {
			for (RecordSummarySortField sortField : sortFields) {
				addOrderBy(q, sortField);
			}
		}
		
		//always order by ID to avoid pagination issues
		q.addOrderBy(OFC_RECORD.ID);
		
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> result = q.fetch();
		
		return jf.fromResult(result);
	}

	private void addFilterByKeyConditions(SelectQuery q, String... keyValues) {
		if ( keyValues != null ) {
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

	private void addOrderBy(SelectQuery q, RecordSummarySortField sortField) {
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
			
			Integer createdById = r.getValue(OFC_RECORD.CREATED_BY_ID);
			if(createdById !=null){
				User user = loadUser(createdById);
				c.setCreatedBy(user);
			}
			Integer modifiedById = r.getValue(OFC_RECORD.MODIFIED_BY_ID);
			if(modifiedById !=null){
				User user = loadUser(modifiedById);
				c.setModifiedBy(user);
			}
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

		private User loadUser(int userId) {
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
