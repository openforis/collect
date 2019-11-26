package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_QUERY_TYPE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQueryType.OFC_DATA_QUERY_TYPE;

import java.util.List;
import java.util.UUID;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryTypeRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataQueryTypeDao")
@Transactional
public class DataQueryTypeDao extends DataCleansingItemDao<DataQueryType, DataQueryTypeDao.JooqDSLContext> {

	public DataQueryTypeDao() {
		super(DataQueryTypeDao.JooqDSLContext.class);
	}
	
	public List<DataQueryType> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataQueryTypeRecord> select = dsl
			.selectFrom(OFC_DATA_QUERY_TYPE)
			.where(OFC_DATA_QUERY_TYPE.SURVEY_ID.eq(survey.getId()))
			.orderBy(OFC_DATA_QUERY_TYPE.CODE);
		Result<OfcDataQueryTypeRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dsl().delete(OFC_DATA_QUERY_TYPE)
			.where(OFC_DATA_QUERY_TYPE.SURVEY_ID.eq(survey.getId()))
			.execute();
	}

	public DataQueryType loadByCode(CollectSurvey survey, String code) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataQueryTypeRecord> select = dsl.selectFrom(OFC_DATA_QUERY_TYPE)
			.where(OFC_DATA_QUERY_TYPE.SURVEY_ID.eq(survey.getId()), 
					OFC_DATA_QUERY_TYPE.CODE.eq(code));
		OfcDataQueryTypeRecord record = select.fetchOne();
		return record == null ? null: dsl.fromRecord(record);
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<Integer, DataQueryType> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_QUERY_TYPE.ID, OFC_DATA_QUERY_TYPE_ID_SEQ, DataQueryType.class, survey);
		}
		
		@Override
		protected DataQueryType newEntity() {
			return new DataQueryType(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataQueryType o) {
			super.fromRecord(r, o);
			o.setCode(r.getValue(OFC_DATA_QUERY_TYPE.CODE));
			o.setCreationDate(r.getValue(OFC_DATA_QUERY_TYPE.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_QUERY_TYPE.DESCRIPTION));
			o.setLabel(r.getValue(OFC_DATA_QUERY_TYPE.LABEL));
			o.setModifiedDate(r.getValue(OFC_DATA_QUERY_TYPE.MODIFIED_DATE));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_QUERY_TYPE.UUID)));
		}
		
		@Override
		protected void fromObject(DataQueryType o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_QUERY_TYPE.CODE, o.getCode());
			q.addValue(OFC_DATA_QUERY_TYPE.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_QUERY_TYPE.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_QUERY_TYPE.LABEL, o.getLabel());
			q.addValue(OFC_DATA_QUERY_TYPE.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_QUERY_TYPE.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_QUERY_TYPE.UUID, o.getUuid().toString());
		}

	}
}
