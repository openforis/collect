package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_REPORT_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQueryGroup.OFC_DATA_QUERY_GROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataReport.OFC_DATA_REPORT;

import java.util.List;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataReport;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.OfcDataReport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataReportRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataReportDao")
@Transactional
public class DataReportDao extends DataCleansingItemDao<DataReport, DataReportDao.JooqDSLContext> {

	public DataReportDao() {
		super(DataReportDao.JooqDSLContext.class);
	}

	public List<DataReport> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<Record> select = dsl
			.select(OFC_DATA_REPORT.fields())
			.from(OFC_DATA_REPORT)
			.join(OFC_DATA_QUERY_GROUP)
				.on(OFC_DATA_QUERY_GROUP.ID.eq(OFC_DATA_REPORT.QUERY_GROUP_ID))
			.where(OFC_DATA_QUERY_GROUP.SURVEY_ID.eq(survey.getId())
		);
		return dsl.fromResult(select.fetch());
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl();
		dsl.delete(OFC_DATA_REPORT)
			.where(OFC_DATA_REPORT.QUERY_GROUP_ID.in(
					DataQueryGroupDao.createDataQueryGroupIdBySurveyQuery(dsl, survey)
				)
			)
			.execute();
	}

	public List<DataReport> loadByQueryGroup(DataQueryGroup queryGroup) {
		CollectSurvey survey = (CollectSurvey) queryGroup.getSurvey();
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataReportRecord> select = dsl
			.selectFrom(OFC_DATA_REPORT)
			.where(OFC_DATA_REPORT.QUERY_GROUP_ID
				.eq(queryGroup.getId())
			);
		Result<OfcDataReportRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	protected static SelectConditionStep<Record1<Integer>> createDataReportIdsBySurveyQuery(
			CollectDSLContext dsl, CollectSurvey survey) {
		return dsl.select(OfcDataReport.OFC_DATA_REPORT.ID)
				.from(OfcDataReport.OFC_DATA_REPORT)
				.where(OfcDataReport.OFC_DATA_REPORT.QUERY_GROUP_ID.in(
						DataQueryGroupDao.createDataQueryGroupIdBySurveyQuery(dsl, survey)
					)
				);
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<Integer, DataReport> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_REPORT.ID, OFC_DATA_REPORT_ID_SEQ, DataReport.class, survey);
		}
		
		@Override
		protected DataReport newEntity() {
			return new DataReport(survey);
		}
		
		@Override
		protected void fromRecord(Record r, DataReport o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_REPORT.CREATION_DATE));
			o.setDatasetSize(r.getValue(OFC_DATA_REPORT.DATASET_SIZE));
			o.setLastRecordModifiedDate(r.getValue(OFC_DATA_REPORT.LAST_RECORD_MODIFIED_DATE));
			o.setQueryGroupId(r.getValue(OFC_DATA_REPORT.QUERY_GROUP_ID));
			o.setRecordStep(Step.valueOf(r.getValue(OFC_DATA_REPORT.RECORD_STEP)));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_REPORT.UUID)));
		}
		
		@Override
		protected void fromObject(DataReport o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_REPORT.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_REPORT.DATASET_SIZE, o.getDatasetSize());
			q.addValue(OFC_DATA_REPORT.LAST_RECORD_MODIFIED_DATE, toTimestamp(o.getLastRecordModifiedDate()));
			q.addValue(OFC_DATA_REPORT.QUERY_GROUP_ID, o.getQueryGroupId());
			q.addValue(OFC_DATA_REPORT.RECORD_STEP, o.getRecordStep().getStepNumber());
			q.addValue(OFC_DATA_REPORT.UUID, o.getUuid().toString());
		}

	}
}

