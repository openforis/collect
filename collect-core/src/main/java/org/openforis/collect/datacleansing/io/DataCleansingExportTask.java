package org.openforis.collect.datacleansing.io;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataCleansingExportTask {

	void setSurvey(CollectSurvey survey);
	
	File getResultFile();
	
}