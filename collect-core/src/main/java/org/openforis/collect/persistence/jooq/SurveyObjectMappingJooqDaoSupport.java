/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.jooq.Record;
import org.jooq.ResultQuery;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * @author S. Ricci
 *
 */
public class SurveyObjectMappingJooqDaoSupport<T extends PersistedSurveyObject, C extends SurveyObjectMappingDSLContext<T>> 
	extends MappingJooqDaoSupport<T, C>  {

	public SurveyObjectMappingJooqDaoSupport(Class<C> jooqFactoryClass) {
		super(jooqFactoryClass);
	}

	@Override
	protected C dsl() {
		throw new UnsupportedOperationException();
	}
	
	public T loadById(CollectSurvey survey, int id) {
		C dsl = dsl(survey);
		ResultQuery<?> selectQuery = dsl.selectByIdQuery(id);
		Record r = selectQuery.fetchOne();
		return r == null ? null : dsl.fromRecord(r);
	}
	
	@Override
	public void insert(T item) {
		C dsl = dsl((CollectSurvey) item.getSurvey());
		dsl.insertQuery(item).execute();
	}
	
	protected C dsl(CollectSurvey survey) {
		Connection conn = getConnection();
		try {
			return jooqFactoryClass.getConstructor(Connection.class, CollectSurvey.class).newInstance(conn, survey);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create " + jooqFactoryClass, e);
		}
	}

	
}
