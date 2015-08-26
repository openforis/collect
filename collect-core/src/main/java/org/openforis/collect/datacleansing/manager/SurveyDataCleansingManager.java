package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface SurveyDataCleansingManager {

	void moveMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey);

	void deleteMetadata(CollectSurvey survey);
	
	void duplicateMetadata(CollectSurvey fromSurvey, CollectSurvey toSurvey);

}