package org.openforis.collect.datacleansing.form.validation;

import java.util.List;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.form.DataQueryGroupForm;
import org.openforis.collect.datacleansing.manager.DataQueryGroupManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataQueryGroupValidator extends SimpleValidator<DataQueryGroupForm> {

	private static final String TITLE_FIELD = "title";
	private static final String QUERY_IDS_FIELD = "queryIds";
	
	@Autowired
	private DataQueryGroupManager dataQueryGroupManager;
	
	@Override
	public void validateForm(DataQueryGroupForm target, Errors errors) {
		if (validateRequiredField(errors, TITLE_FIELD)) {
			validateTitleUniqueness(target, errors);
		}
		if (validateRequiredField(errors, QUERY_IDS_FIELD)) {
			validateQueryUniqueness(target, errors);
		}
	}

	private boolean validateQueryUniqueness(DataQueryGroupForm target, Errors errors) {
		CollectSurvey survey = getActiveSurvey();
		List<DataQueryGroup> items = dataQueryGroupManager.loadBySurvey(survey);
		for (DataQueryGroup item : items) {
			if (! item.getId().equals(target.getId())) {
				List<DataQuery> queries = item.getQueries();
				List<Integer> queryIds = CollectionUtils.project(queries, "id");
				if (queryIds.equals(target.getQueryIds())) {
					rejectDuplicateValue(errors, QUERY_IDS_FIELD);
					return false;
				}
			}
		}
		return true;
	}

	private boolean validateTitleUniqueness(DataQueryGroupForm target, Errors errors) {
		String title = target.getTitle();
		CollectSurvey survey = getActiveSurvey();
		List<DataQueryGroup> items = dataQueryGroupManager.loadBySurvey(survey);
		for (DataQueryGroup item : items) {
			if (! item.getId().equals(target.getId()) && item.getTitle().equalsIgnoreCase(title)) {
				rejectDuplicateValue(errors, TITLE_FIELD);
				return false;
			}
		}
		return true;
	}


}
