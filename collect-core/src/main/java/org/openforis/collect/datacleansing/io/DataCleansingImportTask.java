package org.openforis.collect.datacleansing.io;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataCleansingImportTask {
	
	void setSurvey(CollectSurvey survey);
	
	void setInputFile(File inputFile);
	
	void setActiveUser(User user);
	
}
