package org.openforis.collect.datacleansing.io;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataCleansingImportTask {
	
	void setSurvey(CollectSurvey survey);
	
	void setInputFile(File inputFile);
	
}
