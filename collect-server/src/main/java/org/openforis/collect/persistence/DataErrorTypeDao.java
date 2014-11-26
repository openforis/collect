package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_TYPE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorType.OFC_DATA_ERROR_TYPE;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class DataErrorTypeDao extends SurveyObjectMappingJooqDaoSupport<ErrorType, DataErrorTypeDao.JooqDSLContext> {

	public DataErrorTypeDao() {
		super(DataErrorTypeDao.JooqDSLContext.class);
	}

	public ErrorType loadById(CollectSurvey survey, int id) {
		JooqDSLContext jf = dsl(survey);
		ResultQuery<?> selectQuery = jf.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		return r == null ? null : jf.fromRecord(r);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<ErrorType> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_TYPE.ID, OFC_DATA_ERROR_TYPE_ID_SEQ, ErrorType.class, survey);
		}
		
		@Override
		protected ErrorType newEntity() {
			return new ErrorType(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, ErrorType object) {
			object.setCode(r.getValue(OFC_DATA_ERROR_TYPE.CODE));
			object.setDescription(r.getValue(OFC_DATA_ERROR_TYPE.DESCRIPTION));
			object.setLabel(r.getValue(OFC_DATA_ERROR_TYPE.LABEL));
		}
		
		@Override
		protected void fromObject(ErrorType object, StoreQuery<?> q) {
			q.addValue(OFC_DATA_ERROR_TYPE.CODE, object.getCode());
			q.addValue(OFC_DATA_ERROR_TYPE.DESCRIPTION, object.getDescription());
			q.addValue(OFC_DATA_ERROR_TYPE.LABEL, object.getLabel());
			q.addValue(OFC_DATA_ERROR_TYPE.SURVEY_ID, object.getSurvey().getId());
		}

		@Override
		protected void setId(ErrorType entity, int id) {
			entity.setId(id);
		}

		@Override
		protected Integer getId(ErrorType entity) {
			return entity.getId();
		}
		
	}
}
