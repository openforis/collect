package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_TYPE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorType.OFC_DATA_ERROR_TYPE;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorTypeRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataErrorTypeDao")
@Transactional
public class DataErrorTypeDao extends SurveyObjectMappingJooqDaoSupport<DataErrorType, DataErrorTypeDao.JooqDSLContext> {

	public DataErrorTypeDao() {
		super(DataErrorTypeDao.JooqDSLContext.class);
	}
	
	public List<DataErrorType> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataErrorTypeRecord> select = dsl.selectFrom(OFC_DATA_ERROR_TYPE)
			.where(OFC_DATA_ERROR_TYPE.SURVEY_ID.eq(survey.getId()));
		Result<OfcDataErrorTypeRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public DataErrorType loadByCode(CollectSurvey survey, String code) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataErrorTypeRecord> select = dsl.selectFrom(OFC_DATA_ERROR_TYPE)
			.where(OFC_DATA_ERROR_TYPE.SURVEY_ID.eq(survey.getId()), 
					OFC_DATA_ERROR_TYPE.CODE.eq(code));
		OfcDataErrorTypeRecord record = select.fetchOne();
		return record == null ? null: dsl.fromRecord(record);
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataErrorType> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_TYPE.ID, OFC_DATA_ERROR_TYPE_ID_SEQ, DataErrorType.class, survey);
		}
		
		@Override
		protected DataErrorType newEntity() {
			return new DataErrorType(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorType o) {
			super.fromRecord(r, o);
			o.setCode(r.getValue(OFC_DATA_ERROR_TYPE.CODE));
			o.setCreationDate(r.getValue(OFC_DATA_ERROR_TYPE.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_ERROR_TYPE.DESCRIPTION));
			o.setLabel(r.getValue(OFC_DATA_ERROR_TYPE.LABEL));
			o.setModifiedDate(r.getValue(OFC_DATA_ERROR_TYPE.MODIFIED_DATE));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_ERROR_TYPE.UUID)));
		}
		
		@Override
		protected void fromObject(DataErrorType o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_TYPE.CODE, o.getCode());
			q.addValue(OFC_DATA_ERROR_TYPE.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_TYPE.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_ERROR_TYPE.LABEL, o.getLabel());
			q.addValue(OFC_DATA_ERROR_TYPE.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_ERROR_TYPE.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_ERROR_TYPE.UUID, o.getUuid().toString());
		}

	}
}
