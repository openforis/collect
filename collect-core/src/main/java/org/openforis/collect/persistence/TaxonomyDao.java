package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonomy.OFC_TAXONOMY;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.StoreQuery;
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
	
	public CollectTaxonomy loadByName(int surveyId, String name) {
		TaxonomyDSLContext dsl = dsl();
		Record r = dsl.selectFrom(OFC_TAXONOMY)
				.where(OFC_TAXONOMY.SURVEY_ID.equal(surveyId))
				.and(OFC_TAXONOMY.NAME.equal(name))
				.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return dsl.fromRecord(r);
		}
	}
	
	public List<CollectTaxonomy> loadAllBySurvey(int surveyId) {
		TaxonomyDSLContext dsl = dsl();
		Result<OfcTaxonomyRecord> r = dsl.selectFrom(OFC_TAXONOMY)
				.where(OFC_TAXONOMY.SURVEY_ID.equal(surveyId))
				.orderBy(OFC_TAXONOMY.NAME)
				.fetch();
		if ( r == null ) {
			return null;
		} else {
			return dsl.fromResult(r);
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

	protected static class TaxonomyDSLContext extends MappingDSLContext<CollectTaxonomy> {

		private static final long serialVersionUID = 1L;

		public TaxonomyDSLContext(Configuration config) {
			super(config, OFC_TAXONOMY.ID, OFC_TAXONOMY_ID_SEQ, CollectTaxonomy.class);
		}

		@Override
		public void fromRecord(Record r, CollectTaxonomy t) {
			t.setId(r.getValue(OFC_TAXONOMY.ID));
			t.setSurveyId(r.getValue(OFC_TAXONOMY.SURVEY_ID));
			t.setName(r.getValue(OFC_TAXONOMY.NAME));
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
