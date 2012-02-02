package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.jooq.Record;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public class MappingJooqDaoSupport<E, J extends MappingJooqFactory<E>> extends JooqDaoSupport {
	private Class<J> jooqFactoryClass;

	public MappingJooqDaoSupport(Class<J> jooqFactoryClass) {
		this.jooqFactoryClass = jooqFactoryClass;
	}
	
	protected J getMappingJooqFactory() {
		Connection conn = getConnection();
		try {
			return jooqFactoryClass.getConstructor(Connection.class).newInstance(conn);
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException("Missing constructor "+jooqFactoryClass.getName()+"(java.sql.Connection)");
		} catch (Exception e) {
			throw new RuntimeException("Failed to create "+jooqFactoryClass, e);
		}
	}
	
	@Transactional
	public E load(int id) {
		J jf = getMappingJooqFactory();
		Record r = jf.selectByIdQuery(id).fetchOne();
		E t = jf.fromRecord(r);
		return t;
	}
	
	@Transactional
	public void insert(E entity) {
		J jf = getMappingJooqFactory();
		jf.insertQuery(entity).execute();
	}
	
	@Transactional
	public void update(E entity) {
		J jf = getMappingJooqFactory();
		jf.updateQuery(entity).execute();
	}

	@Transactional
	public void delete(int id) {
		J jf = getMappingJooqFactory();
		jf.deleteQuery(id).execute();
	}
}
