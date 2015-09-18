package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_QUERY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQuery.OFC_DATA_ERROR_QUERY;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorType.OFC_DATA_ERROR_TYPE;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataErrorQuery.Severity;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorQueryRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataErrorQueryDao")
@Transactional
public class DataErrorQueryDao extends SurveyObjectMappingJooqDaoSupport<DataErrorQuery, DataErrorQueryDao.JooqDSLContext> {

	public DataErrorQueryDao() {
		super(DataErrorQueryDao.JooqDSLContext.class);
	}
	
	public static Select<Record1<Integer>> createErrorTypeIdsSelect(DSLContext dsl, CollectSurvey survey) {
		Select<Record1<Integer>> select = 
			dsl.select(OFC_DATA_ERROR_TYPE.ID)
				.from(OFC_DATA_ERROR_TYPE)
				.where(OFC_DATA_ERROR_TYPE.SURVEY_ID.eq(survey.getId()));
		return select;
	}

	@Override
	public List<DataErrorQuery> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataErrorQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY)
				.where(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID.in(createErrorTypeIdsSelect(dsl, survey)));
		
		Result<OfcDataErrorQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	public List<DataErrorQuery> loadByQuery(DataQuery query) {
		JooqDSLContext dsl = dsl((CollectSurvey) query.getSurvey());
		Select<OfcDataErrorQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY)
				.where(OFC_DATA_ERROR_QUERY.QUERY_ID.eq(query.getId()));
		
		Result<OfcDataErrorQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public List<DataErrorQuery> loadByType(DataErrorType errorType) {
		JooqDSLContext dsl = dsl((CollectSurvey) errorType.getSurvey());
		Select<OfcDataErrorQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY)
				.where(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID.eq(errorType.getId()));
		
		Result<OfcDataErrorQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataErrorQuery> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_QUERY.ID, OFC_DATA_ERROR_QUERY_ID_SEQ, DataErrorQuery.class, survey);
		}
		
		@Override
		protected DataErrorQuery newEntity() {
			return new DataErrorQuery(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorQuery o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_ERROR_QUERY.CREATION_DATE));
			o.setModifiedDate(r.getValue(OFC_DATA_ERROR_QUERY.MODIFIED_DATE));
			o.setQueryId(r.getValue(OFC_DATA_ERROR_QUERY.QUERY_ID));
			o.setSeverity(Severity.fromCode(r.getValue(OFC_DATA_ERROR_QUERY.SEVERITY)));
			o.setTypeId(r.getValue(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_ERROR_QUERY.UUID)));
		}
		
		@Override
		protected void fromObject(DataErrorQuery o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_QUERY.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID, o.getTypeId());
			q.addValue(OFC_DATA_ERROR_QUERY.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_ERROR_QUERY.QUERY_ID, o.getQueryId());
			q.addValue(OFC_DATA_ERROR_QUERY.SEVERITY, String.valueOf(o.getSeverity().getCode()));
			q.addValue(OFC_DATA_ERROR_QUERY.UUID, o.getUuid().toString());
		}

	}

}

