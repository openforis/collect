/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.LookupProvider;
import org.openforis.idm.model.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class DatabaseLookupProvider implements LookupProvider {

	private static final String SURVEY_ID_FIELD = "survey_id";
	private static final String SURVEY_WORK_ID_FIELD = "survey_work_id";

	@Autowired
	private DynamicTableDao dynamicTableDao;

	@Override
	public Object lookup(Survey survey, String name, String attribute, Object... columns) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		if ( survey instanceof CollectSurvey ) {
			filters.add(createSurveyFilter((CollectSurvey) survey));
		}
		filters.addAll(Arrays.asList(NameValueEntry.fromKeyValuePairs(columns)));
		Object object = dynamicTableDao.loadValue(name, attribute, filters.toArray(new NameValueEntry[0]));
		if(object != null){
			Coordinate coordinate = Coordinate.parseCoordinate(object.toString());
			return coordinate;
		}
		return null;
	}

	protected NameValueEntry createSurveyFilter(CollectSurvey survey) {
		Integer surveyId = survey.getId();
		if ( surveyId != null ) {
			String surveyIdFieldName = survey.isWork() ? SURVEY_WORK_ID_FIELD : SURVEY_ID_FIELD;
			NameValueEntry keyValue = new NameValueEntry(surveyIdFieldName, surveyId.toString());
			return keyValue;
		} else {
			return null;
		}
	}
}
