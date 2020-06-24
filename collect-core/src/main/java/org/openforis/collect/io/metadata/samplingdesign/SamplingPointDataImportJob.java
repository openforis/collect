/**
 * 
 */
package org.openforis.collect.io.metadata.samplingdesign;

import org.openforis.collect.io.metadata.ReferenceDataImportSimpleJob;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SamplingPointDataImportJob extends ReferenceDataImportSimpleJob<ParsingError, SamplingDesignImportTask> {

	@Override
	protected void buildTasks() throws Throwable {
		SamplingDesignImportTask task = createTask(SamplingDesignImportTask.class);
		task.setFile(file);
		task.setSurvey(survey);
		task.setOverwriteAll(true);
		addTask(task);
	}

}
