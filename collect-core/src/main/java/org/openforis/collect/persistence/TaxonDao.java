package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.StoreQuery;
import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author S. Ricci
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

	public List<Taxon> findByCode(int taxonomyId, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.CODE, taxonomyId, searchString, maxResults);
	}

	public List<Taxon> findByScientificName(int taxonomyId, String searchString, int maxResults) {
		return findStartingWith(OFC_TAXON.SCIENTIFIC_NAME, taxonomyId, searchString, maxResults);
	}

	protected List<Taxon> findStartingWith(TableField<?,String> field, int taxonomyId, String searchString, int maxResults) {
		JooqFactory jf = getMappingJooqFactory();
		searchString = searchString.toUpperCase() + "%";
		Select<?> query = 
			jf.select()
			.from(OFC_TAXON)
			.where(OFC_TAXON.TAXONOMY_ID.equal(taxonomyId)
				.and(JooqFactory.upper(field).like(searchString)))
			.limit(maxResults);
		Result<?> result = query.fetch();
		List<Taxon> entities = jf.fromResult(result);
		return entities;
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
			String taxonRankName = r.getValue(OFC_TAXON.TAXON_RANK);
			TaxonRank taxonRank = TaxonRank.fromName(taxonRankName);
			t.setTaxonRank(taxonRank);
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
			TaxonRank taxonRank = t.getTaxonRank();
			q.addValue(OFC_TAXON.TAXON_RANK, taxonRank != null ? taxonRank.getName(): null);
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

