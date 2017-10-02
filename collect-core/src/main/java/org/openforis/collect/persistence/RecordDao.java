package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.Tables.OFC_RECORD_DATA;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Batch;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecordSummary.StepSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordDataRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.ModelSerializer;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
@SuppressWarnings("rawtypes")
public class RecordDao extends JooqDaoSupport {
	
	private static final TableField[] RECORD_KEY_FIELDS = 
		{OFC_RECORD.KEY1, OFC_RECORD.KEY2, OFC_RECORD.KEY3};
	
	private static final TableField[] RECORD_COUNT_FIELDS = 
		{OFC_RECORD.COUNT1, OFC_RECORD.COUNT2, OFC_RECORD.COUNT3, OFC_RECORD.COUNT4, OFC_RECORD.COUNT5};
	
	private static final TableField[] RECORD_SUMMARY_FIELDS = 
		{OFC_RECORD.SUMMARY1, OFC_RECORD.SUMMARY2, OFC_RECORD.SUMMARY3};
	
	private static final TableField[] RECORD_QUALIFIER_FIELDS = 
		{OFC_RECORD.QUALIFIER1, OFC_RECORD.QUALIFIER2, OFC_RECORD.QUALIFIER3};
	
	private static final TableField[] RECORD_DATA_KEY_FIELDS = 
		{OFC_RECORD_DATA.KEY1, OFC_RECORD_DATA.KEY2, OFC_RECORD_DATA.KEY3};
	
	private static final TableField[] RECORD_DATA_COUNT_FIELDS = 
		{OFC_RECORD_DATA.COUNT1, OFC_RECORD_DATA.COUNT2, OFC_RECORD_DATA.COUNT3, OFC_RECORD_DATA.COUNT4, OFC_RECORD_DATA.COUNT5};
	
	private static final TableField[] RECORD_DATA_SUMMARY_FIELDS = 
		{OFC_RECORD_DATA.SUMMARY1, OFC_RECORD_DATA.SUMMARY2, OFC_RECORD_DATA.SUMMARY3};
	
	private static final TableField[] RECORD_DATA_QUALIFIER_FIELDS = 
		{OFC_RECORD_DATA.QUALIFIER1, OFC_RECORD_DATA.QUALIFIER2, OFC_RECORD_DATA.QUALIFIER3};
	
	private static final TableField[] SUMMARY_FIELDS = ArrayUtils.addAll(
			new TableField[]{OFC_RECORD.ID, OFC_RECORD.SURVEY_ID, OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.MODEL_VERSION,
				OFC_RECORD.DATE_CREATED, OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.MODIFIED_BY_ID, 
				OFC_RECORD.STEP, OFC_RECORD.DATA_SEQ_NUM, OFC_RECORD.STATE, 
				OFC_RECORD.ERRORS, OFC_RECORD.WARNINGS, OFC_RECORD.MISSING, OFC_RECORD.SKIPPED, 
				OFC_RECORD.OWNER_ID},
			ArrayUtils.addAll(
					ArrayUtils.addAll(
							ArrayUtils.addAll(RECORD_KEY_FIELDS, RECORD_COUNT_FIELDS), 
							RECORD_SUMMARY_FIELDS), 
					RECORD_QUALIFIER_FIELDS)
			);

	private static final TableField[] RECORD_DATA_FULL_SUMMARY_FIELDS = ArrayUtils.addAll(
			new TableField[]{OFC_RECORD.SURVEY_ID, OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.ID,
				OFC_RECORD.MODEL_VERSION, OFC_RECORD.OWNER_ID, OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_CREATED, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.MODIFIED_BY_ID,
				OFC_RECORD_DATA.RECORD_ID, OFC_RECORD_DATA.DATE_CREATED, OFC_RECORD_DATA.CREATED_BY, OFC_RECORD_DATA.DATE_MODIFIED, OFC_RECORD_DATA.MODIFIED_BY, 
				OFC_RECORD_DATA.STEP, OFC_RECORD_DATA.SEQ_NUM, OFC_RECORD_DATA.STATE, OFC_RECORD_DATA.ERRORS,
				OFC_RECORD_DATA.WARNINGS, OFC_RECORD_DATA.MISSING, OFC_RECORD_DATA.SKIPPED},
			ArrayUtils.addAll(
					ArrayUtils.addAll(
							ArrayUtils.addAll(RECORD_DATA_KEY_FIELDS, RECORD_DATA_COUNT_FIELDS), 
							RECORD_DATA_SUMMARY_FIELDS), 
					RECORD_DATA_QUALIFIER_FIELDS)
		);
	
	private static final int SERIALIZATION_BUFFER_SIZE = 50000;
	
	public CollectRecord load(CollectSurvey survey, int id, Step step) {
		return load(survey, id, step, true);
	}
	
	public CollectRecord load(CollectSurvey survey, int id, int workflowSequenceNumber, boolean toBeUpdated) {
		SelectQuery query = selectRecordQuery(id, false, null, workflowSequenceNumber);
		Record r = query.fetchOne();
		return r == null ? null : fromQueryResult(survey, r, toBeUpdated);
	}
	
	public CollectRecord load(CollectSurvey survey, int id, Step step, boolean toBeUpdated) {
		SelectQuery query = selectRecordQuery(id, false, step, null);
		Record r = query.fetchOne();
		return r == null ? null : fromQueryResult(survey, r, toBeUpdated);
	}
	
	public byte[] loadBinaryData(CollectSurvey survey, int id, Step step) {
		SelectQuery query = selectRecordQuery(id, false, step, null);
		Record r = query.fetchOne();
		return r == null ? null : r.getValue(OFC_RECORD_DATA.DATA);
	}

	public int loadSurveyId(int recordId) {
		OfcRecordRecord record = dsl().selectFrom(OFC_RECORD).where(OFC_RECORD.ID.eq(recordId)).fetchOne();
		return record.getSurveyId();
	}

	public boolean hasAssociatedRecords(int userId) {
		int count = dsl().fetchCount(dsl().select(OFC_RECORD.ID)
			.from(OFC_RECORD)
			.where(OFC_RECORD.OWNER_ID.eq(userId)
				.or(DSL.exists(
						dsl().select(OFC_RECORD_DATA.RECORD_ID)
						.from(OFC_RECORD_DATA)
						.where(OFC_RECORD_DATA.RECORD_ID.eq(OFC_RECORD.ID)
								.and(OFC_RECORD_DATA.CREATED_BY.eq(userId)
										.or(OFC_RECORD_DATA.MODIFIED_BY.eq(userId)
									)
								)
							)
					)
				)
			)
		);
		return count > 0;
	}

	public List<CollectRecordSummary> loadSummaries(RecordFilter filter) {
		return loadSummaries(filter, null);
	}
	
	public List<CollectRecordSummary> loadSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		CollectSurvey survey = filter.getSurvey();
		SelectQuery<Record> q = createSelectSummariesQuery(filter, sortFields);
		Result<Record> result = q.fetch();
		return fromSummaryQueryResult(result, survey);
	}
	
	public List<CollectRecordSummary> loadFullSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		List<CollectRecordSummary> recordSummaries = loadSummaries(filter, sortFields);
		for (CollectRecordSummary recordSummary : recordSummaries) {
			recordSummary.clearStepSummaries();
			Map<Step, StepSummary> summaryByStep = loadLatestStepSummaries(filter.getSurvey(), recordSummary.getId());
			for (Step step : Step.values()) {
				StepSummary stepSummary = summaryByStep.get(step);
				if (stepSummary != null) {
					recordSummary.addStepSummary(stepSummary);
				}
			}
		}
		return recordSummaries;
	}

	public void visitSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields, 
			Visitor<CollectRecordSummary> visitor) {
		visitSummaries(filter, sortFields, visitor, false);
	}
	
	public void visitSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields, 
			Visitor<CollectRecordSummary> visitor, boolean includeStepDetails) {
		SelectQuery<Record> q = createSelectSummariesQuery(filter, sortFields);

		Cursor<Record> cursor = null;
		try {
			cursor = q.fetchLazy();
			while (cursor.hasNext()) {
				Record r = cursor.fetchOne();
				CollectRecordSummary s = fromSummaryQueryRecord(filter.getSurvey(), r);
				if (includeStepDetails) {
					s.clearStepSummaries();
					Map<Step, StepSummary> summaryByStep = loadLatestStepSummaries(filter.getSurvey(), s.getId());
					for (Step step : Step.values()) {
						StepSummary stepSummary = summaryByStep.get(step);
						if (stepSummary != null) {
							s.addStepSummary(stepSummary);
						}
					}
				}
				visitor.visit(s);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	private SelectQuery<Record> createSelectSummariesQuery(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		SelectQuery<Record> q = dsl.selectQuery();
		
		q.addSelect(SUMMARY_FIELDS);
		Field<String> ownerNameField = OFC_USER.USERNAME.as(RecordSummarySortField.Sortable.OWNER_NAME.name());
		q.addSelect(ownerNameField);
		q.addFrom(OFC_RECORD);
		//join with user table to get owner name
		q.addJoin(OFC_USER, JoinType.LEFT_OUTER_JOIN, OFC_RECORD.OWNER_ID.equal(OFC_USER.ID));

		addRecordSummaryFilterConditions(q, filter);
		
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
	
	private Map<Step, StepSummary> loadLatestStepSummaries(CollectSurvey survey, int recordId) {
		Map<Step, StepSummary> summaryByStep = new HashMap<Step, StepSummary>();
		List<StepSummary> allStepsSummaries = loadAllStepsSummaries(survey, recordId);
		ListIterator<StepSummary> listIterator = allStepsSummaries.listIterator(allStepsSummaries.size());
		while(listIterator.hasPrevious() && summaryByStep.size() < Step.values().length) {
			StepSummary stepSummary = listIterator.previous();
			summaryByStep.put(stepSummary.getStep(), stepSummary);
		}
		return summaryByStep;
	}
	
	private List<StepSummary> loadAllStepsSummaries(CollectSurvey survey, int recordId) {
		Result<Record> result = dsl.select(RECORD_DATA_FULL_SUMMARY_FIELDS)
			.from(OFC_RECORD_DATA).join(OFC_RECORD).on(OFC_RECORD.ID.eq(OFC_RECORD_DATA.RECORD_ID))
			.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId))
			.orderBy(OFC_RECORD_DATA.SEQ_NUM)
			.fetch();
		
		return fromStepSummaryQueryResult(result, survey);
	}
	
	public Date[] findWorkingPeriod(int surveyId) {
		Record2<Timestamp, Timestamp> record = dsl()
			.select(DSL.min(OFC_RECORD.DATE_CREATED), DSL.max(OFC_RECORD.DATE_MODIFIED))
			.from(OFC_RECORD)
			.where(OFC_RECORD.SURVEY_ID.eq(surveyId))
			.fetchOne();
		Date start = (Date) record.getValue(0);
		Date end = (Date) record.getValue(1);
		if (start == null || end == null) {
			return null;
		} else {
			return new Date[]{start, end};
		}
	}

	private void addRecordSummaryFilterConditions(SelectQuery<?> q, RecordFilter filter) {
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
		
		if (CollectionUtils.isNotEmpty(filter.getQualifiers())) {
			addFilterByFieldsConditions(q, RECORD_QUALIFIER_FIELDS, false, false, filter.getQualifiers());
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
		SelectQuery<Record> q = dsl.selectQuery();
		q.addSelect(DSL.count());
		q.addFrom(OFC_RECORD);
		addRecordSummaryFilterConditions(q, filter);
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
		addFilterByFieldsConditions(q, RECORD_KEY_FIELDS, caseSensitiveKeyValues, includeNullConditions, keyValues);
	}
	
	private void addFilterByFieldsConditions(SelectQuery<?> q, TableField[] fields,
			boolean caseSensitiveValues, boolean includeNullConditions, List<String> values) {
		addFilterByFieldsConditions(q, fields, caseSensitiveValues, includeNullConditions, values.toArray(new String[values.size()]));
	}

	private void addFilterByFieldsConditions(SelectQuery q, TableField[] fields, boolean caseSensitiveValues, boolean includeNullConditions,
			String... values) {
		if ( values != null && values.length > 0 ) {
			for (int i = 0; i < values.length && i < fields.length; i++) {
				String key = values[i];
				@SuppressWarnings("unchecked")
				Field<String> keyField = (Field<String>) fields[i];
				if (StringUtils.isNotBlank(key)) {
					if (caseSensitiveValues) {
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

	public void insert(CollectRecord record) {
		execute(createInsertQueries(record));
	}
	
	public List<CollectStoreQuery> createInsertQueries(CollectRecord r) {
		InsertQuery<OfcRecordRecord> recordStoreQuery = dsl.insertQuery(OFC_RECORD);
		int recordId = nextId();
		recordStoreQuery.addValue(OFC_RECORD.ID, recordId);
		r.setId(recordId); //TODO set id here before executing the query?!
		r.setWorkflowSequenceNumber(1);
		fillRecordStoreQueryFromObject(recordStoreQuery, r);
		
		return Arrays.asList(new CollectStoreQuery(recordStoreQuery), createRecordStepInsertQuery(r, recordId));
	}
	
	private CollectStoreQuery createRecordStepInsertQuery(CollectRecord r,int recordId) {
		InsertQuery<OfcRecordDataRecord> recordStepStoreQuery = dsl.insertQuery(OFC_RECORD_DATA);
		fillRecordStepStoreQueryFromObject(recordStepStoreQuery, recordId, r, true);
		return new CollectStoreQuery(recordStepStoreQuery);
	}

	public void update(CollectRecord record) {
		execute(createUpdateQueries(record, record.getStep()));
	}
	
	public void updateSummary(CollectRecord record) {
		execute(Arrays.asList(createSummaryUpdateQuery(record)));
	}
	
	public void updateStepDataState(CollectSurvey survey, int recordId, Step step, State state) {
		dsl.update(OFC_RECORD_DATA)
			.set(OFC_RECORD_DATA.STATE, state == null ? null : state.getCode())
			.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
					.and(OFC_RECORD_DATA.SEQ_NUM.eq(
							getLatestWorkflowSequenceNumber(recordId, step, true)
					))
			).execute();
	}
	
	public Step duplicateLatestActiveStepData(CollectSurvey survey, int recordId) {
		Field[] columns = ArrayUtils.addAll(RECORD_DATA_FULL_SUMMARY_FIELDS, new Field[] {OFC_RECORD_DATA.DATA, OFC_RECORD_DATA.APP_VERSION});
		int indexOfSequenceNumberCol = ArrayUtils.indexOf(columns, OFC_RECORD_DATA.SEQ_NUM);
		Field[] selectColumns = ArrayUtils.clone(columns);
		Integer latestWorkflowSequenceNumber = getLatestWorkflowSequenceNumber(recordId);
		Integer nextSequenceNumber = getNextWorkflowSequenceNumber(recordId);
		selectColumns[indexOfSequenceNumberCol] = DSL.val(nextSequenceNumber);
		
		dsl.insertInto(OFC_RECORD_DATA)
			.columns(columns)
			.select(dsl.select(selectColumns)
				.from(OFC_RECORD_DATA)
				.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
					.and(OFC_RECORD_DATA.SEQ_NUM.eq(latestWorkflowSequenceNumber))
				)
			).execute();
		
		String newStepCode = (String) dsl.select(OFC_RECORD_DATA.STEP)
			.from(OFC_RECORD_DATA)
			.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
					.and(OFC_RECORD_DATA.SEQ_NUM.eq(nextSequenceNumber)))
			.fetchOne(0);
		return Step.valueOf(newStepCode);
	}

	private Integer getNextWorkflowSequenceNumber(int recordId) {
		Integer latestWorkflowSequenceNumber = getLatestWorkflowSequenceNumber(recordId, null, false);
		return latestWorkflowSequenceNumber == null ? 0 : latestWorkflowSequenceNumber + 1;
	}

	private Integer getLatestWorkflowSequenceNumber(int recordId) {
		return getLatestWorkflowSequenceNumber(recordId, null, true);
	}
	
	private Integer getLatestWorkflowSequenceNumber(int recordId, Step step, boolean notRejected) {
		Condition condition = OFC_RECORD_DATA.RECORD_ID.eq(recordId);
		if (notRejected) {
			condition = condition.and(OFC_RECORD_DATA.STATE.isNull());
		}
		if (step != null) {
			condition = condition.and(OFC_RECORD_DATA.STEP.eq(step.getStepNumber()));
		}
		Select<Record1<Integer>> query = dsl.select(DSL.max(OFC_RECORD_DATA.SEQ_NUM))
				.from(OFC_RECORD_DATA)
				.where(condition);
		return (Integer) query.fetchOne(0);
	}

	public List<CollectStoreQuery> createUpdateQueries(CollectRecord r, Step step) {
		CollectStoreQuery recordStepStoreQuery;
		if (r.getWorkflowSequenceNumber() == null) {
			Integer latestWorkflowSequenceNumber = getLatestWorkflowSequenceNumber(r.getId(), step, true);
			if (latestWorkflowSequenceNumber == null) {
				//create new step data
				recordStepStoreQuery = createRecordStepInsertQuery(r, r.getId());
			} else {
				r.setWorkflowSequenceNumber(latestWorkflowSequenceNumber);
				recordStepStoreQuery = createRecordStepUpdateQuery(r, true);
			}
		} else {
			recordStepStoreQuery = createRecordStepUpdateQuery(r, true);
		}
		return Arrays.asList(createSummaryUpdateQuery(r), recordStepStoreQuery);
	}

	private CollectStoreQuery createSummaryUpdateQuery(CollectRecord r) {
		UpdateQuery<OfcRecordRecord> q = dsl.updateQuery(OFC_RECORD);
		
		fillRecordStoreQueryFromObject(q, r);
		
		q.addConditions(OFC_RECORD.ID.eq(r.getId()));
		return new CollectStoreQuery(q);
	}
	
	private CollectStoreQuery createRecordStepUpdateQuery(CollectRecord r, boolean storeData) {
		Integer recordId = r.getId();
		
		UpdateQuery<OfcRecordDataRecord> q = dsl.updateQuery(OFC_RECORD_DATA);
		
		fillRecordStepStoreQueryFromObject(q, recordId, r, storeData);
		
		q.addConditions(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
			.and(OFC_RECORD_DATA.SEQ_NUM.eq(r.getWorkflowSequenceNumber())));
		return new CollectStoreQuery(q);
	}
	
	@SuppressWarnings("unchecked")
	protected void fillRecordStoreQueryFromObject(StoreQuery<?> q, CollectRecord r) {
		Entity rootEntity = r.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		int rootEntityDefnId = rootEntityDefn.getId();
		q.addValue(OFC_RECORD.SURVEY_ID, r.getSurvey().getId());
		q.addValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, rootEntityDefnId);
		q.addValue(OFC_RECORD.DATE_CREATED, toTimestamp(r.getCreationDate()));
		q.addValue(OFC_RECORD.CREATED_BY_ID, getUserId(r.getCreatedBy()));
		q.addValue(OFC_RECORD.DATE_MODIFIED, toTimestamp(r.getModifiedDate()));
		q.addValue(OFC_RECORD.MODIFIED_BY_ID, getUserId(r.getModifiedBy()));
		
		q.addValue(OFC_RECORD.MODEL_VERSION, r.getVersion() != null ? r.getVersion().getName(): null);
		q.addValue(OFC_RECORD.STEP, r.getStep().getStepNumber());
		q.addValue(OFC_RECORD.STATE, r.getState() != null ? r.getState().getCode(): null);
		q.addValue(OFC_RECORD.DATA_SEQ_NUM, r.getWorkflowSequenceNumber());

		q.addValue(OFC_RECORD.OWNER_ID, getUserId(r.getOwner()));

		q.addValue(OFC_RECORD.SKIPPED, r.getSkipped());
		q.addValue(OFC_RECORD.MISSING, r.getMissing());
		q.addValue(OFC_RECORD.ERRORS, r.getErrors());
		q.addValue(OFC_RECORD.WARNINGS, r.getWarnings());

		// set keys
		List<String> keys = r.getRootEntityKeyValues();
		for (int i = 0; i < keys.size(); i++) {
			q.addValue(RECORD_KEY_FIELDS[i], keys.get(i));
		}
		// set counts
		List<Integer> counts = r.getEntityCounts();
		for (int i = 0; i < counts.size(); i++) {
			q.addValue(RECORD_COUNT_FIELDS[i], counts.get(i));
		}
		
		// set qualifier values
		List<String> qualifierValues = r.getQualifierValues();
		for (int i = 0; i < qualifierValues.size(); i++) {
			q.addValue(RECORD_QUALIFIER_FIELDS[i], qualifierValues.get(i));
		}
		
		// set summary values
		List<String> summaryValues = r.getSummaryValues();
		for (int i = 0; i < summaryValues.size(); i++) {
			q.addValue(RECORD_SUMMARY_FIELDS[i], summaryValues.get(i));
		}
		
	}

	@SuppressWarnings({"unchecked"})
	protected void fillRecordStepStoreQueryFromObject(StoreQuery<?> q, int recordId, CollectRecord r, boolean storeData) {
		q.addValue(OFC_RECORD_DATA.RECORD_ID, recordId);
		q.addValue(OFC_RECORD_DATA.SEQ_NUM, r.getWorkflowSequenceNumber());
		q.addValue(OFC_RECORD_DATA.DATE_CREATED, toTimestamp(r.getCurrentStepCreationDate()));
		q.addValue(OFC_RECORD_DATA.CREATED_BY, getUserId(r.getCurrentStepCreatedBy()));
		q.addValue(OFC_RECORD_DATA.DATE_MODIFIED, toTimestamp(r.getCurrentStepModifiedDate()));
		q.addValue(OFC_RECORD_DATA.MODIFIED_BY, getUserId(r.getCurrentStepModifiedBy()));
		q.addValue(OFC_RECORD_DATA.STEP, r.getStep().getStepNumber());
		q.addValue(OFC_RECORD_DATA.STATE, r.getState() != null ? r.getState().getCode(): null);
		q.addValue(OFC_RECORD_DATA.SKIPPED, r.getSkipped());
		q.addValue(OFC_RECORD_DATA.MISSING, r.getMissing());
		q.addValue(OFC_RECORD_DATA.ERRORS, r.getErrors());
		q.addValue(OFC_RECORD_DATA.WARNINGS, r.getWarnings());
		q.addValue(OFC_RECORD_DATA.APP_VERSION, r.getApplicationVersion().toString());

		// set keys
		List<String> keys = r.getRootEntityKeyValues();
		for (int i = 0; i < keys.size(); i++) {
			q.addValue(RECORD_DATA_KEY_FIELDS[i], keys.get(i));
		}

		// set counts
		List<Integer> counts = r.getEntityCounts();
		for (int i = 0; i < counts.size(); i++) {
			q.addValue(RECORD_DATA_COUNT_FIELDS[i], counts.get(i));
		}
		
		// set qualifiers
		List<String> qualifiers = r.getQualifierValues();
		for (int i = 0; i < qualifiers.size(); i++) {
			q.addValue(RECORD_DATA_QUALIFIER_FIELDS[i], qualifiers.get(i));
		}
		
		// set summary values
		List<String> summaryValues = r.getQualifierValues();
		for (int i = 0; i < summaryValues.size(); i++) {
			q.addValue(RECORD_DATA_SUMMARY_FIELDS[i], summaryValues.get(i));
		}
		
		if (storeData) {
			Entity rootEntity = r.getRootEntity();
			byte[] data = new ModelSerializer(SERIALIZATION_BUFFER_SIZE).toByteArray(rootEntity);
			q.addValue(OFC_RECORD_DATA.DATA, data);
		}
	}

	public SelectQuery selectRecordQuery(int id, boolean onlySummary, Step step, Integer workflowSequenceNumber) {
		SelectQuery<Record> query = dsl.selectQuery();
		query.addSelect(RECORD_DATA_FULL_SUMMARY_FIELDS);
		if (! onlySummary) {
			query.addSelect(OFC_RECORD_DATA.DATA, OFC_RECORD_DATA.APP_VERSION);
		}
		query.addFrom(OFC_RECORD_DATA);
		if (workflowSequenceNumber == null) {
			workflowSequenceNumber = getLatestWorkflowSequenceNumber(id, step, true);
		}
		query.addJoin(OFC_RECORD, 
			OFC_RECORD_DATA.RECORD_ID.eq(OFC_RECORD.ID)
			.and(OFC_RECORD_DATA.SEQ_NUM.eq(workflowSequenceNumber))
		);
		query.addConditions(OFC_RECORD.ID.equal(id));
		return query;
	}
	
	@SuppressWarnings("unchecked")
	public CollectRecord fromQueryResult(CollectSurvey survey, Record r, boolean recordToBeUpdated) {
		int rootEntityId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
		String version = r.getValue(OFC_RECORD.MODEL_VERSION);
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getDefinitionById(rootEntityId);
		CollectRecord c = new CollectRecord(survey, version, rootEntityDefn, recordToBeUpdated);
		c.setId(r.getValue(OFC_RECORD.ID));
		c.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
		c.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
		c.setCreatedBy(createDetachedUser(r.getValue(OFC_RECORD.CREATED_BY_ID)));
		c.setModifiedBy(createDetachedUser(r.getValue(OFC_RECORD.MODIFIED_BY_ID)));
		
		c.setOwner(createDetachedUser(r.getValue(OFC_RECORD.OWNER_ID)));
		
		c.setStep(Step.valueOf(r.getValue(OFC_RECORD_DATA.STEP)));
		c.setWorkflowSequenceNumber(r.getValue(OFC_RECORD_DATA.SEQ_NUM));
		c.setCurrentStepCreationDate(r.getValue(OFC_RECORD_DATA.DATE_CREATED));
		c.setCurrentStepModifiedDate(r.getValue(OFC_RECORD_DATA.DATE_MODIFIED));
		c.setCurrentStepCreatedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.CREATED_BY)));
		c.setCurrentStepModifiedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.MODIFIED_BY)));
		c.setWarnings(r.getValue(OFC_RECORD_DATA.WARNINGS));
		c.setErrors(r.getValue(OFC_RECORD_DATA.ERRORS));
		c.setSkipped(r.getValue(OFC_RECORD_DATA.SKIPPED));
		c.setMissing(r.getValue(OFC_RECORD_DATA.MISSING));
		
		String state = r.getValue(OFC_RECORD.STATE);
		c.setState(state == null ? null : State.fromCode(state));
		
		// create list of entity counts
		List<Integer> counts = new ArrayList<Integer>(RECORD_COUNT_FIELDS.length);
		for (TableField tableField : RECORD_COUNT_FIELDS) {
			counts.add((Integer) r.getValue(tableField));
		}
		c.setEntityCounts(counts);

		c.setApplicationVersion(new Version(r.getValue(OFC_RECORD_DATA.APP_VERSION)));
		byte[] data = r.getValue(OFC_RECORD_DATA.DATA);
		ModelSerializer modelSerializer = new ModelSerializer(SERIALIZATION_BUFFER_SIZE);
		Entity rootEntity = c.getRootEntity();
		modelSerializer.mergeFrom(data, rootEntity);

		// create list of keys
		EntityDefinition rootEntityDef = c.getRootEntity().getDefinition();
		List<AttributeDefinition> keyDefs = rootEntityDef.getKeyAttributeDefinitions();
		List<String> keys = new ArrayList<String>(keyDefs.size());
		for (int i = 0; i < keyDefs.size(); i++) {
			TableField tableField = RECORD_KEY_FIELDS[i];
			keys.add((String) r.getValue(tableField));
		}
		c.setRootEntityKeyValues(keys);
		
		return c;
	}
	
	public List<CollectRecordSummary> fromSummaryQueryResult(Result<Record> result, CollectSurvey survey) {
		List<CollectRecordSummary> summaries = new ArrayList<CollectRecordSummary>(result.size());
		for (Record record : result) {
			summaries.add(fromSummaryQueryRecord(survey, record));
		}
		return summaries;
	}
	
	@SuppressWarnings("unchecked")
	public CollectRecordSummary fromSummaryQueryRecord(CollectSurvey survey, Record r) {
		int rootEntityDefId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
		String versionName = r.getValue(OFC_RECORD.MODEL_VERSION);
		ModelVersion modelVersion = versionName == null ? null : survey.getVersion(versionName);
		
		CollectRecordSummary s = new CollectRecordSummary();
		s.setSurvey(survey);
		s.setVersion(modelVersion);
		s.setRootEntityDefinitionId(rootEntityDefId);
		s.setId(r.getValue(OFC_RECORD.ID));
		s.setOwner(createDetachedUser(r.getValue(OFC_RECORD.OWNER_ID)));
		
		Step step = Step.valueOf(r.getValue(OFC_RECORD.STEP));
		s.setStep(step);
		s.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
		s.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
		s.setCreatedBy(createDetachedUser(r.getValue(OFC_RECORD.CREATED_BY_ID)));
		s.setModifiedBy(createDetachedUser(r.getValue(OFC_RECORD.MODIFIED_BY_ID)));
		
		StepSummary stepSummary = new StepSummary(step);
		stepSummary.setSequenceNumber(r.getValue(OFC_RECORD.DATA_SEQ_NUM));
		stepSummary.setErrors(r.getValue(OFC_RECORD.ERRORS));
		stepSummary.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
		stepSummary.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
		stepSummary.setMissing(r.getValue(OFC_RECORD.MISSING));
		
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinition(rootEntityDefId);

		// create list of entity counts
		List<EntityDefinition> countableDefs = survey.getSchema().getCountableEntitiesInRecordList(rootEntityDef);
		List<Integer> entityCounts = new ArrayList<Integer>(countableDefs.size());
		for (int i = 0; i < countableDefs.size(); i++) {
			entityCounts.add((Integer) r.getValue(RECORD_COUNT_FIELDS[i]));
		}
		stepSummary.setEntityCounts(entityCounts);

		// key values
		List<AttributeDefinition> keyDefs = rootEntityDef.getKeyAttributeDefinitions();
		List<String> keys = new ArrayList<String>(keyDefs.size());
		for (int i = 0; i < keyDefs.size(); i++) {
			keys.add((String) r.getValue(RECORD_KEY_FIELDS[i]));
		}
		stepSummary.setRootEntityKeyValues(keys);
		
		// qualifier values
		List<AttributeDefinition> qualifierDefs = survey.getSchema().getQualifierAttributeDefinitions(rootEntityDef);
		List<String> qualifiers = new ArrayList<String>(qualifierDefs.size());
		for (int i = 0; i < qualifierDefs.size(); i++) {
			qualifiers.add((String) r.getValue(RECORD_QUALIFIER_FIELDS[i]));
		}
		stepSummary.setQualifierValues(qualifiers);
		
		// summary values
		List<AttributeDefinition> summaryDefs = survey.getSchema().getSummaryAttributeDefinitions(rootEntityDef);
		List<String> summaryValues = new ArrayList<String>(summaryDefs.size());
		for (int i = 0; i < summaryDefs.size(); i++) {
			summaryValues.add((String) r.getValue(RECORD_SUMMARY_FIELDS[i]));
		}
		stepSummary.setSummaryValues(summaryValues);
		
		String state = r.getValue(OFC_RECORD.STATE);
		stepSummary.setState(state == null ? null : State.fromCode(state));
		
		s.addStepSummary(stepSummary);
		
		return s;
	}
	
	public List<StepSummary> fromStepSummaryQueryResult(Result<Record> result, CollectSurvey survey) {
		List<StepSummary> summaries = new ArrayList<StepSummary>(result.size());
		for (Record record : result) {
			summaries.add(fromStepSummaryQueryRecord(record, survey));
		}
		return summaries;
	}

	@SuppressWarnings("unchecked")
	public StepSummary fromStepSummaryQueryRecord(Record r, CollectSurvey survey) {
		Step step = Step.valueOf(r.getValue(OFC_RECORD.STEP));
		StepSummary s = new StepSummary(step);

		s.setCreationDate(r.getValue(OFC_RECORD_DATA.DATE_CREATED));
		s.setModifiedDate(r.getValue(OFC_RECORD_DATA.DATE_MODIFIED));
		s.setCreatedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.CREATED_BY)));
		s.setModifiedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.MODIFIED_BY)));
		
		s.setErrors(r.getValue(OFC_RECORD_DATA.ERRORS));
		s.setWarnings(r.getValue(OFC_RECORD_DATA.WARNINGS));
		s.setSkipped(r.getValue(OFC_RECORD_DATA.SKIPPED));
		s.setMissing(r.getValue(OFC_RECORD_DATA.MISSING));
		
		// create list of entity counts
		List<Integer> counts = new ArrayList<Integer>(RECORD_DATA_COUNT_FIELDS.length);
		for (TableField tableField : RECORD_DATA_COUNT_FIELDS) {
			counts.add((Integer) r.getValue(tableField));
		}
		s.setEntityCounts(counts);

		int rootEntityDefId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinition(rootEntityDefId);
		
		// key values
		List<AttributeDefinition> keyDefs = rootEntityDef.getKeyAttributeDefinitions();
		List<String> keys = new ArrayList<String>(keyDefs.size());
		for (int i = 0; i < keyDefs.size(); i++) {
			keys.add((String) r.getValue(RECORD_DATA_KEY_FIELDS[i]));
		}
		s.setRootEntityKeyValues(keys);
		
		// qualifier values
		List<AttributeDefinition> qualifierDefs = schema.getQualifierAttributeDefinitions(rootEntityDef);
		List<String> qualifiers = new ArrayList<String>(qualifierDefs.size());
		for (int i = 0; i < qualifierDefs.size(); i++) {
			qualifiers.add((String) r.getValue(RECORD_DATA_QUALIFIER_FIELDS[i]));
		}
		s.setQualifierValues(qualifiers);
		
		// summary values
		List<AttributeDefinition> summaryDefs = schema.getSummaryAttributeDefinitions(rootEntityDef);
		List<String> summaryValues = new ArrayList<String>(summaryDefs.size());
		for (int i = 0; i < summaryDefs.size(); i++) {
			summaryValues.add((String) r.getValue(RECORD_DATA_SUMMARY_FIELDS[i]));
		}
		s.setSummaryValues(summaryValues);
		
		String state = r.getValue(OFC_RECORD_DATA.STATE);
		s.setState(state == null ? null : State.fromCode(state));
		
		return s;
	}
	
	public void execute(List<CollectStoreQuery> queries) {
		List<Query> internalQueries = new ArrayList<Query>(queries.size());
		for (CollectStoreQuery recordStoreQuery : queries) {
			internalQueries.add(recordStoreQuery.getInternalQuery());
		}
		Batch batch = dsl().batch(internalQueries);
		batch.execute();
	}
	
	public void delete(int id) {
		dsl().deleteFrom(OFC_RECORD_DATA)
			.where(OFC_RECORD_DATA.RECORD_ID.eq(id))
			.execute();
		dsl().deleteFrom(OFC_RECORD)
			.where(OFC_RECORD.ID.eq(id))
			.execute();
	}
	
	public void deleteByIds(Set<Integer> ids) {
		for (Integer id : ids) {
			delete(id);
		}
	}

	public void assignOwner(int recordId, Integer ownerId) {
		dsl().update(OFC_RECORD)
			.set(OFC_RECORD.OWNER_ID, ownerId)
			.where(OFC_RECORD.ID.eq(recordId))
			.execute();
	}

	public void deleteBySurvey(int id) {
		dsl().deleteFrom(OFC_RECORD_DATA)
				.where(OFC_RECORD_DATA.RECORD_ID.in(
			dsl().select(OFC_RECORD.ID)
				.from(OFC_RECORD)
				.where(OFC_RECORD.SURVEY_ID.eq(id))
		)).execute();
		
		dsl().delete(OFC_RECORD)
			.where(OFC_RECORD.SURVEY_ID.equal(id))
			.execute();
	}
	
	public int nextId() {
		return dsl().nextId(OFC_RECORD.ID, OFC_RECORD_ID_SEQ);
	}
	
	public void restartIdSequence(Number value) {
		dsl().restartSequence(OFC_RECORD_ID_SEQ, value);
	}
	
	private static User createDetachedUser(Integer userId) {
		if ( userId == null ) {
			return null;
		}
		User user = new User();
		user.setId(userId);
		return user;
	}
	
	private Integer getUserId(User user) {
		return user == null ? null : user.getId();
	}

}
