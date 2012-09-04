package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_TAXONOMY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcTaxonomy.OFC_TAXONOMY;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.StoreQuery;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class TaxonomyDao extends MappingJooqDaoSupport<Taxonomy, TaxonomyDao.JooqFactory> {

	public TaxonomyDao() {
		super(TaxonomyDao.JooqFactory.class);
	}
	
	@Transactional
	public Taxonomy load(String name) {
		JooqFactory jf = getMappingJooqFactory();
		Record r = jf.selectByFieldQuery(OFC_TAXONOMY.NAME, name).fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}
	
	@Override
	public Taxonomy loadById(int id) {
		return super.loadById(id);
	}

	@Override
	public void insert(Taxonomy entity) {
		super.insert(entity);
	}

	@Override
	public void update(Taxonomy entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}

	protected static class JooqFactory extends MappingJooqFactory<Taxonomy> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_TAXONOMY.ID, OFC_TAXONOMY_ID_SEQ, Taxonomy.class);
		}

		@Override
		public void fromRecord(Record r, Taxonomy t) {
			t.setId(r.getValue(OFC_TAXONOMY.ID));
			t.setName(r.getValue(OFC_TAXONOMY.NAME));
		}
		
		@Override
		public void fromObject(Taxonomy t, StoreQuery<?> q) {
			q.addValue(OFC_TAXONOMY.ID, t.getId());
			q.addValue(OFC_TAXONOMY.NAME, t.getName());
			q.addValue(OFC_TAXONOMY.METADATA, " ");
		}

		@Override
		protected void setId(Taxonomy taxonomy, int id) {
			taxonomy.setId(id);
		}

		@Override
		protected Integer getId(Taxonomy t) {
			return t.getId();
		}
	}
}
