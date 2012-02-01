package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.Taxonomy.TAXONOMY;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.model.species.Taxonomy;

/**
 * @author G. Miceli
 */
public class TaxonomyDAO extends CollectDAO {

	public Taxonomy load(int id) {
		Record r =  getJooqFactory()
				.select()
				.where(TAXONOMY.ID.equal(id))
				.fetchOne();
		Taxonomy t = new Taxonomy();
		t.setId(r.getValue(TAXONOMY.ID));
		t.setName(r.getValue(TAXONOMY.NAME));
		return t;
	}
	
	public void store(Taxonomy t) {
		UpdatableRecord<?> r = getJooqFactory().newRecord(TAXONOMY);
		r.setValue(TAXONOMY.ID, t.getId());
		r.setValue(TAXONOMY.NAME, t.getName());
		r.store();
	}

}
