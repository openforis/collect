package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;

/**
 * 
 * @author S. Ricci
 *
 */
public interface SurveyDataCleansingManager {

	void moveMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey, User activeUser);

	void deleteMetadata(CollectSurvey survey);
	
	void duplicateMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey, User activeUser);

}