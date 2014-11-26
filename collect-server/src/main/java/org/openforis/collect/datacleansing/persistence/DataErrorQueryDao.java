package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_QUERY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQuery.OFC_DATA_ERROR_QUERY;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorType.OFC_DATA_ERROR_TYPE;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.ErrorQuery;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorQueryRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class DataErrorQueryDao extends SurveyObjectMappingJooqDaoSupport<ErrorQuery, DataErrorQueryDao.JooqDSLContext> {

	public DataErrorQueryDao() {
		super(DataErrorQueryDao.JooqDSLContext.class);
	}

	public ErrorQuery loadById(CollectSurvey survey, int id) {
		JooqDSLContext jf = dsl(survey);
		ResultQuery<?> selectQuery = jf.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		return r == null ? null : jf.fromRecord(r);
	}
	
	public List<ErrorQuery> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<Record1<Integer>> errorQueryIdsSelect = 
			dsl.select(OFC_DATA_ERROR_TYPE.ID)
				.from(OFC_DATA_ERROR_TYPE)
				.where(OFC_DATA_ERROR_TYPE.SURVEY_ID.eq(survey.getId()));
		
		Select<OfcDataErrorQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY)
				.where(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID.in(errorQueryIdsSelect));
		
		Result<OfcDataErrorQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<ErrorQuery> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_QUERY.ID, OFC_DATA_ERROR_QUERY_ID_SEQ, ErrorQuery.class, survey);
		}
		
		@Override
		protected ErrorQuery newEntity() {
			return new ErrorQuery(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, ErrorQuery object) {
			object.setAttributeDefinitionId(r.getValue(OFC_DATA_ERROR_QUERY.ATTRIBUTE_ID));
			object.setConditions(r.getValue(OFC_DATA_ERROR_QUERY.CONDITIONS));
			object.setCreationDate(r.getValue(OFC_DATA_ERROR_QUERY.CREATION_DATE));
			object.setDescription(r.getValue(OFC_DATA_ERROR_QUERY.DESCRIPTION));
			object.setEntityDefinitionId(r.getValue(OFC_DATA_ERROR_QUERY.ENTITY_ID));
			Integer stepNumber = r.getValue(OFC_DATA_ERROR_QUERY.RECORD_STEP);
			object.setStep(stepNumber == null ? null: Step.valueOf(stepNumber));
			object.setTitle(r.getValue(OFC_DATA_ERROR_QUERY.TITLE));
			object.setTypeId(r.getValue(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID));
		}
		
		@Override
		protected void fromObject(ErrorQuery o, StoreQuery<?> q) {
			q.addValue(OFC_DATA_ERROR_QUERY.ATTRIBUTE_ID, o.getAttributeDefinitionId());
			q.addValue(OFC_DATA_ERROR_QUERY.CONDITIONS, o.getConditions());
			q.addValue(OFC_DATA_ERROR_QUERY.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_QUERY.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_ERROR_QUERY.ENTITY_ID, o.getEntityDefinitionId());
			q.addValue(OFC_DATA_ERROR_QUERY.ERROR_TYPE_ID, o.getTypeId());
			q.addValue(OFC_DATA_ERROR_QUERY.RECORD_STEP, o.getStep() == null ? null: o.getStep().getStepNumber());
			q.addValue(OFC_DATA_ERROR_QUERY.TITLE, o.getTitle());
		}

		@Override
		protected void setId(ErrorQuery entity, int id) {
			entity.setId(id);
		}

		@Override
		protected Integer getId(ErrorQuery entity) {
			return entity.getId();
		}
		
	}
}

