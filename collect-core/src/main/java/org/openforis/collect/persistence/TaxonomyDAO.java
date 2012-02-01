package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Taxonomy.TAXONOMY;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.model.species.Taxonomy;

/**
 * @author G. Miceli
 */
public class TaxonomyDAO extends CollectDAO {

	private TaxonomyMapper mapper;
	
	public TaxonomyDAO() {
		mapper = new TaxonomyMapper();
	}
	
	public Taxonomy load(int id) {
		Record r = mapper.selectQuery(getJooqFactory(), id).fetchOne();
		Taxonomy t = mapper.fromRecord(r);
		return t;
	}
	
	public void insert(Taxonomy t) {
		mapper.insertQuery(getJooqFactory(), t).execute();
	}

	
	public void update(Taxonomy t) {	
		mapper.updateQuery(getJooqFactory(), t).execute();
	}

	public void delete(int id) {
		mapper.deleteQuery(getJooqFactory(), id).execute();
	}

	public class TaxonomyMapper extends TableMapper<Taxonomy> {

		public TaxonomyMapper() {
			super(TAXONOMY.ID, TAXONOMY_ID_SEQ, Taxonomy.class);
		}

		@Override
		public void fromRecord(Record r, Taxonomy t) {
			t.setId(r.getValue(TAXONOMY.ID));
			t.setName(r.getValue(TAXONOMY.NAME));
		}
		
		@Override
		public void toRecord(Taxonomy t, UpdatableRecord<?> r) {
			r.setValue(TAXONOMY.ID, t.getId());
			r.setValue(TAXONOMY.NAME, t.getName());
			r.setValue(TAXONOMY.METADATA, "Not yet implemented");
		}

		@Override
		protected void setId(Taxonomy taxonomy, int id) {
			taxonomy.setId(id);
		}
	}
}
