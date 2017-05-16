package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonomy.OFC_TAXONOMY;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.StoreQuery;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.persistence.jooq.MappingDSLContext;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonomyRecord;

/**
 * @author G. Miceli
 */
public class TaxonomyDao extends MappingJooqDaoSupport<CollectTaxonomy, TaxonomyDao.TaxonomyDSLContext> {

	public TaxonomyDao() {
		super(TaxonomyDao.TaxonomyDSLContext.class);
	}
	
	public CollectTaxonomy loadByName(CollectSurvey survey, String name) {
		TaxonomyDSLContext dsl = dsl(survey);
		Record r = dsl.selectFrom(OFC_TAXONOMY)
				.where(OFC_TAXONOMY.SURVEY_ID.equal(survey.getId()))
				.and(OFC_TAXONOMY.NAME.equal(name))
				.fetchOne();
		return r == null ? null : dsl.fromRecord(r);
	}
	
	public List<CollectTaxonomy> loadAllBySurvey(CollectSurvey survey) {
		TaxonomyDSLContext dsl = dsl(survey);
		Result<OfcTaxonomyRecord> r = dsl.selectFrom(OFC_TAXONOMY)
				.where(OFC_TAXONOMY.SURVEY_ID.equal(survey.getId()))
				.orderBy(OFC_TAXONOMY.NAME)
				.fetch();
		return r == null ? null : dsl.fromResult(r);
	}
	
	public CollectTaxonomy loadById(CollectSurvey survey, int id) {
		return loadById(dsl(survey), id);
	}

	@Override
	public void insert(CollectTaxonomy entity) {
		dsl(entity.getSurvey()).insertQuery(entity).execute();
	}

	@Override
	public void update(CollectTaxonomy entity) {
		dsl(entity.getSurvey()).updateQuery(entity).execute();
	}

	public void delete(CollectTaxonomy taxonomy) {
		dsl(taxonomy.getSurvey()).deleteQuery(taxonomy.getId()).execute();
	}

	@Override
	protected TaxonomyDSLContext dsl() {
		throw new UnsupportedOperationException();
	}
	
	private TaxonomyDSLContext dsl(CollectSurvey survey) {
		return new TaxonomyDSLContext(getConfiguration(), survey);
	}

	protected static class TaxonomyDSLContext extends MappingDSLContext<CollectTaxonomy> {

		private static final long serialVersionUID = 1L;
		
		private CollectSurvey survey;

		public TaxonomyDSLContext(Configuration config, CollectSurvey survey) {
			super(config, OFC_TAXONOMY.ID, OFC_TAXONOMY_ID_SEQ, CollectTaxonomy.class);
			this.survey = survey;
		}

		@Override
		public void fromRecord(Record r, CollectTaxonomy t) {
			t.setId(r.getValue(OFC_TAXONOMY.ID));
			t.setSurveyId(r.getValue(OFC_TAXONOMY.SURVEY_ID));
			t.setName(r.getValue(OFC_TAXONOMY.NAME));
			t.setSurvey(survey);
		}
		
		@Override
		public void fromObject(CollectTaxonomy t, StoreQuery<?> q) {
			q.addValue(OFC_TAXONOMY.ID, t.getId());
			q.addValue(OFC_TAXONOMY.SURVEY_ID, t.getSurveyId());
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
