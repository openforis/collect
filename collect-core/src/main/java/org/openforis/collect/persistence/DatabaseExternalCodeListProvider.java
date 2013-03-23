/**
 * 
 */
package org.openforis.collect.persistence;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class DatabaseExternalCodeListProvider implements
		ExternalCodeListProvider {

	@Autowired
	private DynamicTableDao dynamicTableDao;
	@Autowired
	private SurveyManager surveyManager;

	@Override
	public String getCode(CodeList list, String attribute, Object... keys) {
		String listName = list.getName();
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		int surveyId = survey.getId();
		boolean surveyWork = surveyManager.isSurveyWork(survey);
		Object object = dynamicTableDao.load(surveyId, surveyWork, listName, attribute, keys);
		if (object == null) {
			return null;
		} else {
			return object.toString();
		}
	}

}
