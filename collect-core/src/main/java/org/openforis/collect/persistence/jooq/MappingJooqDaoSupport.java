package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SimpleSelectQuery;
import org.jooq.TableField;
import org.openforis.collect.persistence.jooq.tables.OfcTaxonVernacularName;
import org.openforis.collect.persistence.jooq.tables.records.OfcTaxonVernacularNameRecord;
import org.openforis.idm.model.species.TaxonVernacularName;
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
	
	protected List<E> findStartingWith(TableField<?,String> field, String searchString, int maxResults) {
		J jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectStartsWithQuery(field, searchString);
		query.addLimit(maxResults);
		query.execute();
		Result<?> result = query.getResult();
		List<E> entities = jf.fromResult(result);
		return entities;

	}
	
	protected List<E> findContaining(TableField<?,String> field, String searchString, int maxResults) {
		J jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectContainsQuery(field, searchString);
		query.addLimit(maxResults);
		query.execute();
		Result<?> result = query.getResult();
		List<E> entities = jf.fromResult(result);
		return entities;

	}
	
	@Transactional
	protected E loadById(int id) {
		J jf = getMappingJooqFactory();
		ResultQuery<?> selectQuery = jf.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		if ( r == null ) {
			return null;
		} else {
			return jf.fromRecord(r);
		}
	}
	
	@Transactional
	protected void insert(E entity) {
		J jf = getMappingJooqFactory();
		jf.insertQuery(entity).execute();
	}
	
	@Transactional
	protected void update(E entity) {
		J jf = getMappingJooqFactory();
		jf.updateQuery(entity).execute();
	}

	@Transactional
	protected void delete(int id) {
		J jf = getMappingJooqFactory();
		jf.deleteQuery(id).execute();
	}
}
