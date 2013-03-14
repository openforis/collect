package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonomy.OFC_TAXONOMY;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonomyRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class TaxonomyDao extends MappingJooqDaoSupport<CollectTaxonomy, TaxonomyDao.JooqFactory> {

	public TaxonomyDao() {
		super(TaxonomyDao.JooqFactory.class);
	}
	
	@Transactional
	public CollectTaxonomy load(int surveyId, String name) {
		return loadBySurvey(surveyId, name, false);
	}

	@Transactional
	public CollectTaxonomy loadBySurveyWork(int surveyId, String name) {
		return loadBySurvey(surveyId, name, true);
	}
	
	protected CollectTaxonomy loadBySurvey(int surveyId, String name, boolean work) {
		JooqFactory jf = getMappingJooqFactory();
		TableField<OfcTaxonomyRecord, Integer> surveyIdField = work ? OFC_TAXONOMY.SURVEY_WORK_ID: OFC_TAXONOMY.SURVEY_ID;
		Record r = jf.selectFrom(OFC_TAXONOMY)
				.where(surveyIdField.equal(surveyId))
				.and(OFC_TAXONOMY.NAME.equal(name))
				.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}
	
	public List<CollectTaxonomy> loadAllBySurvey(int surveyId) {
		return loadBySurvey(surveyId, false);
	}
	
	public List<CollectTaxonomy> loadAllBySurveyWork(int surveyId) {
		return loadBySurvey(surveyId, true);
	}
	
	protected List<CollectTaxonomy> loadBySurvey(int surveyId, boolean work) {
		JooqFactory jf = getMappingJooqFactory();
		TableField<OfcTaxonomyRecord, Integer> surveyIdField = work ? OFC_TAXONOMY.SURVEY_WORK_ID: OFC_TAXONOMY.SURVEY_ID;
		Result<OfcTaxonomyRecord> r = jf.selectFrom(OFC_TAXONOMY)
				.where(surveyIdField.equal(surveyId))
				.orderBy(OFC_TAXONOMY.NAME)
				.fetch();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromResult(r);
		}
	}
	
	@Override
	public CollectTaxonomy loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(CollectTaxonomy entity) {
		super.insert(entity);
	}

	@Override
	public void update(CollectTaxonomy entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	protected static class JooqFactory extends MappingJooqFactory<CollectTaxonomy> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXONOMY.ID, OFC_TAXONOMY_ID_SEQ, CollectTaxonomy.class);
		}

		@Override
		public void fromRecord(Record r, CollectTaxonomy t) {
			t.setId(r.getValue(OFC_TAXONOMY.ID));
			t.setSurveyId(r.getValue(OFC_TAXONOMY.SURVEY_ID));
			t.setSurveyWorkId(r.getValue(OFC_TAXONOMY.SURVEY_WORK_ID));
			t.setName(r.getValue(OFC_TAXONOMY.NAME));
		}
		
		@Override
		public void fromObject(CollectTaxonomy t, StoreQuery<?> q) {
			q.addValue(OFC_TAXONOMY.ID, t.getId());
			q.addValue(OFC_TAXONOMY.SURVEY_ID, t.getSurveyId());
			q.addValue(OFC_TAXONOMY.SURVEY_WORK_ID, t.getSurveyWorkId());
			q.addValue(OFC_TAXONOMY.NAME, t.getName());
			q.addValue(OFC_TAXONOMY.METADATA, " ");
		}

		@Override
		protected void setId(CollectTaxonomy taxonomy, int id) {
			taxonomy.setId(id);
		}

		@Override
		protected Integer getId(CollectTaxonomy t) {
			return t.getId();
		}
	}
}
