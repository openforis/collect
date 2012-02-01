package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Taxonomy.TAXONOMY;

import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.jooq.UpdateQuery;
import org.jooq.impl.Factory;
import org.openforis.collect.model.species.Taxonomy;
import org.openforis.collect.persistence.jooq.tables.records.TaxonomyRecord;

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
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void insert(Taxonomy t) {
		Factory jf = getJooqFactory();
		int nextId = jf.nextval(TAXONOMY_ID_SEQ).intValue();
		t.setId(nextId);
		
		UpdatableRecord r = toRecord(t);
		
		InsertQuery insert = jf.insertQuery(r.getTable());
		insert.setRecord(r);
		insert.execute();
	}

	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void update(Taxonomy t) {	
		UpdatableRecord r = toRecord(t);
		
		UpdateQuery update = getJooqFactory().updateQuery(r.getTable());
		update.setRecord(r);
		update.execute();
	}

	public void delete(int id) {
		getJooqFactory()
			.delete(TAXONOMY)
			.where(TAXONOMY.ID.equal(id))
			.execute();
	}
	
	private UpdatableRecord<?> toRecord(Taxonomy t) {
		UpdatableRecord<?> r = getJooqFactory().newRecord(TAXONOMY);
		r.setValue(TAXONOMY.ID, t.getId());
		r.setValue(TAXONOMY.NAME, t.getName());
		r.setValue(TAXONOMY.METADATA, "Not yet implemented");
		return r;
	}

}
