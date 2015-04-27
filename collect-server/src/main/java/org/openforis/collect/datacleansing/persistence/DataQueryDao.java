package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_QUERY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQuery.OFC_DATA_QUERY;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataQueryDao")
@Transactional
public class DataQueryDao extends SurveyObjectMappingJooqDaoSupport<DataQuery, DataQueryDao.JooqDSLContext> {

	public DataQueryDao() {
		super(DataQueryDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataQuery> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_QUERY)
				.where(OFC_DATA_QUERY.SURVEY_ID.eq(survey.getId()));
		
		Result<OfcDataQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataQuery> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_QUERY.ID, OFC_DATA_QUERY_ID_SEQ, DataQuery.class, survey);
		}
		
		@Override
		protected DataQuery newEntity() {
			return new DataQuery(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataQuery o) {
			super.fromRecord(r, o);
			o.setAttributeDefinitionId(r.getValue(OFC_DATA_QUERY.ATTRIBUTE_ID));
			o.setConditions(r.getValue(OFC_DATA_QUERY.CONDITIONS));
			o.setCreationDate(r.getValue(OFC_DATA_QUERY.CREATION_DATE));
			o.setModifiedDate(r.getValue(OFC_DATA_QUERY.MODIFIED_DATE));
			o.setDescription(r.getValue(OFC_DATA_QUERY.DESCRIPTION));
			o.setEntityDefinitionId(r.getValue(OFC_DATA_QUERY.ENTITY_ID));
			o.setTitle(r.getValue(OFC_DATA_QUERY.TITLE));
		}
		
		@Override
		protected void fromObject(DataQuery o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_QUERY.ATTRIBUTE_ID, o.getAttributeDefinitionId());
			q.addValue(OFC_DATA_QUERY.CONDITIONS, o.getConditions());
			q.addValue(OFC_DATA_QUERY.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_QUERY.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_QUERY.ENTITY_ID, o.getEntityDefinitionId());
			q.addValue(OFC_DATA_QUERY.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_QUERY.TITLE, o.getTitle());
		}

	}
}

