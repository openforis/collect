package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_QUERY_GROUP_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQueryGroup.OFC_DATA_QUERY_GROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataQueryGroupQuery.OFC_DATA_QUERY_GROUP_QUERY;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataQueryGroupRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataQueryGroupDao")
@Transactional
public class DataQueryGroupDao extends DataCleansingItemDao<DataQueryGroup, DataQueryGroupDao.JooqDSLContext> {

	public DataQueryGroupDao() {
		super(DataQueryGroupDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataQueryGroup> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataQueryGroupRecord> select = 
			dsl.selectFrom(OFC_DATA_QUERY_GROUP)
				.where(OFC_DATA_QUERY_GROUP.SURVEY_ID.eq(survey.getId()))
				.orderBy(OFC_DATA_QUERY_GROUP.TITLE)
			;
		Result<OfcDataQueryGroupRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dsl().deleteFrom(OFC_DATA_QUERY_GROUP)
			.where(OFC_DATA_QUERY_GROUP.SURVEY_ID.eq(survey.getId()))
			.execute();
	}
	
	public Set<DataQueryGroup> loadGroupsByQuery(DataQuery query) {
		JooqDSLContext dsl = dsl((CollectSurvey) query.getSurvey());
		SelectConditionStep<Record1<Integer>> subselect = dsl
			.select(OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID)
			.from(OFC_DATA_QUERY_GROUP_QUERY)
			.where(OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID.eq(query.getId()));
		
		Select<OfcDataQueryGroupRecord> select = 
			dsl.selectFrom(OFC_DATA_QUERY_GROUP)
				.where(OFC_DATA_QUERY_GROUP.ID.in(subselect));
		
		Result<OfcDataQueryGroupRecord> result = select.fetch();
		return new HashSet<DataQueryGroup>(dsl.fromResult(result));
	}
	
	public List<Integer> loadQueryIds(DataQueryGroup group) {
		JooqDSLContext dsl = dsl((CollectSurvey) group.getSurvey());
		Select<Record1<Integer>> select = 
				dsl.select(OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID)
					.from(OFC_DATA_QUERY_GROUP_QUERY)
					.where(OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID.eq(group.getId()))
					.orderBy(OFC_DATA_QUERY_GROUP_QUERY.SORT_ORDER);
		List<Integer> ids = select.fetch(OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID);
		return ids;
	}
	
	public void deleteQueryAssociations(DataQueryGroup group) {
		JooqDSLContext dsl = dsl((CollectSurvey) group.getSurvey());
		dsl.delete(OFC_DATA_QUERY_GROUP_QUERY)
			.where(OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID.eq(group.getId()))
			.execute();
	}
	
	public void deleteQueryAssociations(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		dsl.delete(OFC_DATA_QUERY_GROUP_QUERY)
			.where(OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID.in(
					DataQueryGroupDao.createDataQueryGroupIdBySurveyQuery(dsl, survey)
				)
			)
			.execute();
	}
	
	public void insertQueryAssociations(DataQueryGroup chain, List<Integer> queryIds) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		BatchBindStep batch = dsl.batch(dsl.insertInto(OFC_DATA_QUERY_GROUP_QUERY,
				OFC_DATA_QUERY_GROUP_QUERY.GROUP_ID, 
				OFC_DATA_QUERY_GROUP_QUERY.QUERY_ID,
				OFC_DATA_QUERY_GROUP_QUERY.SORT_ORDER)
			.values((Integer) null, (Integer) null, (Integer) null));
		int position = 0;
		for (Integer stepId : queryIds) {
			batch.bind(chain.getId(), stepId, position ++);
		}
		batch.execute();
	}
	
	protected static SelectConditionStep<Record1<Integer>> createDataQueryGroupIdBySurveyQuery(
			CollectDSLContext dsl, CollectSurvey survey) {
		return dsl.select(OFC_DATA_QUERY_GROUP.ID)
			.from(OFC_DATA_QUERY_GROUP)
			.where(OFC_DATA_QUERY_GROUP.SURVEY_ID.eq(survey.getId()));
	}

	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<Integer, DataQueryGroup> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_QUERY_GROUP.ID, OFC_DATA_QUERY_GROUP_ID_SEQ, DataQueryGroup.class, survey);
		}
		
		@Override
		protected DataQueryGroup newEntity() {
			return new DataQueryGroup(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataQueryGroup o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_QUERY_GROUP.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_QUERY_GROUP.DESCRIPTION));
			o.setModifiedDate(r.getValue(OFC_DATA_QUERY_GROUP.MODIFIED_DATE));
			o.setTitle(r.getValue(OFC_DATA_QUERY_GROUP.TITLE));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_QUERY_GROUP.UUID)));
		}
		
		@Override
		protected void fromObject(DataQueryGroup o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_QUERY_GROUP.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_QUERY_GROUP.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_QUERY_GROUP.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_QUERY_GROUP.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_QUERY_GROUP.TITLE, o.getTitle());
			q.addValue(OFC_DATA_QUERY_GROUP.UUID, o.getUuid().toString());
		}

	}
}

