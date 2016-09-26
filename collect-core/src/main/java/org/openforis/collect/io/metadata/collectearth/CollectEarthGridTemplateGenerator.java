package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.IOException;

import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author S. Ricci
 *
 */
public interface CollectEarthGridTemplateGenerator {

	File generateTemplateCSVFile(CollectSurvey survey) throws IOException;

	CSVFileValidationResult validate(File file, CollectSurvey survey);

}