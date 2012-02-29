package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxon.OFC_TAXON;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.Taxon;

/**
 * @author G. Miceli
 */
public class TaxonDAO extends MappingJooqDaoSupport<Taxon, TaxonDAO.JooqFactory> {
	public TaxonDAO() {
		super(TaxonDAO.JooqFactory.class);
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
			t.setId(r.getValue(OFC_TAXON.ID));
			t.setParentId(r.getValue(OFC_TAXON.PARENT_ID));
			t.setCode(r.getValueAsString(OFC_TAXON.CODE));
			t.setScientificName(r.getValue(OFC_TAXON.SCIENTIFIC_NAME));
			t.setTaxonomicRank(r.getValue(OFC_TAXON.TAXON_RANK));
			t.setTaxonomyId(r.getValue(OFC_TAXON.TAXONOMY_ID));
			t.setStep(r.getValue(OFC_TAXON.STEP));
		}
		
		@Override
		public void toRecord(Taxon t, UpdatableRecord<?> r) {
			r.setValue(OFC_TAXON.ID, t.getId());			
			r.setValue(OFC_TAXON.PARENT_ID, t.getParentId());
			r.setValue(OFC_TAXON.CODE, t.getCode());
			r.setValue(OFC_TAXON.SCIENTIFIC_NAME, t.getScientificName());
			r.setValue(OFC_TAXON.TAXON_RANK, t.getTaxonomicRank());
			r.setValue(OFC_TAXON.TAXONOMY_ID, t.getTaxonomyId());
			r.setValue(OFC_TAXON.STEP, t.getStep());
		}

		@Override
		protected void setId(Taxon t, int id) {
			t.setId(id);
		}

		@Override
		protected int getId(Taxon t) {
			return t.getId();
		}
	}
}
