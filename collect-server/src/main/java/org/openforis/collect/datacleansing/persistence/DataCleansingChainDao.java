package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_CLEANSING_CHAIN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.DeleteConditionStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingChainRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Component("dataCleansingChainDao")
@Transactional
public class DataCleansingChainDao extends DataCleansingItemDao<DataCleansingChain, DataCleansingChainDao.JooqDSLContext> {

	public DataCleansingChainDao() {
		super(DataCleansingChainDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataCleansingChain> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataCleansingChainRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_CHAIN)
				.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId()))
				.orderBy(OFC_DATA_CLEANSING_CHAIN.TITLE);
		
		Result<OfcDataCleansingChainRecord> result = select.fetch();
		return dsl.fromResult(result);
	}
	
	@Override
	public void deleteBySurvey(CollectSurvey survey) {
		dsl().delete(OFC_DATA_CLEANSING_CHAIN)
			.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId()))
			.execute();
	}

	public Set<DataCleansingChain> loadChainsByStep(DataCleansingStep step) {
		JooqDSLContext dsl = dsl((CollectSurvey) step.getSurvey());
		Select<Record1<Integer>> subselect = dsl
			.select(OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID)
			.from(OFC_DATA_CLEANSING_CHAIN_STEPS)
			.where(OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID.eq(step.getId()))
			.orderBy(OFC_DATA_CLEANSING_CHAIN_STEPS.POS);
		
		Select<OfcDataCleansingChainRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_CHAIN)
				.where(OFC_DATA_CLEANSING_CHAIN.ID.in(subselect));
		
		Result<OfcDataCleansingChainRecord> result = select.fetch();
		return new HashSet<DataCleansingChain>(dsl.fromResult(result));
	}
	
	public List<Integer> loadStepIds(DataCleansingChain chain) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		Select<Record1<Integer>> select = 
				dsl.select(OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID)
					.from(OFC_DATA_CLEANSING_CHAIN_STEPS)
					.where(OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID.eq(chain.getId()))
					.orderBy(OFC_DATA_CLEANSING_CHAIN_STEPS.POS);
		List<Integer> ids = select.fetch(OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID);
		return ids;
	}
	
	public void deleteStepAssociations(DataCleansingChain chain) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		DeleteConditionStep<?> query = dsl.delete(OFC_DATA_CLEANSING_CHAIN_STEPS)
			.where(OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID.eq(chain.getId()));
		query.execute();
	}
	
	public void deleteStepAssociations(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		DeleteConditionStep<?> query = dsl.delete(OFC_DATA_CLEANSING_CHAIN_STEPS)
			.where(OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID.in(
					dsl.select(OFC_DATA_CLEANSING_CHAIN.ID)
						.from(OFC_DATA_CLEANSING_CHAIN)
						.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId())
				)
			)
		);
		query.execute();
	}
	
	public void insertStepAssociations(DataCleansingChain chain, List<Integer> stepIds) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		BatchBindStep batch = dsl.batch(dsl.insertInto(OFC_DATA_CLEANSING_CHAIN_STEPS, 
				OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID, 
				OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID,
				OFC_DATA_CLEANSING_CHAIN_STEPS.POS)
			.values((Integer) null, (Integer) null, (Integer) null));
		int position = 0;
		for (Integer stepId : stepIds) {
			batch.bind(chain.getId(), stepId, position ++);
		}
		batch.execute();
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<Integer, DataCleansingChain> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Configuration config) {
			this(config, null);
		}
		
		public JooqDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_DATA_CLEANSING_CHAIN.ID, OFC_DATA_CLEANSING_CHAIN_ID_SEQ, DataCleansingChain.class, survey);
		}
		
		@Override
		protected DataCleansingChain newEntity() {
			return new DataCleansingChain(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataCleansingChain o) {
			super.fromRecord(r, o);
			o.setCreationDate(r.getValue(OFC_DATA_CLEANSING_CHAIN.CREATION_DATE));
			o.setDescription(r.getValue(OFC_DATA_CLEANSING_CHAIN.DESCRIPTION));
			o.setModifiedDate(r.getValue(OFC_DATA_CLEANSING_CHAIN.MODIFIED_DATE));
			o.setTitle(r.getValue(OFC_DATA_CLEANSING_CHAIN.TITLE));
			o.setUuid(UUID.fromString(r.getValue(OFC_DATA_CLEANSING_CHAIN.UUID)));
		}
		
		@Override
		protected void fromObject(DataCleansingChain o, StoreQuery<?> q) {
			super.fromObject(o, q);
			q.addValue(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID, o.getSurvey().getId());
			q.addValue(OFC_DATA_CLEANSING_CHAIN.CREATION_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_CLEANSING_CHAIN.DESCRIPTION, o.getDescription());
			q.addValue(OFC_DATA_CLEANSING_CHAIN.MODIFIED_DATE, toTimestamp(o.getCreationDate()));
			q.addValue(OFC_DATA_CLEANSING_CHAIN.TITLE, o.getTitle());
			q.addValue(OFC_DATA_CLEANSING_CHAIN.UUID, o.getUuid().toString());
		}

	}
}

