/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
public class DataQueryManager extends AbstractSurveyObjectManager<DataQuery, DataQueryDao> {

	@Override
	@Autowired
	@Qualifier("dataQueryDao")
	public void setDao(DataQueryDao dao) {
		super.setDao(dao);
	}
	
}
