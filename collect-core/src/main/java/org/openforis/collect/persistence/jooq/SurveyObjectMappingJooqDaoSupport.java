/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import java.sql.Connection;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 *
 */
public class SurveyObjectMappingJooqDaoSupport<T extends SurveyObject, C extends SurveyObjectMappingDSLContext<T>> 
	extends MappingJooqDaoSupport<T, C>  {

	public SurveyObjectMappingJooqDaoSupport(Class<C> jooqFactoryClass) {
		super(jooqFactoryClass);
	}

	@Override
	protected C dsl() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void insert(T item) {
		C dsl = dsl((CollectSurvey) item.getSurvey());
		dsl.insertQuery(item).execute();
	}
	
	protected C dsl(CollectSurvey survey) {
		Connection conn = getConnection();
		try {
			return jooqFactoryClass.getConstructor(Connection.class).newInstance(conn, survey);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create " + jooqFactoryClass, e);
		}
	}

	
}
