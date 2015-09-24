/**
 * 
 */
package org.openforis.collect.datacleansing.manager;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.persistence.DataErrorTypeDao;
import org.openforis.collect.manager.AbstractSurveyObjectManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component("dataErrorTypeManager")
public class DataErrorTypeManager extends AbstractSurveyObjectManager<DataErrorType, DataErrorTypeDao> {
	
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private MessageSource messageSource;
	
	@Override
	@Autowired
	@Qualifier("dataErrorTypeDao")
	public void setDao(DataErrorTypeDao dao) {
		super.setDao(dao);
	}
	
	public DataErrorType loadByCode(CollectSurvey survey, String code) {
		return dao.loadByCode(survey, code);
	}
	
	@Override
	public void delete(DataErrorType obj) {
		DataErrorType errorType = loadById((CollectSurvey) obj.getSurvey(), obj.getId());
		checkNotInUse(errorType);
		super.delete(errorType);
	}

	private void checkNotInUse(DataErrorType errorType) {
		List<DataErrorQuery> dataErrorQueries = dataErrorQueryManager.loadByType(errorType);
		if (! dataErrorQueries.isEmpty()) {
			String message = messageSource.getMessage("data_error_type.delete.error.used_by_error_query", 
					new String[]{errorType.getCode()}, Locale.ENGLISH);
			throw new IllegalStateException(message);
		}
	}
	
}
