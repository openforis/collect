package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Taxonomy.TAXONOMY;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public class TaxonomyDAO extends MappingJooqDaoSupport<Taxonomy, TaxonomyDAO.JooqFactory> {

	public TaxonomyDAO() {
		super(TaxonomyDAO.JooqFactory.class);
	}
	
	@Transactional
	public Taxonomy load(String name) {
		JooqFactory jf = getMappingJooqFactory();
		Record r = jf.selectByFieldQuery(TAXONOMY.NAME, name).fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}
	
	protected static class JooqFactory extends MappingJooqFactory<Taxonomy> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, TAXONOMY.ID, TAXONOMY_ID_SEQ, Taxonomy.class);
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

		@Override
		protected int getId(Taxonomy t) {
			return t.getId();
		}
	}
}
