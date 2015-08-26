package org.openforis.collect.datacleansing.manager;

import org.openforis.collect.datacleansing.DataCleansingMetadata;
import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataCleansingMetadataManager extends SurveyDataCleansingManager {

	DataCleansingMetadata loadMetadata(CollectSurvey survey);

	void saveMetadata(CollectSurvey survey, DataCleansingMetadata metadata);

}