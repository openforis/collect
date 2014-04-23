package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.StoreQuery;
import org.openforis.collect.model.Logo;
import org.openforis.collect.model.LogoPosition;
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
	
	public Logo loadByPosition(LogoPosition position) {
		int id = getInternalId(position);
		return loadById(id);
	}

	@Override
	public void insert(Logo entity) {
		if ( entity.getId() == null ) {
			//TODO get new id from sequence
			entity.setId(LogoDao.getInternalId(entity.getPosition()));
		}
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
	
	public void deleteByPosition(LogoPosition position) {
		int id = getInternalId(position);
		delete(id);
	}
	
	//TODO remove it, add id column and load logos by position
	public static int getInternalId(LogoPosition position) {
		switch ( position ) {
		case TOP_RIGHT:
			return 1;
		case HEADER:
			return 2;
		case FOOTER:
			return 3;
		default:
			return -1;
		}
	}
	
	public static LogoPosition getPositionFromId(Integer id) {
		switch ( id ) {
		case 1:
			return LogoPosition.TOP_RIGHT;
		case 2:
			return LogoPosition.HEADER;
		case 3:
			return LogoPosition.FOOTER;
		default:
			return null;
		}
	}

	protected static class JooqFactory extends MappingJooqFactory<Logo> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection connection) {
			super(connection, OFC_LOGO.POS, null, Logo.class);
		}

		@Override
		public void fromRecord(Record r, Logo l) {
			//TODO remove it, get id from db
			Integer id = r.getValue(OFC_LOGO.POS);
			LogoPosition p = LogoDao.getPositionFromId(id);
			l.setId(id);
			l.setPosition(p);
			l.setImage(r.getValue(OFC_LOGO.IMAGE));
			//TODO set contentType
			//l.setContentType(contentType);
		}
		
		@Override
		protected void fromObject(Logo l, StoreQuery<?> q) {
			//TODO fix it
			//q.addValue(OFC_LOGO.ID, l.getId());
			q.addValue(OFC_LOGO.POS, l.getId());
			//q.addValue(OFC_LOGO.POSITION, l.getPosition().name());			
			q.addValue(OFC_LOGO.IMAGE, l.getImage());
			//q.addValue(OFC_LOGO.CONTENT_TYPE, l.getContentType());
		}

		@Override
		protected void setId(Logo l, int id) {
			l.setId(id);
		}

		@Override
		protected Integer getId(Logo l) {
			return l.getId();
		}
	}

}
