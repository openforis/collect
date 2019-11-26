package org.openforis.collect.datacleansing.persistence;

import org.openforis.collect.datacleansing.DataCleansingItem;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingDSLContext;
import org.openforis.collect.persistence.jooq.SurveyObjectMappingJooqDaoSupport;

public abstract class DataCleansingItemDao<T extends DataCleansingItem, C extends SurveyObjectMappingDSLContext<Integer, T>> extends SurveyObjectMappingJooqDaoSupport<Integer, T, C> {

	public DataCleansingItemDao(Class<C> jooqFactoryClass) {
		super(jooqFactoryClass);
	}

}
