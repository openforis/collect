package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Taxonomy.TAXONOMY;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.model.species.Taxonomy;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;

/**
 * @author G. Miceli
 */
public class TaxonomyDAO extends CollectDAO {

	public TaxonomyDAO() {
	}
	
	private TaxonomyJooqFactory createJooqFactory() {
		return new TaxonomyJooqFactory(getConnection());
		
	}
	public Taxonomy load(int id) {
		TaxonomyJooqFactory jf = createJooqFactory();
		Record r = jf.selectByIdQuery(id).fetchOne();
		Taxonomy t = jf.fromRecord(r);
		return t;
	}
	
	public Taxonomy load(String name) {
		TaxonomyJooqFactory jf = createJooqFactory();
		Record r = jf.selectByFieldQuery(TAXONOMY.NAME, name).fetchOne();
		Taxonomy t = jf.fromRecord(r);
		return t;
	}
	
	public void insert(Taxonomy t) {
		TaxonomyJooqFactory jf = createJooqFactory();
		jf.insertQuery(t).execute();
	}
	
	public void update(Taxonomy t) {
		TaxonomyJooqFactory jf = createJooqFactory();
		jf.updateQuery(t).execute();
	}

	public void delete(int id) {
		TaxonomyJooqFactory jf = createJooqFactory();
		jf.deleteQuery(id).execute();
	}

	public class TaxonomyJooqFactory extends MappingJooqFactory<Taxonomy> {

		private static final long serialVersionUID = 1L;

		public TaxonomyJooqFactory(Connection conn) {
			super(conn, TAXONOMY.ID, TAXONOMY_ID_SEQ, Taxonomy.class);
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
			r.setValue(TAXONOMY.METADATA, " ");
		}

		@Override
		protected void setId(Taxonomy taxonomy, int id) {
			taxonomy.setId(id);
		}
	}
}
