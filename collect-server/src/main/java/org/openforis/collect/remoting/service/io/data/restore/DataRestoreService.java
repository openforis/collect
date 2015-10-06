package org.openforis.collect.remoting.service.io.data.restore;

import java.io.File;

/**
 * 
 * @author S. Ricci
 *
 */
public interface DataRestoreService {

	String startSurveyDataRestore(String surveyName, File backupFile);
	
}
