package org.openforis.collect.io.metadata.collectearth;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface CollectEarthProjectFileCreator {
	
	public static final String PLACEHOLDER_FOR_EXTRA_CSV_DATA = "PLACEHOLDER_FOR_EXTRA_CSV_DATA";
	public static final String PLACEHOLDER_FOR_EXTRA_COLUMNS_HEADER = "PLACEHOLDER_FOR_EXTRA_COLUMNS_HEADER";
	public static final String PLACEHOLDER_FOR_EXTRA_COLUMNS_VALUES = "PLACEHOLDER_FOR_EXTRA_COLUMNS_VALUES";
	
	File create(CollectSurvey survey) throws Exception;
	
}
