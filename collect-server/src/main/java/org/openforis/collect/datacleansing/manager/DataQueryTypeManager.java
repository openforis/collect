/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.datacleansing.persistence.DataQueryTypeDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataQueryTypeManager")
public class DataQueryTypeManager extends AbstractSurveyObjectManager<Integer, DataQueryType, DataQueryTypeDao> {
	
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private MessageSource messageSource;
	
	public DataQueryType loadByCode(CollectSurvey survey, String code) {
		return dao.loadByCode(survey, code);
	}
	
	@Override
	public void delete(DataQueryType obj) {
		DataQueryType errorType = loadById((CollectSurvey) obj.getSurvey(), obj.getId());
		checkNotInUse(errorType);
		super.delete(errorType);
	}

	private void checkNotInUse(DataQueryType errorType) {
		List<DataQuery> dataQueries = dataQueryManager.loadByType(errorType);
		if (! dataQueries.isEmpty()) {
			String message = messageSource.getMessage("data_query_type.delete.error.used_by_query", 
					new String[]{errorType.getCode()}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
	
}
