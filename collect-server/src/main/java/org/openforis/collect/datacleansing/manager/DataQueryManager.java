/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.persistence.DataQueryDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataQueryManager")
public class DataQueryManager extends AbstractSurveyObjectManager<DataQuery, DataQueryDao> {

	@Autowired
	private DataCleansingStepManager dataCleansingStepManager;
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private MessageSource messageSource;
	
	@Override
	@Autowired
	@Qualifier("dataQueryDao")
	public void setDao(DataQueryDao dao) {
		super.setDao(dao);
	}

	@Override
	public void delete(DataQuery query) {
		checkNotExistsCalculationStepUsingQuery(query);
		checkNotExistsErrorQueryUsingQuery(query);
		super.delete(query);
	}

	private void checkNotExistsCalculationStepUsingQuery(DataQuery query) {
		List<DataCleansingStep> steps = dataCleansingStepManager.loadByQuery(query);
		if (! steps.isEmpty()) {
			String message = messageSource.getMessage("data_query.delete.error.used_by_data_cleansing_step", new String[]{query.getTitle()}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
	
	private void checkNotExistsErrorQueryUsingQuery(DataQuery query) {
		List<DataErrorQuery> errorQueries = dataErrorQueryManager.loadByQuery(query);
		if (! errorQueries.isEmpty()) {
			String message = messageSource.getMessage("data_query.delete.error.used_by_error_query", new String[]{query.getTitle()}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
}
