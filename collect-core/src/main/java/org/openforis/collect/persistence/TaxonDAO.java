package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.Taxon.TAXON;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.model.species.Taxon;
import org.openforis.collect.persistence.jooq.CollectJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;

/**
 * @author G. Miceli
 */

public class TaxonDAO extends JooqDaoSupport {
	public TaxonDAO() {
	}

	public Taxon load(int id) {
		Record record = getJooqFactory()
				.select()
				.where(TAXON.ID.equal(id))
				.fetchOne();
		Taxon taxon = new Taxon();
		taxon.setId(record.getValue(TAXON.ID));
		taxon.setParentId(record.getValue(TAXON.PARENT_ID));
		taxon.setScientificName(record.getValue(TAXON.SCIENTIFIC_NAME));
		taxon.setTaxonomicRank(record.getValue(TAXON.TAXON_RANK));
		taxon.setTaxonomyId(record.getValue(TAXON.TAXONOMY_ID));
		taxon.setStep(record.getValue(TAXON.STEP));
		return taxon;
	}
	
	public void store(Taxon taxon) {
		UpdatableRecord<?> r = getJooqFactory().newRecord(TAXON);
		r.setValue(TAXON.ID, taxon.getId());
		r.setValue(TAXON.PARENT_ID, taxon.getParentId());
		r.setValue(TAXON.SCIENTIFIC_NAME, taxon.getScientificName());
		r.setValue(TAXON.TAXON_RANK, taxon.getTaxonomicRank());
		r.setValue(TAXON.TAXONOMY_ID, taxon.getTaxonomyId());
		r.setValue(TAXON.STEP, taxon.getStep());
		r.store();
	}
}
