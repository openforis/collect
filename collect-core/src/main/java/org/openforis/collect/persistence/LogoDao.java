package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.StoreQuery;
import org.openforis.collect.model.Logo;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class LogoDao extends MappingJooqDaoSupport<Logo, LogoDao.JooqFactory> {
	public LogoDao() {
		super(LogoDao.JooqFactory.class);
	}

	@Override
	public Logo loadById(int id) {
		return super.loadById(id);
	}
	
	@Override
	public void insert(Logo entity) {
		super.insert(entity);
	}

	@Override
	public void update(Logo entity) {
		super.update(entity);
	}

	@Override
	public void delete(int id) {
		super.delete(id);
	}
	
	protected static class JooqFactory extends MappingJooqFactory<Logo> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_LOGO.POS, null, Logo.class);
		}

		@Override
		public void fromRecord(Record r, Logo l) {
			l.setPosition(r.getValue(OFC_LOGO.POS));
			l.setImage(r.getValue(OFC_LOGO.IMAGE));
		}
		
		@Override
		protected void fromObject(Logo l, StoreQuery<?> q) {
			q.addValue(OFC_LOGO.POS, l.getPosition());			
			q.addValue(OFC_LOGO.IMAGE, l.getImage());
		}

		@Override
		protected void setId(Logo l, int id) {
			l.setPosition(id);
		}

		@Override
		protected Integer getId(Logo l) {
			return l.getPosition();
		}
	}
}
