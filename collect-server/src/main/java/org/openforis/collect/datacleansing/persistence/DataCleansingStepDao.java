package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_CLEANSING_STEP_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQuery.OFC_DATA_QUERY;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataCleansingStepDao")
@Transactional
public class DataCleansingStepDao extends SurveyObjectMappingJooqDaoSupport<DataCleansingStep, DataCleansingStepDao.JooqDSLContext> {

	private static final TableField<?, ?>[] FIELD_FIX_EXPRESSION_FIELDS = new TableField<?, ?>[] {
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION1, 
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION2,
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION3,
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION4,
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION5,
		OFC_DATA_CLEANSING_STEP.FIELD_FIX_EXPRESSION6
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
				.where(OFC_DATA_CLEANSING_STEP.QUERY_ID.in(createQueryIdsSelect(dsl, survey)));
		
		Result<OfcDataCleansingStepRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataCleansingStep> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_CLEANSING_STEP.ID, OFC_DATA_CLEANSING_STEP_ID_SEQ, DataCleansingStep.class, survey);
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
			o.setFixExpression(r.getValue(OFC_DATA_CLEANSING_STEP.FIX_EXPRESSION));
			o.setModifiedDate(r.getValue(OFC_DATA_CLEANSING_STEP.MODIFIED_DATE));
			o.setQueryId(r.getValue(OFC_DATA_CLEANSING_STEP.QUERY_ID));
			o.setTitle(r.getValue(OFC_DATA_CLEANSING_STEP.TITLE));
			
			List<String> fieldFixExpressions = new ArrayList<String>(FIELD_FIX_EXPRESSION_FIELDS.length);
			for (int i = 0; i < FIELD_FIX_EXPRESSION_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> tableField = (TableField<?, String>) FIELD_FIX_EXPRESSION_FIELDS[i];
				String value = r.getValue(tableField);
				fieldFixExpressions.add(value);
			}
			o.setFieldFixExpressions(fieldFixExpressions);
		}
		
		@Override
		protected void fromObject(DataCleansingStep o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_CLEANSING_STEP.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_CLEANSING_STEP.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_CLEANSING_STEP.FIX_EXPRESSION, o.getFixExpression());
			q.addValue(OFC_DATA_CLEANSING_STEP.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_CLEANSING_STEP.QUERY_ID, o.getQueryId());
			q.addValue(OFC_DATA_CLEANSING_STEP.TITLE, o.getTitle());
			
			int fieldFixExpressionsSize = o.getFieldFixExpressions() == null ? 0 : o.getFieldFixExpressions().size();
			for (int i = 0; i < FIELD_FIX_EXPRESSION_FIELDS.length; i++) {
				@SuppressWarnings("unchecked")
				TableField<?, String> tableField = (TableField<?, String>) FIELD_FIX_EXPRESSION_FIELDS[i];
				String value;
				if (i <= fieldFixExpressionsSize - 1) {
					value = o.getFieldFixExpressions().get(i);
				} else {
					value = null;
				}
				q.addValue(tableField, value);
			}
		}

	}
}

