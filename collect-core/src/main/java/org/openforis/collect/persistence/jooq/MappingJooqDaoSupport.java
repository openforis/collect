package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public class MappingJooqDaoSupport<E, C extends MappingDSLContext<E>> extends JooqDaoSupport {
	protected Class<C> jooqFactoryClass;

	public MappingJooqDaoSupport(Class<C> jooqFactoryClass) {
		this.jooqFactoryClass = jooqFactoryClass;
	}
	
	protected C dsl() {
		Connection conn = getConnection();
		try {
			return jooqFactoryClass.getConstructor(Connection.class).newInstance(conn);
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException("Missing constructor "+jooqFactoryClass.getName()+"(java.sql.Connection)");
		} catch (Exception e) {
			throw new RuntimeException("Failed to create "+jooqFactoryClass, e);
		}
	}
	
	protected List<E> findStartingWith(TableField<?,String> field, String searchString, int maxResults) {
		C dsl = dsl();
		SelectQuery<?> query = dsl.selectStartsWithQuery(field, searchString);
		query.addLimit(maxResults);
		query.execute();
		Result<?> result = query.getResult();
		List<E> entities = dsl.fromResult(result);
		return entities;

	}
	
	protected List<E> findContaining(TableField<?,String> field, String searchString, int maxResults) {
		C dsl = dsl();
		SelectQuery<?> query = dsl.selectContainsQuery(field, searchString);
		query.addLimit(maxResults);
		query.execute();
		Result<?> result = query.getResult();
		List<E> entities = dsl.fromResult(result);
		return entities;

	}
	
	@Transactional
	protected E loadById(int id) {
		C dsl = dsl();
		ResultQuery<?> selectQuery = dsl.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return dsl.fromRecord(r);
		}
	}
	
	@Transactional
	public void insert(E entity) {
		dsl().insertQuery(entity).execute();
	}
	
	@Transactional
	public void update(E entity) {
		dsl().updateQuery(entity).execute();
	}

	@Transactional
	public void delete(int id) {
		dsl().deleteQuery(id).execute();
	}
}
