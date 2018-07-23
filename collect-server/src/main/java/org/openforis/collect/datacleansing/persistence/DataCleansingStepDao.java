package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_CLEANSING_STEP_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStepValue.OFC_DATA_CLEANSING_STEP_VALUE;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQuery.OFC_DATA_QUERY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStep.DataCleansingStepType;
import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataCleansingStepDao")
@Transactional
public class DataCleansingStepDao extends SurveyObjectMappingJooqDaoSupport<DataCleansingStep, DataCleansingStepDao.JooqDSLContext> {

	private static final TableField<?, ?>[] FIELD_FIX_EXPRESSION_FIELDS = new TableField<?, ?>[] {
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION1, 
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION2,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION3,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION4,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION5,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION6
	};
	
	public DataCleansingStepDao() {
		super(DataCleansingStepDao.JooqDSLContext.class);
	}
	
	public static Select<Record1<Integer>> createQueryIdsSelect(DSLContext dsl, CollectSurvey survey) {
		Select<Record1<Integer>> select = 
			dsl.select(OFC_DATA_QUERY.ID)
				.from(OFC_DATA_QUERY)
				.where(OFC_DATA_QUERY.SURVEY_ID.eq(survey.getId()));
		return select;
	}
	
	@Override
	public List<DataCleansingStep> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataCleansingStepRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_STEP)
				.where(OFC_DATA_CLEANSING_STEP.QUERY_ID.in(createQueryIdsSelect(dsl, survey)))
				.orderBy(OFC_DATA_CLEANSING_STEP.TITLE);
		
		Result<OfcDataCleansingStepRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl();
		dsl.delete(OFC_DATA_CLEANSING_STEP)
			.where(OFC_DATA_CLEANSING_STEP.QUERY_ID.in(createQueryIdsSelect(dsl, survey)))
			.execute();
	}
	
	public List<DataCleansingStep> loadByQuery(DataQuery query) {
		JooqDSLContext dsl = dsl((CollectSurvey) query.getSurvey());
		Select<OfcDataCleansingStepRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_STEP)
				.where(OFC_DATA_CLEANSING_STEP.QUERY_ID.eq(query.getId()));
		
		Result<OfcDataCleansingStepRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	public void insertStepValues(int stepId, List<DataCleansingStepValue> values) {
		JooqDSLContext dsl = dsl();
		List<Field<?>> fields = new ArrayList<Field<?>>(Arrays.<Field<?>>asList(
				OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID,
				OFC_DATA_CLEANSING_STEP_VALUE.SORT_ORDER,
				OFC_DATA_CLEANSING_STEP_VALUE.TYPE,
				OFC_DATA_CLEANSING_STEP_VALUE.CONDITION,
				OFC_DATA_CLEANSING_STEP_VALUE.FIX_EXPRESSION
		));
		fields.addAll(Arrays.asList(FIELD_FIX_EXPRESSION_FIELDS));
		InsertValuesStepN<OfcDataCleansingStepValueRecord> insert = 
				dsl.insertInto(OFC_DATA_CLEANSING_STEP_VALUE, fields)
				.values(Collections.nCopies(fields.size(), "?")); //add ? placeholders
		BatchBindStep batch = dsl.batch(insert);

		int stepIndex = 0;
		for (DataCleansingStepValue stepValue : values) {
			List<Object> insertValues = new ArrayList<Object>(Arrays.<Object>asList(
				stepId,
				stepIndex + 1,
				String.valueOf(stepValue.getUpdateType().getCode()),
				stepValue.getCondition(),
				stepValue.getFixExpression()
			));
			List<String> fieldFixExpressions = getFieldFixExpressionValues(stepValue);
			insertValues.addAll(fieldFixExpressions);
			
			batch.bind(insertValues.toArray(new Object[insertValues.size()]));
			stepIndex ++;
		}
		batch.execute();
	}

	private List<String> getFieldFixExpressionValues(DataCleansingStepValue stepValue) {
		List<String> fieldFixExpressions = new ArrayList<String>(FIELD_FIX_EXPRESSION_FIELDS.length);
		for (int i = 0; i < FIELD_FIX_EXPRESSION_FIELDS.length; i++) {
			String fieldFixExpression = null;
			if (stepValue.getUpdateType() == UpdateType.FIELD) {
				List<String> stepValueFieldFixExpressions = stepValue.getFieldFixExpressions();
				if (i < stepValueFieldFixExpressions.size()) {
					fieldFixExpression = stepValueFieldFixExpressions.get(i);
				}
			}
			fieldFixExpressions.add(fieldFixExpression);
		}
		return fieldFixExpressions;
	}
	
	public void deleteStepValues(int stepId) {
		dsl().delete(OFC_DATA_CLEANSING_STEP_VALUE)
			.where(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID.eq(stepId))
			.execute();
	}
	
	public void deleteStepValues(CollectSurvey survey) {
		JooqDSLContext dsl = dsl();
		dsl.delete(OFC_DATA_CLEANSING_STEP_VALUE)
			.where(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID.in(
					dsl.select(OFC_DATA_CLEANSING_STEP.ID)
						.from(OFC_DATA_CLEANSING_STEP)
						.where(OFC_DATA_CLEANSING_STEP.QUERY_ID.in(createQueryIdsSelect(dsl, survey)))
					)
			)
			.execute();
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataCleansingStep> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_CLEANSING_STEP.ID, OFC_DATA_CLEANSING_STEP_ID_SEQ, DataCleansingStep.class, survey);
		}
		
		@Override
		protected DataCleansingStep newEntity() {
			return new DataCleansingStep(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataCleansingStep o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_CLEANSING_STEP.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_CLEANSING_STEP.DESCRIPTION));
			o.setModifiedDate(r.getValue(OFC_DATA_CLEANSING_STEP.MODIFIED_DATE));
			o.setQueryId(r.getValue(OFC_DATA_CLEANSING_STEP.QUERY_ID));
			o.setTitle(r.getValue(OFC_DATA_CLEANSING_STEP.TITLE));
			o.setType(DataCleansingStepType.fromCode(r.getValue(OFC_DATA_CLEANSING_STEP.TYPE).charAt(0)));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_CLEANSING_STEP.UUID)));
			
			if (o.getType() == DataCleansingStepType.ATTRIBUTE_UPDATE) {
				o.setUpdateValues(loadValues(o));
			}
		}
		
		@Override
		protected void fromObject(DataCleansingStep o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_CLEANSING_STEP.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_CLEANSING_STEP.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_CLEANSING_STEP.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_CLEANSING_STEP.QUERY_ID, o.getQueryId());
			q.addValue(OFC_DATA_CLEANSING_STEP.TITLE, o.getTitle());
			q.addValue(OFC_DATA_CLEANSING_STEP.TYPE, String.valueOf(o.getType().getCode()));
			q.addValue(OFC_DATA_CLEANSING_STEP.UUID, o.getUuid().toString());
		}

		private List<DataCleansingStepValue> loadValues(DataCleansingStep step) {
			Select<OfcDataCleansingStepValueRecord> select = 
				selectFrom(OFC_DATA_CLEANSING_STEP_VALUE)
					.where(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID.eq(step.getId()))
					.orderBy(OFC_DATA_CLEANSING_STEP_VALUE.SORT_ORDER);
			
			Result<OfcDataCleansingStepValueRecord> result = select.fetch();
			return toStepValues(result, step);
		}

		private List<DataCleansingStepValue> toStepValues(Result<OfcDataCleansingStepValueRecord> result,
				DataCleansingStep step) {
			List<DataCleansingStepValue> values = new ArrayList<DataCleansingStepValue>(result.size());
			for (OfcDataCleansingStepValueRecord record : result) {
				DataCleansingStepValue v = new DataCleansingStepValue();
				String typeVal = record.getValue(OFC_DATA_CLEANSING_STEP_VALUE.TYPE);
				v.setUpdateType(UpdateType.fromCode(typeVal));
				v.setCondition(record.getValue(OFC_DATA_CLEANSING_STEP_VALUE.CONDITION));
				v.setFixExpression(record.getValue(OFC_DATA_CLEANSING_STEP_VALUE.FIX_EXPRESSION));
				if (v.getUpdateType() == UpdateType.FIELD) {
					List<String> fieldFixExpressions = new ArrayList<String>(FIELD_FIX_EXPRESSION_FIELDS.length);
					for (int i = 0; i < FIELD_FIX_EXPRESSION_FIELDS.length; i++) {
						@SuppressWarnings("unchecked")
						TableField<?, String> tableField = (TableField<?, String>) FIELD_FIX_EXPRESSION_FIELDS[i];
						fieldFixExpressions.add(record.getValue(tableField));
					}
					v.setFieldFixExpressions(fieldFixExpressions);
				}
				values.add(v);
			}
			return values;
		}
	}
}

