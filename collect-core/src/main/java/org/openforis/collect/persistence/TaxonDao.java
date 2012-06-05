package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.StoreQuery;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.Taxon;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class TaxonDao extends MappingJooqDaoSupport<Taxon, TaxonDao.JooqFactory> {
	public TaxonDao() {
		super(TaxonDao.JooqFactory.class);
	}

	@Override
	public Taxon loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(Taxon entity) {
		super.insert(entity);
	}

	@Override
	public void update(Taxon entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	public List<Taxon> findByCode(String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.CODE, searchString, maxResults);
	}

	public List<Taxon> findByScientificName(String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.SCIENTIFIC_NAME, searchString, maxResults);
	}

	protected static class JooqFactory extends MappingJooqFactory<Taxon> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXON.ID, OFC_TAXON_ID_SEQ, Taxon.class);
		}

		@Override
		public void fromRecord(Record r, Taxon t) {
			t.setSystemId(r.getValue(OFC_TAXON.ID));
			t.setTaxonId(r.getValue(OFC_TAXON.TAXON_ID));
			t.setParentId(r.getValue(OFC_TAXON.PARENT_ID));
			t.setCode(r.getValueAsString(OFC_TAXON.CODE));
			t.setScientificName(r.getValue(OFC_TAXON.SCIENTIFIC_NAME));
			t.setTaxonomicRank(r.getValue(OFC_TAXON.TAXON_RANK));
			t.setTaxonomyId(r.getValue(OFC_TAXON.TAXONOMY_ID));
			t.setStep(r.getValue(OFC_TAXON.STEP));
		}

		@Override
		public void fromObject(Taxon t, StoreQuery<?> q) {
			q.addValue(OFC_TAXON.ID, t.getSystemId());
			q.addValue(OFC_TAXON.TAXON_ID, t.getTaxonId());
			q.addValue(OFC_TAXON.PARENT_ID, t.getParentId());
			q.addValue(OFC_TAXON.CODE, t.getCode());
			q.addValue(OFC_TAXON.SCIENTIFIC_NAME, t.getScientificName());
			q.addValue(OFC_TAXON.TAXON_RANK, t.getTaxonomicRank());
			q.addValue(OFC_TAXON.TAXONOMY_ID, t.getTaxonomyId());
			q.addValue(OFC_TAXON.STEP, t.getStep());
		}

		@Override
		protected void setId(Taxon t, int id) {
			t.setSystemId(id);
		}

		@Override
		protected Integer getId(Taxon t) {
			return t.getSystemId();
		}
	}
}

