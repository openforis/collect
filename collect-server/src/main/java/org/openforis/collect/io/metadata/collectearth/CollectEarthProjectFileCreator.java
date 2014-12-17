package org.openforis.collect.io.metadata.collectearth;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface CollectEarthProjectFileCreator {
	
	File create(CollectSurvey survey) throws Exception;
	
}
