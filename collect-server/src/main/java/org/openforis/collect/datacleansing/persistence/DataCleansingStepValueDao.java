package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStepValue.OFC_DATA_CLEANSING_STEP_VALUE;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingStepValueRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataCleansingStepValueDao")
@Transactional
public class DataCleansingStepValueDao extends SurveyObjectMappingJooqDaoSupport<DataCleansingStepValue, DataCleansingStepValueDao.JooqDSLContext> {

	private static final TableField<?, ?>[] FIELD_FIX_EXPRESSION_FIELDS = new TableField<?, ?>[] {
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION1, 
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION2,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION3,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION4,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION5,
		OFC_DATA_CLEANSING_STEP_VALUE.FIELD_FIX_EXPRESSION6
	};
	
	public DataCleansingStepValueDao() {
		super(DataCleansingStepValueDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataCleansingStepValue> loadBySurvey(CollectSurvey survey) {
		throw new UnsupportedOperationException();
	}
	
	public List<DataCleansingStepValue> loadByStep(DataCleansingStep step) {
		JooqDSLContext dsl = dsl(step);
		Select<OfcDataCleansingStepValueRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_STEP_VALUE)
				.where(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID.eq(step.getId()))
				.orderBy(OFC_DATA_CLEANSING_STEP_VALUE.SORT_ORDER);
		
		Result<OfcDataCleansingStepValueRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	public void deleteByStep(DataCleansingStep step) {
		JooqDSLContext dsl = dsl(step);
		dsl
			.delete(OFC_DATA_CLEANSING_STEP_VALUE)
			.where(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID.eq(step.getId()))
			.execute();
	}

	private JooqDSLContext dsl(DataCleansingStep step) {
		return new JooqDSLContext(getConnection(), step);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataCleansingStepValue> {

		private static final long serialVersionUID = 1L;
		
		private DataCleansingStep step;
		
		public JooqDSLContext(Connection connection) {
			this(connection, (CollectSurvey) null);
		}
		
		public JooqDSLContext(Connection connection, DataCleansingStep step) {
			this(connection, (CollectSurvey) step.getSurvey());
			this.step = step;
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, null, null, DataCleansingStepValue.class, survey);
		}
		
		@Override
		protected DataCleansingStepValue newEntity() {
			return new DataCleansingStepValue(step);
		}
		
		@Override
		protected void fromRecord(Record r, DataCleansingStepValue o) {
			super.fromRecord(r, o);
			String typeVal = r.getValue(OFC_DATA_CLEANSING_STEP_VALUE.TYPE);
			o.setUpdateType(UpdateType.fromCode(typeVal));
			o.setCondition(r.getValue(OFC_DATA_CLEANSING_STEP_VALUE.CONDITION));
			o.setFixExpression(r.getValue(OFC_DATA_CLEANSING_STEP_VALUE.FIX_EXPRESSION));
			
			if (StringUtils.isBlank(o.getFixExpression())) {
				List<String> fieldFixExpressions = new ArrayList<String>(FIELD_FIX_EXPRESSION_FIELDS.length);
				for (int i = 0; i < FIELD_FIX_EXPRESSION_FIELDS.length; i++) {
					@SuppressWarnings("unchecked")
					TableField<?, String> tableField = (TableField<?, String>) FIELD_FIX_EXPRESSION_FIELDS[i];
					fieldFixExpressions.add(r.getValue(tableField));
				}
				o.setFieldFixExpressions(fieldFixExpressions);
			}
		}
		
		@Override
		protected void fromObject(DataCleansingStepValue o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_CLEANSING_STEP_VALUE.STEP_ID, o.getStep().getId());
			q.addValue(OFC_DATA_CLEANSING_STEP_VALUE.TYPE, String.valueOf(o.getUpdateType().getCode()));
			q.addValue(OFC_DATA_CLEANSING_STEP_VALUE.CONDITION, o.getCondition());
			q.addValue(OFC_DATA_CLEANSING_STEP_VALUE.FIX_EXPRESSION, o.getFixExpression());
			
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

