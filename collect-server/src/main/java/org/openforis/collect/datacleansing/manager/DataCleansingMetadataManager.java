package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataCleansingMetadataManager extends SurveyDataCleansingManager {

	DataCleansingMetadata loadMetadata(CollectSurvey survey);

	void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata, boolean skipErrors, User activeUser);
	
}