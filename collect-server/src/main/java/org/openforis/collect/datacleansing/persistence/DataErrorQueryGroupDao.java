package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_ERROR_QUERY_GROUP_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQueryGroup.OFC_DATA_ERROR_QUERY_GROUP;
import static org.openforis.collect.persistence.jooq.tables.OfcDataErrorQueryGroupQuery.OFC_DATA_ERROR_QUERY_GROUP_QUERY;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.BatchBindStep;
import org.jooq.DeleteConditionStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataErrorQueryGroupRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataErrorQueryGroupDao")
@Transactional
public class DataErrorQueryGroupDao extends SurveyObjectMappingJooqDaoSupport<DataErrorQueryGroup, DataErrorQueryGroupDao.JooqDSLContext> {

	public DataErrorQueryGroupDao() {
		super(DataErrorQueryGroupDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataErrorQueryGroup> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataErrorQueryGroupRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY_GROUP)
				.where(OFC_DATA_ERROR_QUERY_GROUP.SURVEY_ID.eq(survey.getId()))
			;
		
		Result<OfcDataErrorQueryGroupRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public Set<DataErrorQueryGroup> loadGroupsByQuery(DataErrorQuery query) {
		JooqDSLContext dsl = dsl((CollectSurvey) query.getSurvey());
		SelectConditionStep<Record1<Integer>> subselect = dsl.select(OFC_DATA_ERROR_QUERY_GROUP_QUERY.GROUP_ID)
			.from(OFC_DATA_ERROR_QUERY_GROUP_QUERY)
			.where(OFC_DATA_ERROR_QUERY_GROUP_QUERY.QUERY_ID.eq(query.getId()));
		
		Select<OfcDataErrorQueryGroupRecord> select = 
			dsl.selectFrom(OFC_DATA_ERROR_QUERY_GROUP)
				.where(OFC_DATA_ERROR_QUERY_GROUP.ID.in(subselect));
		
		Result<OfcDataErrorQueryGroupRecord> result = select.fetch();
		return new HashSet<DataErrorQueryGroup>(dsl.fromResult(result));
	}
	
	public List<Integer> loadQueryIds(DataErrorQueryGroup group) {
		JooqDSLContext dsl = dsl((CollectSurvey) group.getSurvey());
		Select<Record1<Integer>> select = 
				dsl.select(OFC_DATA_ERROR_QUERY_GROUP_QUERY.QUERY_ID)
					.from(OFC_DATA_ERROR_QUERY_GROUP_QUERY)
					.where(OFC_DATA_ERROR_QUERY_GROUP_QUERY.GROUP_ID.eq(group.getId()))
					.orderBy(OFC_DATA_ERROR_QUERY_GROUP_QUERY.SORT_ORDER);
		List<Integer> ids = select.fetch(OFC_DATA_ERROR_QUERY_GROUP_QUERY.QUERY_ID);
		return ids;
	}
	
	public void deleteQueryAssociations(DataErrorQueryGroup group) {
		JooqDSLContext dsl = dsl((CollectSurvey) group.getSurvey());
		DeleteConditionStep<?> query = dsl.delete(OFC_DATA_ERROR_QUERY_GROUP_QUERY)
			.where(OFC_DATA_ERROR_QUERY_GROUP_QUERY.GROUP_ID.eq(group.getId()));
		query.execute();
	}
	
	public void insertQueryAssociations(DataErrorQueryGroup chain, List<Integer> queryIds) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		BatchBindStep batch = dsl.batch(dsl.insertInto(OFC_DATA_ERROR_QUERY_GROUP_QUERY,
				OFC_DATA_ERROR_QUERY_GROUP_QUERY.GROUP_ID, 
				OFC_DATA_ERROR_QUERY_GROUP_QUERY.QUERY_ID,
				OFC_DATA_ERROR_QUERY_GROUP_QUERY.SORT_ORDER)
			.values((Integer) null, (Integer) null, (Integer) null));
		int position = 0;
		for (Integer stepId : queryIds) {
			batch.bind(chain.getId(), stepId, position ++);
		}
		batch.execute();
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataErrorQueryGroup> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_ERROR_QUERY_GROUP.ID, OFC_DATA_ERROR_QUERY_GROUP_ID_SEQ, DataErrorQueryGroup.class, survey);
		}
		
		@Override
		protected DataErrorQueryGroup newEntity() {
			return new DataErrorQueryGroup(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataErrorQueryGroup o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_ERROR_QUERY_GROUP.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_ERROR_QUERY_GROUP.DESCRIPTION));
			o.setModifiedDate(r.getValue(OFC_DATA_ERROR_QUERY_GROUP.MODIFIED_DATE));
			o.setTitle(r.getValue(OFC_DATA_ERROR_QUERY_GROUP.TITLE));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_ERROR_QUERY_GROUP.UUID)));
		}
		
		@Override
		protected void fromObject(DataErrorQueryGroup o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.MODIFIED_DATE, toTimestamp(o.getModifiedDate()));
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.TITLE, o.getTitle());
			q.addValue(OFC_DATA_ERROR_QUERY_GROUP.UUID, o.getUuid().toString());
		}

	}
}

