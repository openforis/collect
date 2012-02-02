package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.TAXON_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Taxon.TAXON;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.model.species.Taxon;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;

/**
 * @author G. Miceli
 */
public class TaxonDAO extends MappingJooqDaoSupport<Taxon, TaxonDAO.JooqFactory> {
	public TaxonDAO() {
		super(TaxonDAO.JooqFactory.class);
	}

	protected static class JooqFactory extends MappingJooqFactory<Taxon> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, TAXON.ID, TAXON_ID_SEQ, Taxon.class);
		}

		@Override
		public void fromRecord(Record r, Taxon t) {
			t.setId(r.getValue(TAXON.ID));
			t.setParentId(r.getValue(TAXON.PARENT_ID));
			t.setScientificName(r.getValue(TAXON.SCIENTIFIC_NAME));
			t.setTaxonomicRank(r.getValue(TAXON.TAXON_RANK));
			t.setTaxonomyId(r.getValue(TAXON.TAXONOMY_ID));
			t.setStep(r.getValue(TAXON.STEP));
		}
		
		@Override
		public void toRecord(Taxon t, UpdatableRecord<?> r) {
			r.setValue(TAXON.ID, t.getId());
			r.setValue(TAXON.PARENT_ID, t.getParentId());
			r.setValue(TAXON.SCIENTIFIC_NAME, t.getScientificName());
			r.setValue(TAXON.TAXON_RANK, t.getTaxonomicRank());
			r.setValue(TAXON.TAXONOMY_ID, t.getTaxonomyId());
			r.setValue(TAXON.STEP, t.getStep());
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
