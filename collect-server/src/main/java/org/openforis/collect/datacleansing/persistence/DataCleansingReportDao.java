package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_CLEANSING_REPORT_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingReport.OFC_DATA_CLEANSING_REPORT;

import java.util.List;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingReport;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingReportRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataCleansingReportDao")
@Transactional
public class DataCleansingReportDao extends SurveyObjectMappingJooqDaoSupport<DataCleansingReport, DataCleansingReportDao.JooqDSLContext> {

	public DataCleansingReportDao() {
		super(DataCleansingReportDao.JooqDSLContext.class);
	}

	public List<DataCleansingReport> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<Record> select = dsl
			.select(OFC_DATA_CLEANSING_REPORT.fields())
			.from(OFC_DATA_CLEANSING_REPORT)
			.join(OFC_DATA_CLEANSING_CHAIN)
				.on(OFC_DATA_CLEANSING_CHAIN.ID.eq(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID))
			.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId())
		);
		return dsl.fromResult(select.fetch());
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dsl().delete(OFC_DATA_CLEANSING_REPORT)
			.where(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID.in(
					dsl().select(OFC_DATA_CLEANSING_CHAIN.ID)
						.from(OFC_DATA_CLEANSING_CHAIN)
						.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId()))
			)
		).execute();
	}

	public List<DataCleansingReport> loadByCleansingChain(DataCleansingChain cleansingChain) {
		CollectSurvey survey = (CollectSurvey) cleansingChain.getSurvey();
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataCleansingReportRecord> select = dsl
			.selectFrom(OFC_DATA_CLEANSING_REPORT)
			.where(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID
				.eq(cleansingChain.getId()))
			.orderBy(OFC_DATA_CLEANSING_REPORT.CREATION_DATE);
		Result<OfcDataCleansingReportRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public DataCleansingReport loadLastReport(DataCleansingChain cleansingChain) {
		CollectSurvey survey = (CollectSurvey) cleansingChain.getSurvey();
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataCleansingReportRecord> select = dsl
			.selectFrom(OFC_DATA_CLEANSING_REPORT)
			.where(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID
				.eq(cleansingChain.getId()))
			.orderBy(OFC_DATA_CLEANSING_REPORT.CREATION_DATE.desc());
		OfcDataCleansingReportRecord record = select.fetchAny();
		return record == null ? null : dsl.fromRecord(record);
	}
	
	public void deleteByCleansingChain(DataCleansingChain chain) {
		CollectSurvey survey = (CollectSurvey) chain.getSurvey();
		JooqDSLContext dsl = dsl(survey);
		dsl.deleteFrom(OFC_DATA_CLEANSING_REPORT)
			.where(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID.eq(chain.getId()))
			.execute();
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataCleansingReport> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_CLEANSING_REPORT.ID, OFC_DATA_CLEANSING_REPORT_ID_SEQ, DataCleansingReport.class, survey);
		}
		
		@Override
		protected DataCleansingReport newEntity() {
			return new DataCleansingReport(survey);
		}
		
		@Override
		protected void fromRecord(Record r, DataCleansingReport o) {
			super.fromRecord(r, o);
			o.setCleansedNodes(r.getValue(OFC_DATA_CLEANSING_REPORT.CLEANSED_NODES));
			o.setCleansedRecords(r.getValue(OFC_DATA_CLEANSING_REPORT.CLEANSED_RECORDS));
			o.setCleansingChainId(r.getValue(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID));
			o.setCreationDate(r.getValue(OFC_DATA_CLEANSING_REPORT.CREATION_DATE));
			o.setDatasetSize(r.getValue(OFC_DATA_CLEANSING_REPORT.DATASET_SIZE));
			o.setLastRecordModifiedDate(r.getValue(OFC_DATA_CLEANSING_REPORT.LAST_RECORD_MODIFIED_DATE));
			o.setRecordStep(Step.valueOf(r.getValue(OFC_DATA_CLEANSING_REPORT.RECORD_STEP)));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_CLEANSING_REPORT.UUID)));
		}
		
		@Override
		protected void fromObject(DataCleansingReport o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_CLEANSING_REPORT.CLEANSED_NODES, o.getCleansedNodes());
			q.addValue(OFC_DATA_CLEANSING_REPORT.CLEANSED_RECORDS, o.getCleansedRecords());
			q.addValue(OFC_DATA_CLEANSING_REPORT.CLEANSING_CHAIN_ID, o.getCleansingChainId());
			q.addValue(OFC_DATA_CLEANSING_REPORT.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_CLEANSING_REPORT.DATASET_SIZE, o.getDatasetSize());
			q.addValue(OFC_DATA_CLEANSING_REPORT.LAST_RECORD_MODIFIED_DATE, toTimestamp(o.getLastRecordModifiedDate()));
			q.addValue(OFC_DATA_CLEANSING_REPORT.RECORD_STEP, o.getRecordStep().getStepNumber());
			q.addValue(OFC_DATA_CLEANSING_REPORT.UUID, o.getUuid().toString());
		}

	}
}

