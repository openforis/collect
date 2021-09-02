package org.openforis.collect.persistence;


import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.val;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jooq.Batch;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.InsertQuery;
import org.jooq.JoinType;
import org.jooq.Param;
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
import org.openforis.collect.Collect;
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
import org.openforis.collect.utils.Numbers;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.commons.collection.Visitor;
import org.openforis.commons.versioning.Version;
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
	
	private static final TableField[] RECORD_FULL_SUMMARY_FIELDS = addAll(
			new TableField[]{OFC_RECORD.ID, OFC_RECORD.SURVEY_ID, OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.MODEL_VERSION,
				OFC_RECORD.DATE_CREATED, OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_MODIFIED, OFC_RECORD.MODIFIED_BY_ID, 
				OFC_RECORD.STEP, OFC_RECORD.DATA_SEQ_NUM, OFC_RECORD.STATE, 
				OFC_RECORD.ERRORS, OFC_RECORD.WARNINGS, OFC_RECORD.MISSING, OFC_RECORD.SKIPPED, 
				OFC_RECORD.OWNER_ID},
			addAll(
					addAll(
							addAll(RECORD_KEY_FIELDS, RECORD_COUNT_FIELDS), 
							RECORD_SUMMARY_FIELDS), 
					RECORD_QUALIFIER_FIELDS)
			);

	private static final TableField[] RECORD_DATA_FULL_SUMMARY_FIELDS = addAll(
			new TableField[]{OFC_RECORD.SURVEY_ID, OFC_RECORD.ROOT_ENTITY_DEFINITION_ID, OFC_RECORD.ID,
				OFC_RECORD.STEP, OFC_RECORD.DATA_SEQ_NUM, OFC_RECORD.MODEL_VERSION, OFC_RECORD.OWNER_ID, 
				OFC_RECORD.CREATED_BY_ID, OFC_RECORD.DATE_CREATED, OFC_RECORD.MODIFIED_BY_ID, OFC_RECORD.DATE_MODIFIED, 
				OFC_RECORD_DATA.RECORD_ID, OFC_RECORD_DATA.DATE_CREATED, OFC_RECORD_DATA.CREATED_BY, OFC_RECORD_DATA.DATE_MODIFIED, OFC_RECORD_DATA.MODIFIED_BY, 
				OFC_RECORD_DATA.STEP, OFC_RECORD_DATA.SEQ_NUM, OFC_RECORD_DATA.STATE, OFC_RECORD_DATA.ERRORS,
				OFC_RECORD_DATA.WARNINGS, OFC_RECORD_DATA.MISSING, OFC_RECORD_DATA.SKIPPED},
			addAll(
					addAll(
							addAll(RECORD_DATA_KEY_FIELDS, RECORD_DATA_COUNT_FIELDS), 
							RECORD_DATA_SUMMARY_FIELDS), 
					RECORD_DATA_QUALIFIER_FIELDS)
		);

	private static final TableField[] RECORD_DATA_INSERT_FIELDS = addAll(new TableField[]{
			OFC_RECORD_DATA.RECORD_ID, OFC_RECORD_DATA.DATE_CREATED, OFC_RECORD_DATA.CREATED_BY, OFC_RECORD_DATA.DATE_MODIFIED, OFC_RECORD_DATA.MODIFIED_BY, 
			OFC_RECORD_DATA.STEP, OFC_RECORD_DATA.SEQ_NUM, OFC_RECORD_DATA.STATE, OFC_RECORD_DATA.DATA, OFC_RECORD_DATA.APP_VERSION, 
			OFC_RECORD_DATA.ERRORS, OFC_RECORD_DATA.WARNINGS, OFC_RECORD_DATA.MISSING, OFC_RECORD_DATA.SKIPPED},
		addAll(
				addAll(
						addAll(RECORD_DATA_KEY_FIELDS, RECORD_DATA_COUNT_FIELDS), 
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
				.or(exists(
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
		visitSummaries(filter, sortFields, visitor, false, null);
	}
	
	public void visitSummaries(RecordFilter filter, List<RecordSummarySortField> sortFields, 
			Visitor<CollectRecordSummary> visitor, boolean includeStepDetails, 
			Predicate<CollectRecordSummary> stopWhenPredicate) {
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
				if (stopWhenPredicate != null && stopWhenPredicate.evaluate(s)) {
					break;
				}					
			}
		} finally {
			if (cursor != null) {
				cursor.close();
		    }
		}
	}

	private SelectQuery<Record> createSelectSummariesQuery(RecordFilter filter, List<RecordSummarySortField> sortFields) {
		SelectQuery<Record> q = dsl.selectQuery();
		
		q.addSelect(RECORD_FULL_SUMMARY_FIELDS);
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
			if (! summaryByStep.containsKey(stepSummary.getStep()) && stepSummary.getState() == null) {
				summaryByStep.put(stepSummary.getStep(), stepSummary);
			}
		}
		return summaryByStep;
	}
	
	private List<StepSummary> loadAllStepsSummaries(CollectSurvey survey, int recordId) {
		Result<Record> result = dsl.select(RECORD_DATA_FULL_SUMMARY_FIELDS)
			.from(OFC_RECORD_DATA).join(OFC_RECORD).on(OFC_RECORD.ID.eq(OFC_RECORD_DATA.RECORD_ID))
			.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId))
			.orderBy(OFC_RECORD_DATA.SEQ_NUM)
			.fetch();
		
		return fromDataSummaryQueryResult(result, survey);
	}
	
	public Date[] findWorkingPeriod(int surveyId) {
		Record2<Timestamp, Timestamp> record = dsl()
			.select(min(OFC_RECORD.DATE_CREATED), max(OFC_RECORD.DATE_MODIFIED))
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
	
	public Set<Integer> loadDistinctOwnerIds(RecordFilter filter) {
		SelectQuery<?> q = dsl().selectQuery();
		q.addSelect(OFC_RECORD.OWNER_ID);
		q.addFrom(OFC_RECORD);
		addRecordSummaryFilterConditions(q, filter);
		q.addConditions(OFC_RECORD.OWNER_ID.isNotNull());
		return q.fetchSet(OFC_RECORD.OWNER_ID);
	}

	private void addRecordSummaryFilterConditions(SelectQuery<?> q, RecordFilter filter) {
		CollectSurvey survey = filter.getSurvey();
		//survey
		q.addConditions(OFC_RECORD.SURVEY_ID.equal(survey.getId()));
		
		//root entity
		if ( filter.getRootEntityId() != null ) {
			q.addConditions(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID.equal(filter.getRootEntityId()));
		}
		
		//record id
		if ( filter.getRecordId() != null ) {
			q.addConditions(OFC_RECORD.ID.equal(filter.getRecordId()));
		}
		
		//records ids
		if (CollectionUtils.isNotEmpty(filter.getRecordIds())) {
			q.addConditions(OFC_RECORD.ID.in(filter.getRecordIds()));
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
		//modified until
		if ( filter.getModifiedUntil() != null ) {
			q.addConditions(OFC_RECORD.DATE_MODIFIED.lessOrEqual(new Timestamp(filter.getModifiedUntil().getTime())));
		}
		//owners
		if ( filter.getOwnerIds() != null && !filter.getOwnerIds().isEmpty() ) {
			q.addConditions(OFC_RECORD.OWNER_ID.in(filter.getOwnerIds()));
		}
		//record keys
		if ( CollectionUtils.isNotEmpty( filter.getKeyValues() ) ) {
			addFilterByFieldsConditions(q, RECORD_KEY_FIELDS, filter.isCaseSensitiveKeyValues(), filter.isIncludeNullConditionsForKeyValues(), 
					filter.getKeyValues());
		}
		//qualifiers
		if (CollectionUtils.isNotEmpty(filter.getQualifiers())) {
			addFilterByFieldsConditions(q, RECORD_QUALIFIER_FIELDS, false, false, filter.getQualifiers());
		}
		//summary values
		if (CollectionUtils.isNotEmpty(filter.getSummaryValues())) {
			addFilterByFieldsConditions(q, RECORD_SUMMARY_FIELDS, false, false, filter.getSummaryValues());
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
		q.addSelect(count());
		q.addFrom(OFC_RECORD);
		addRecordSummaryFilterConditions(q, filter);
		Record record = q.fetchOne();
		int result = record.getValue(count());
		return result;
	}

	public int countRecords(CollectSurvey survey, int rootDefinitionId, String... keyValues) {
		RecordFilter filter = new RecordFilter(survey, rootDefinitionId);
		filter.setKeyValues(keyValues);
		return countRecords(filter);
	}
	
	private void addFilterByFieldsConditions(SelectQuery<?> q, TableField[] fields,
			boolean caseSensitiveValues, boolean isNullWhenNotSpecified, List<String> values) {
		addFilterByFieldsConditions(q, fields, caseSensitiveValues, isNullWhenNotSpecified, values.toArray(new String[values.size()]));
	}

	private void addFilterByFieldsConditions(SelectQuery q, TableField[] fields, boolean caseSensitiveValues, 
			boolean isNullWhenNotSpecified, String... values) {
		if ( values != null && values.length > 0 ) {
			for (int i = 0; i < values.length && i < fields.length; i++) {
				String value = values[i];
				@SuppressWarnings("unchecked")
				Field<String> field = (Field<String>) fields[i];
				if (isNotBlank(value)) {
					Condition condition;
					boolean likeSearchType = value.contains("*");
					if (likeSearchType) {
						condition = field.like(value.replaceAll("\\*", "%"));
					} else if (caseSensitiveValues) {
						condition = field.equal(value);
					} else {
						condition = field.upper().equal(value.toUpperCase(Locale.ENGLISH));
					}
					q.addConditions(condition);
				} else if (isNullWhenNotSpecified) {
					q.addConditions(field.isNull());
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
			case SUMMARY1:
				orderBy = OFC_RECORD.SUMMARY1;
				break;
			case SUMMARY2:
				orderBy = OFC_RECORD.SUMMARY2;
				break;
			case SUMMARY3:
				orderBy = OFC_RECORD.SUMMARY3;
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
		int recordId;
		if (r.getId() == null) {
			recordId = nextId();
			r.setId(recordId); //TODO set id here before executing the query?!
		} else {
			recordId = r.getId();
		}
		recordStoreQuery.addValue(OFC_RECORD.ID, recordId);
		r.setWorkflowSequenceNumber(1);
		r.setDataWorkflowSequenceNumber(1);
		fillRecordStoreQueryFromObject(recordStoreQuery, r);
		
		return Arrays.asList(new CollectStoreQuery(recordStoreQuery), createRecordDataInsertQuery(r, recordId, Step.ENTRY, 1));
	}
	
	@SuppressWarnings("unchecked")
	public CollectStoreQuery createRecordDataInsertQuery(CollectRecord r, int recordId, Step step, Integer sequenceNumber) {
		if (sequenceNumber == null) {
			List<Field<?>> insertFields = new ArrayList<Field<?>>();
			
			insertFields.addAll(Arrays.asList(val(recordId), 
					val(toTimestamp(r.getCreationDate())), val(getUserId(r.getCreatedBy())), 
					val(toTimestamp(r.getModifiedDate())), val(getUserId(r.getModifiedBy())),
					val(step.getStepNumber()), 
					coalesce(max(OFC_RECORD_DATA.SEQ_NUM), val(1)).add(1), 
					val(null), 
					val(new ModelSerializer(SERIALIZATION_BUFFER_SIZE).toByteArray(r.getRootEntity())), 
					val(Collect.VERSION.toString()),
					val(r.getErrors()), val(r.getWarnings()), val(r.getMissing()), val(r.getSkipped())));
			
			insertFields.addAll(createParamsFromFields(RECORD_DATA_KEY_FIELDS, r.getRootEntityKeyValues()));
			insertFields.addAll(createParamsFromFields(RECORD_DATA_COUNT_FIELDS, r.getEntityCounts()));
			insertFields.addAll(createParamsFromFields(RECORD_DATA_SUMMARY_FIELDS, r.getDataSummaryValues()));
			insertFields.addAll(createParamsFromFields(RECORD_DATA_QUALIFIER_FIELDS, r.getQualifierValues()));
			
			Insert<OfcRecordDataRecord> q = dsl.insertInto(OFC_RECORD_DATA)
				.columns(RECORD_DATA_INSERT_FIELDS)
				.select(dsl.select(insertFields)
						.from(OFC_RECORD_DATA)
						.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId))
				);
			return new CollectStoreQuery(q);
		} else {
			InsertQuery<OfcRecordDataRecord> q = dsl.insertQuery(OFC_RECORD_DATA);
			fillRecordDataStoreQueryFromObject(q, recordId, sequenceNumber, step, r);
			return new CollectStoreQuery(q);
		}
	}
	
	public void update(CollectRecord record) {
		execute(createUpdateQueries(record, record.getDataStep()));
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
		Integer sequenceNumber = getLatestWorkflowSequenceNumber(recordId);
		return duplicateStepData(recordId, sequenceNumber);
	}
	
	public Step duplicateLatestNotRejectedStepData(CollectSurvey survey, int recordId, Step step) {
		Integer sequenceNumber = getLatestWorkflowSequenceNumber(recordId, step, false);
		return duplicateStepData(recordId, sequenceNumber);
	}

	private Step duplicateStepData(int recordId, Integer latestWorkflowSequenceNumber) {
		List<Field> columns = new ArrayList<Field>();
		columns.addAll(Arrays.asList(RECORD_DATA_INSERT_FIELDS));
		int indexOfSequenceNumberCol = columns.indexOf(OFC_RECORD_DATA.SEQ_NUM);
		List<Field>  selectColumns = new ArrayList<Field>(columns);
		Integer nextSequenceNumber = getNextWorkflowSequenceNumber(recordId);
		Param<Integer> seqNumberVal = val(nextSequenceNumber);
		selectColumns.set(indexOfSequenceNumberCol, seqNumberVal);
		
		dsl.insertInto(OFC_RECORD_DATA)
			.columns(columns.toArray(new Field[columns.size()]))
			.select(dsl.select(selectColumns.toArray(new Field[selectColumns.size()]))
				.from(OFC_RECORD_DATA)
				.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
					.and(OFC_RECORD_DATA.SEQ_NUM.eq(latestWorkflowSequenceNumber))
				)
			).execute();
		
		int newStepNumber = dsl.select(OFC_RECORD_DATA.STEP)
			.from(OFC_RECORD_DATA)
			.where(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
					.and(OFC_RECORD_DATA.SEQ_NUM.eq(nextSequenceNumber)))
			.fetchOne(OFC_RECORD_DATA.STEP);
		return Step.valueOf(newStepNumber);
	}
	
	private Integer getNextWorkflowSequenceNumber(int recordId) {
		Integer latestWorkflowSequenceNumber = getLatestWorkflowSequenceNumber(recordId, null, false);
		return latestWorkflowSequenceNumber == null ? 1 : latestWorkflowSequenceNumber + 1;
	}

	private Integer getLatestWorkflowSequenceNumber(int recordId) {
		return getLatestWorkflowSequenceNumber(recordId, null, true);
	}
	
	private Integer getLatestWorkflowSequenceNumber(int recordId, Step step, boolean notRejected) {
		Select<Record1<Integer>> query = selectWorkflowSequenceNumber(recordId, step, notRejected);
		return (Integer) query.fetchOne(0);
	}

	private Select<Record1<Integer>> selectWorkflowSequenceNumber(int recordId, Step step, boolean notRejected) {
		Condition condition = OFC_RECORD_DATA.RECORD_ID.eq(recordId);
		if (notRejected) {
			condition = condition.and(OFC_RECORD_DATA.STATE.isNull());
		}
		if (step != null) {
			condition = condition.and(OFC_RECORD_DATA.STEP.eq(step.getStepNumber()));
		}
		Select<Record1<Integer>> query = dsl.select(max(OFC_RECORD_DATA.SEQ_NUM))
				.from(OFC_RECORD_DATA)
				.where(condition);
		return query;
	}

	public List<CollectStoreQuery> createUpdateQueries(CollectRecord r, Step dataStep) {
		CollectStoreQuery q;
		if (r.getDataWorkflowSequenceNumber() == null) {
			Integer latestWorkflowSequenceNumber = getLatestWorkflowSequenceNumber(r.getId(), dataStep, true);
			if (latestWorkflowSequenceNumber == null) {
				//create new step data
				int sequenceNumber = getNextWorkflowSequenceNumber(r.getId());
				r.setDataWorkflowSequenceNumber(sequenceNumber);
				q = createRecordDataInsertQuery(r, r.getId(), dataStep, sequenceNumber);
			} else {
				r.setDataWorkflowSequenceNumber(latestWorkflowSequenceNumber);
				q = createRecordDataUpdateQuery(r, r.getId(), dataStep, latestWorkflowSequenceNumber);
			}
		} else {
			q = createRecordDataUpdateQuery(r, r.getId(), dataStep, r.getDataWorkflowSequenceNumber());
		}
		List<CollectStoreQuery> queries = new ArrayList<CollectStoreQuery>();
		if (dataStep == r.getStep()) {
			queries.add(createSummaryUpdateQuery(r));
		}
		queries.add(q);
		return queries;
	}

	public CollectStoreQuery createSummaryUpdateQuery(CollectRecord r) {
		UpdateQuery<OfcRecordRecord> q = dsl.updateQuery(OFC_RECORD);
		
		fillRecordStoreQueryFromObject(q, r);
		
		q.addConditions(OFC_RECORD.ID.eq(r.getId()));
		return new CollectStoreQuery(q);
	}
	
	public CollectStoreQuery createRecordDataUpdateQuery(CollectRecord r, int recordId, Step step, int sequenceNumber) {
		UpdateQuery<OfcRecordDataRecord> q = dsl.updateQuery(OFC_RECORD_DATA);

		fillRecordDataStoreQueryFromObject(q, recordId, sequenceNumber, step, r);
		
		q.addConditions(OFC_RECORD_DATA.RECORD_ID.eq(recordId)
			.and(OFC_RECORD_DATA.SEQ_NUM.eq(sequenceNumber))
		);
		return new CollectStoreQuery(q);
	}
	
	public void updateRecordData(CollectRecord r, Step step, int sequenceNumber) {
		CollectStoreQuery q = createRecordDataUpdateQuery(r, r.getId(), step, sequenceNumber);
		execute(Arrays.asList(q));
	}
	
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
		q.addValue(OFC_RECORD.DATA_SEQ_NUM, r.getDataWorkflowSequenceNumber());

		q.addValue(OFC_RECORD.OWNER_ID, getUserId(r.getOwner()));

		q.addValue(OFC_RECORD.SKIPPED, r.getSkipped());
		q.addValue(OFC_RECORD.MISSING, r.getMissing());
		q.addValue(OFC_RECORD.ERRORS, r.getErrors());
		q.addValue(OFC_RECORD.WARNINGS, r.getWarnings());

		addValuesToQuery(q, RECORD_KEY_FIELDS, r.getRootEntityKeyValues());
		addValuesToQuery(q, RECORD_COUNT_FIELDS, r.getEntityCounts());
		addValuesToQuery(q, RECORD_QUALIFIER_FIELDS, r.getQualifierValues());
		addValuesToQuery(q, RECORD_SUMMARY_FIELDS, r.getDataSummaryValues());
	}

	protected void fillRecordDataStoreQueryFromObject(StoreQuery<?> q, int recordId, 
			Integer sequenceNumber, Step step, CollectRecord r) {
		q.addValues(createRecordDataFieldValueMap(recordId, sequenceNumber, step, r));
	}
	
	private Map<Field<?>, Object> createRecordDataFieldValueMap(int recordId, Integer sequenceNumber, Step step, CollectRecord r) {
		Map<Field<?>, Object> map = new HashMap<Field<?>, Object>();
		map.put(OFC_RECORD_DATA.RECORD_ID, recordId);
		if (sequenceNumber != null) {
			map.put(OFC_RECORD_DATA.SEQ_NUM, sequenceNumber);
		}
		map.put(OFC_RECORD_DATA.DATE_CREATED, toTimestamp(defaultIfNull(r.getDataCreationDate(), r.getCreationDate())));
		map.put(OFC_RECORD_DATA.CREATED_BY, getUserId(defaultIfNull(r.getDataCreatedBy(), r.getCreatedBy())));
		map.put(OFC_RECORD_DATA.DATE_MODIFIED, toTimestamp(defaultIfNull(r.getDataModifiedDate(), r.getModifiedDate())));
		map.put(OFC_RECORD_DATA.MODIFIED_BY, getUserId(defaultIfNull(r.getDataModifiedBy(), r.getModifiedBy())));
		map.put(OFC_RECORD_DATA.STEP, step.getStepNumber());
		map.put(OFC_RECORD_DATA.STATE, r.getState() != null ? r.getState().getCode(): null);
		map.put(OFC_RECORD_DATA.SKIPPED, r.getSkipped());
		map.put(OFC_RECORD_DATA.MISSING, r.getMissing());
		map.put(OFC_RECORD_DATA.ERRORS, r.getErrors());
		map.put(OFC_RECORD_DATA.WARNINGS, r.getWarnings());
		map.put(OFC_RECORD_DATA.APP_VERSION, r.getApplicationVersion().toString());

		addValuesToMap(map, RECORD_DATA_KEY_FIELDS, r.getRootEntityKeyValues());
		addValuesToMap(map, RECORD_DATA_COUNT_FIELDS, r.getEntityCounts());
		addValuesToMap(map, RECORD_DATA_QUALIFIER_FIELDS, r.getQualifierValues());
		addValuesToMap(map, RECORD_DATA_SUMMARY_FIELDS, r.getDataSummaryValues());
		
		Entity rootEntity = r.getRootEntity();
		byte[] data = new ModelSerializer(SERIALIZATION_BUFFER_SIZE).toByteArray(rootEntity);
		map.put(OFC_RECORD_DATA.DATA, data);
		
		return map;
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
	
	public CollectRecord fromQueryResult(CollectSurvey survey, Record r, boolean recordToBeUpdated) {
		int rootEntityId = r.getValue(OFC_RECORD.ROOT_ENTITY_DEFINITION_ID);
		String version = r.getValue(OFC_RECORD.MODEL_VERSION);
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefn = schema.getDefinitionById(rootEntityId);
		CollectRecord c = new CollectRecord(survey, version, rootEntityDefn, recordToBeUpdated);
		c.setId(r.getValue(OFC_RECORD.ID));
		c.setStep(Step.valueOf(r.getValue(OFC_RECORD.STEP)));
		c.setWorkflowSequenceNumber(r.getValue(OFC_RECORD.DATA_SEQ_NUM));
		c.setCreationDate(r.getValue(OFC_RECORD.DATE_CREATED));
		c.setModifiedDate(r.getValue(OFC_RECORD.DATE_MODIFIED));
		c.setCreatedBy(createDetachedUser(r.getValue(OFC_RECORD.CREATED_BY_ID)));
		c.setModifiedBy(createDetachedUser(r.getValue(OFC_RECORD.MODIFIED_BY_ID)));
		c.setWarnings(r.getValue(OFC_RECORD.WARNINGS));
		c.setErrors(r.getValue(OFC_RECORD.ERRORS));
		c.setSkipped(r.getValue(OFC_RECORD.SKIPPED));
		c.setMissing(r.getValue(OFC_RECORD.MISSING));
		c.setOwner(createDetachedUser(r.getValue(OFC_RECORD.OWNER_ID)));
		
		c.setDataStep(Step.valueOf(r.getValue(OFC_RECORD_DATA.STEP)));
		c.setDataWorkflowSequenceNumber(r.getValue(OFC_RECORD_DATA.SEQ_NUM));
		c.setDataCreationDate(r.getValue(OFC_RECORD_DATA.DATE_CREATED));
		c.setDataModifiedDate(r.getValue(OFC_RECORD_DATA.DATE_MODIFIED));
		c.setDataCreatedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.CREATED_BY)));
		c.setDataModifiedBy(createDetachedUser(r.getValue(OFC_RECORD_DATA.MODIFIED_BY)));
		c.setDataRootEntityKeyValues(getFieldValues(r, rootEntityDefn.getKeyAttributeDefinitions(), RECORD_DATA_KEY_FIELDS, String.class));
		c.setDataEntityCounts(getFieldValues(r, schema.getCountableEntitiesInRecordList(rootEntityDefn), RECORD_DATA_COUNT_FIELDS, Integer.class));
		c.setDataQualifierValues(getFieldValues(r, schema.getQualifierAttributeDefinitions(rootEntityDefn), RECORD_DATA_QUALIFIER_FIELDS, String.class));
		c.setDataSummaryValues(getFieldValues(r, schema.getSummaryAttributeDefinitions(rootEntityDefn), RECORD_DATA_SUMMARY_FIELDS, String.class));
		
		String state = r.getValue(OFC_RECORD.STATE);
		c.setState(state == null ? null : State.fromCode(state));
		
		c.setApplicationVersion(new Version(r.getValue(OFC_RECORD_DATA.APP_VERSION)));
		byte[] data = r.getValue(OFC_RECORD_DATA.DATA);
		ModelSerializer modelSerializer = new ModelSerializer(SERIALIZATION_BUFFER_SIZE);
		Entity rootEntity = c.getRootEntity();
		modelSerializer.mergeFrom(data, rootEntity);

		c.setRootEntityKeyValues(getFieldValues(r, rootEntityDefn.getKeyAttributeDefinitions(), RECORD_KEY_FIELDS, String.class));
		c.setEntityCounts(getFieldValues(r, schema.getCountableEntitiesInRecordList(rootEntityDefn), RECORD_COUNT_FIELDS, Integer.class));
		c.setQualifierValues(getFieldValues(r, schema.getQualifierAttributeDefinitions(rootEntityDefn), RECORD_QUALIFIER_FIELDS, String.class));
		c.setSummaryValues(getFieldValues(r, schema.getSummaryAttributeDefinitions(rootEntityDefn), RECORD_SUMMARY_FIELDS, String.class));
		return c;
	}

	public List<CollectRecordSummary> fromSummaryQueryResult(Result<Record> result, CollectSurvey survey) {
		List<CollectRecordSummary> summaries = new ArrayList<CollectRecordSummary>(result.size());
		for (Record record : result) {
			summaries.add(fromSummaryQueryRecord(survey, record));
		}
		return summaries;
	}
	
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
		s.setWorkflowSequenceNumber(r.getValue(OFC_RECORD.DATA_SEQ_NUM));
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
		stepSummary.setTotalErrors(Numbers.sum(
				stepSummary.getErrors(), 
				stepSummary.getSkipped(), 
				stepSummary.getMissing())
		);
		
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinition(rootEntityDefId);

		stepSummary.setRootEntityKeyValues(getFieldValues(r, rootEntityDef.getKeyAttributeDefinitions(), RECORD_KEY_FIELDS, String.class));
		stepSummary.setEntityCounts(getFieldValues(r, schema.getCountableEntitiesInRecordList(rootEntityDef), RECORD_COUNT_FIELDS, Integer.class));
		stepSummary.setQualifierValues(getFieldValues(r, schema.getQualifierAttributeDefinitions(rootEntityDef), RECORD_QUALIFIER_FIELDS, String.class));
		stepSummary.setSummaryValues(getFieldValues(r, schema.getSummaryAttributeDefinitions(rootEntityDef), RECORD_SUMMARY_FIELDS, String.class));
		
		String state = r.getValue(OFC_RECORD.STATE);
		stepSummary.setState(state == null ? null : State.fromCode(state));
		
		s.addStepSummary(stepSummary);
		
		return s;
	}
	
	public List<StepSummary> fromDataSummaryQueryResult(Result<Record> result, CollectSurvey survey) {
		List<StepSummary> summaries = new ArrayList<StepSummary>(result.size());
		for (Record record : result) {
			summaries.add(fromDataSummaryQueryRecord(record, survey));
		}
		return summaries;
	}

	@SuppressWarnings("unchecked")
	public StepSummary fromDataSummaryQueryRecord(Record r, CollectSurvey survey) {
		Step step = Step.valueOf(r.getValue(OFC_RECORD_DATA.STEP));
		StepSummary s = new StepSummary(step);

		s.setSequenceNumber(r.getValue(OFC_RECORD_DATA.SEQ_NUM));
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
		
		s.setRootEntityKeyValues(getFieldValues(r, rootEntityDef.getKeyAttributeDefinitions(), RECORD_DATA_KEY_FIELDS, String.class));
		s.setEntityCounts(getFieldValues(r, schema.getCountableEntitiesInRecordList(rootEntityDef), RECORD_DATA_COUNT_FIELDS, Integer.class));
		s.setQualifierValues(getFieldValues(r, schema.getQualifierAttributeDefinitions(rootEntityDef), RECORD_DATA_QUALIFIER_FIELDS, String.class));
		s.setSummaryValues(getFieldValues(r, schema.getSummaryAttributeDefinitions(rootEntityDef), RECORD_DATA_SUMMARY_FIELDS, String.class));
		
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
	
	private <T> List<T> getFieldValues(Record r, List<?> defs, TableField[] fields, Class<T> type) {
		List<T> values = new ArrayList<T>(defs.size());
		for (int i = 0; i < defs.size(); i++) {
			@SuppressWarnings("unchecked")
			T value = (T) r.getValue(fields[i]);
			values.add(value);
		}
		return values;
	}
	
	@SuppressWarnings("unchecked")
	private void addValuesToQuery(StoreQuery<?> q, TableField[] fields, List<?> values) {
		for (int i = 0; i < fields.length; i++) {
			q.addValue(fields[i], values.size() > i ? values.get(i) : null);
		}
	}
	
	private void addValuesToMap(Map<Field<?>, Object> map, TableField[] fields, List<?> values) {
		for (int i = 0; i < fields.length; i++) {
			map.put(fields[i], values.size() > i ? values.get(i) : null);
		}
	}
	
	private List<Param<?>> createParamsFromFields(TableField[] fields, List<?> values) {
		List<Param<?>> params = new ArrayList<Param<?>>(fields.length);
		for (int i = 0; i < fields.length; i++) {
			params.add(values.size() <= i ? val(null) : val(values.get(i)));
		}
		return params;
	}

}
