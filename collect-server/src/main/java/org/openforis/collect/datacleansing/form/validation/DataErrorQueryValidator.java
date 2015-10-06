package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.form.DataErrorQueryForm;
import org.openforis.collect.datacleansing.manager.DataErrorQueryManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataErrorQueryValidator extends SimpleValidator<DataErrorQueryForm> {

	private static final String TYPE_ID_FIELD = "typeId";
	private static final String QUERY_ID_FIELD = "queryId";
	
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	
	@Override
	public void validateForm(DataErrorQueryForm target, Errors errors) {
		if (validateRequiredFields(errors, TYPE_ID_FIELD, QUERY_ID_FIELD)) {
			validateUniqueness(target, errors);
		}
	}

	private boolean validateUniqueness(DataErrorQueryForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataErrorQuery> errorQueries = dataErrorQueryManager.loadBySurvey(survey);
		for (DataErrorQuery dataErrorQuery : errorQueries) {
			if (! dataErrorQuery.getId().equals(target.getId()) && 
					dataErrorQuery.getQueryId().equals(target.getQueryId()) && 
					dataErrorQuery.getTypeId().equals(target.getTypeId())) {
				rejectDuplicateValue(errors, QUERY_ID_FIELD);
				rejectDuplicateValue(errors, TYPE_ID_FIELD);
				return false;
			}
		}
		return true;
	}

}
