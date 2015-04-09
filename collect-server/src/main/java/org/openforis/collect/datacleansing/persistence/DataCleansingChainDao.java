package org.openforis.collect.datacleansing.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_DATA_CLEANSING_CHAIN_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN;
import static org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChainSteps.OFC_DATA_CLEANSING_CHAIN_STEPS;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingChainRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class DataCleansingChainDao extends SurveyObjectMappingJooqDaoSupport<DataCleansingChain, DataCleansingChainDao.JooqDSLContext> {

	public DataCleansingChainDao() {
		super(DataCleansingChainDao.JooqDSLContext.class);
	}
	
	@Override
	public List<DataCleansingChain> loadBySurvey(CollectSurvey survey) {
		JooqDSLContext dsl = dsl(survey);
		Select<OfcDataCleansingChainRecord> select = 
			dsl.selectFrom(OFC_DATA_CLEANSING_CHAIN)
				.where(OFC_DATA_CLEANSING_CHAIN.SURVEY_ID.eq(survey.getId()));
		
		Result<OfcDataCleansingChainRecord> result = select.fetch();
		return dsl.fromResult(result);
	}

	public List<Integer> loadStepIds(DataCleansingChain chain) {
		JooqDSLContext dsl = dsl((CollectSurvey) chain.getSurvey());
		Select<Record1<Integer>> select = 
				dsl.select(OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID)
					.from(OFC_DATA_CLEANSING_CHAIN_STEPS)
					.where(OFC_DATA_CLEANSING_CHAIN_STEPS.CHAIN_ID.eq(chain.getId()));
		List<Integer> ids = select.fetch(OFC_DATA_CLEANSING_CHAIN_STEPS.STEP_ID);
		return ids;
	}
	
	protected static class JooqDSLContext extends SurveyObjectMappingDSLContext<DataCleansingChain> {

		private static final long serialVersionUID = 1L;
		
		public JooqDSLContext(Connection connection) {
			this(connection, null);
		}
		
		public JooqDSLContext(Connection connection, CollectSurvey survey) {
			super(connection, OFC_DATA_CLEANSING_CHAIN.ID, OFC_DATA_CLEANSING_CHAIN_ID_SEQ, DataCleansingChain.class, survey);
		}
		
		@Override
		protected DataCleansingChain newEntity() {
			return new DataCleansingChain(getSurvey());
		}
		
		@Override
		protected void fromRecord(Record r, DataCleansingChain o) {
			super.fromRecord(r, o);
		}
		
		@Override
		protected void fromObject(DataCleansingChain o, StoreQuery<?> q) {
			super.fromObject(o, q);
		}

	}
}

