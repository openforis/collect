package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_QUERY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQuery.OFC_DATA_QUERY;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;
import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataQueryDao")
@Transactional
public class DataQueryDao extends DataCleansingItemDao<DataQuery, DataQueryDao.JooqDSLContext> {

	public DataQueryDao() {
		super(DataQueryDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataQuery> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_QUERY)
				.where(OFC_DATA_QUERY.SURVEY_ID.eq(survey.getId()))
				.orderBy(OFC_DATA_QUERY.TITLE);
		
		Result<OfcDataQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dsl().delete(OFC_DATA_QUERY)
			.where(OFC_DATA_QUERY.SURVEY_ID.eq(survey.getId()))
			.execute();
	}
	
	public List<DataQuery> loadByType(DataQueryType type) {
		JooqDSLContext dsl = dsl((CollectSurvey) type.getSurvey());
		Select<OfcDataQueryRecord> select = 
			dsl.selectFrom(OFC_DATA_QUERY)
				.where(OFC_DATA_QUERY.TYPE_ID.eq(type.getId()));
		
		Result<OfcDataQueryRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<Integer, DataQuery> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_QUERY.ID, OFC_DATA_QUERY_ID_SEQ, DataQuery.class, survey);
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
			o.setErrorSeverity(extractSeverity(r));
			o.setModifiedDate(r.getValue(OFC_DATA_QUERY.MODIFIED_DATE));
			o.setDescription(r.getValue(OFC_DATA_QUERY.DESCRIPTION));
			o.setEntityDefinitionId(r.getValue(OFC_DATA_QUERY.ENTITY_ID));
			o.setTitle(r.getValue(OFC_DATA_QUERY.TITLE));
			o.setTypeId(r.getValue(OFC_DATA_QUERY.TYPE_ID));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_QUERY.UUID)));
		}

		private ErrorSeverity extractSeverity(Record r) {
			String severityCode = r.getValue(OFC_DATA_QUERY.SEVERITY);
			ErrorSeverity severity = null;
			if (StringUtils.isNotBlank(severityCode)) {
				severity = ErrorSeverity.fromCode(severityCode);
			}
			return severity;
		}
		
		@Override
		protected void fromObject(DataQuery o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_QUERY.ATTRIBUTE_ID, o.getAttributeDefinitionId());
			q.addValue(OFC_DATA_QUERY.CONDITIONS, o.getConditions());
			q.addValue(OFC_DATA_QUERY.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_QUERY.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_QUERY.ENTITY_ID, o.getEntityDefinitionId());
			q.addValue(OFC_DATA_QUERY.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			ErrorSeverity severity = o.getErrorSeverity();
			String severityCode = severity == null ? null : String.valueOf(severity.getCode());
			q.addValue(OFC_DATA_QUERY.SEVERITY, severityCode);
			q.addValue(OFC_DATA_QUERY.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_QUERY.TITLE, o.getTitle());
			q.addValue(OFC_DATA_QUERY.TYPE_ID, o.getTypeId());
			q.addValue(OFC_DATA_QUERY.UUID, o.getUuid().toString());
		}

	}
}

