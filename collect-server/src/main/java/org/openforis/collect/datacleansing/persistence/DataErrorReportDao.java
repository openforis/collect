package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_REPORT_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQuery.OFC_DATA_ERROR_QUERY;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorReport.OFC_DATA_ERROR_REPORT;

import java.sql.Connection;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.PersistedObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorReportRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class DataErrorReportDao extends MappingJooqDaoSupport<DataErrorReport, DataErrorReportDao.JooqDSLContext> {

	public DataErrorReportDao() {
		super(DataErrorReportDao.JooqDSLContext.class);
	}

	public static Select<Record1<Integer>> createErrorQueryIdsSelect(DSLContext dsl, CollectSurvey survey) {
		Select<Record1<Integer>> select = dsl
			.select(OFC_DATA_ERROR_QUERY.ID)
			.from(OFC_DATA_ERROR_QUERY)
			.where(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID
				.in(DataErrorQueryDao.createErrorTypeIdsSelect(dsl, survey))
			);
		return select;
	}

	public List<DataErrorReport> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl();
		Select<OfcDataErrorReportRecord> select = dsl
			.selectFrom(OFC_DATA_ERROR_REPORT)
			.where(OFC_DATA_ERROR_REPORT.QUERY_ID
				.in(createErrorQueryIdsSelect(dsl, survey))
			);
		Result<OfcDataErrorReportRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public List<DataErrorReport> loadByQuery(DataErrorQuery query) {
		JooqDSLContext dsl = dsl();
		Select<OfcDataErrorReportRecord> select = dsl
			.selectFrom(OFC_DATA_ERROR_REPORT)
			.where(OFC_DATA_ERROR_REPORT.QUERY_ID
				.eq(query.getId())
			);
		Result<OfcDataErrorReportRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	protected static class JooqDSLContext extends PersistedObjectMappingDSLContext<DataErrorReport> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			super(connection, OFC_DATA_ERROR_REPORT.ID, OFC_DATA_ERROR_REPORT_ID_SEQ, DataErrorReport.class);
		}
		
		@Override
		protected DataErrorReport newEntity() {
			return new DataErrorReport();
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorReport o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_ERROR_REPORT.CREATION_DATE));
			o.setQueryId(r.getValue(OFC_DATA_ERROR_REPORT.QUERY_ID));
		}
		
		@Override
		protected void fromObject(DataErrorReport o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_REPORT.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_REPORT.QUERY_ID, o.getQueryId());
		}

	}
}

