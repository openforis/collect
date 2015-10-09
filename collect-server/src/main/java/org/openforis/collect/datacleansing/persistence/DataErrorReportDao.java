package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_REPORT_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQueryGroup.OFC_DATA_ERROR_QUERY_GROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorReport.OFC_DATA_ERROR_REPORT;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorReportRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataErrorReportDao")
@Transactional
public class DataErrorReportDao extends SurveyObjectMappingJooqDaoSupport<DataErrorReport, DataErrorReportDao.JooqDSLContext> {

	public DataErrorReportDao() {
		super(DataErrorReportDao.JooqDSLContext.class);
	}

	public List<DataErrorReport> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<Record> select = dsl
			.select(OFC_DATA_ERROR_REPORT.fields())
			.from(OFC_DATA_ERROR_REPORT)
			.join(OFC_DATA_ERROR_QUERY_GROUP)
				.on(OFC_DATA_ERROR_QUERY_GROUP.ID.eq(OFC_DATA_ERROR_REPORT.QUERY_GROUP_ID))
			.where(OFC_DATA_ERROR_QUERY_GROUP.SURVEY_ID.eq(survey.getId())
		);
		return dsl.fromResult(select.fetch());
	}

	public List<DataErrorReport> loadByQueryGroup(DataErrorQueryGroup queryGroup) {
		CollectSurvey survey = (CollectSurvey) queryGroup.getSurvey();
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataErrorReportRecord> select = dsl
			.selectFrom(OFC_DATA_ERROR_REPORT)
			.where(OFC_DATA_ERROR_REPORT.QUERY_GROUP_ID
				.eq(queryGroup.getId())
			);
		Result<OfcDataErrorReportRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataErrorReport> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_REPORT.ID, OFC_DATA_ERROR_REPORT_ID_SEQ, DataErrorReport.class, survey);
		}
		
		@Override
		protected DataErrorReport newEntity() {
			return new DataErrorReport(survey);
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorReport o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_ERROR_REPORT.CREATION_DATE));
			o.setDatasetSize(r.getValue(OFC_DATA_ERROR_REPORT.DATASET_SIZE));
			o.setLastRecordModifiedDate(r.getValue(OFC_DATA_ERROR_REPORT.LAST_RECORD_MODIFIED_DATE));
			o.setQueryGroupId(r.getValue(OFC_DATA_ERROR_REPORT.QUERY_GROUP_ID));
			o.setRecordStep(Step.valueOf(r.getValue(OFC_DATA_ERROR_REPORT.RECORD_STEP)));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_ERROR_REPORT.UUID)));
		}
		
		@Override
		protected void fromObject(DataErrorReport o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_REPORT.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_REPORT.DATASET_SIZE, o.getDatasetSize());
			q.addValue(OFC_DATA_ERROR_REPORT.LAST_RECORD_MODIFIED_DATE, toTimestamp(o.getLastRecordModifiedDate()));
			q.addValue(OFC_DATA_ERROR_REPORT.QUERY_GROUP_ID, o.getQueryGroupId());
			q.addValue(OFC_DATA_ERROR_REPORT.RECORD_STEP, o.getRecordStep().getStepNumber());
			q.addValue(OFC_DATA_ERROR_REPORT.UUID, o.getUuid().toString());
		}

	}
}

