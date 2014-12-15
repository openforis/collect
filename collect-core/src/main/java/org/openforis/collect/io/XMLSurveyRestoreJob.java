/**
 * 
 */
package org.openforis.collect.io;

import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XMLSurveyRestoreJob extends AbstractSurveyRestoreJob {

	@Override
	protected void buildTasks() throws Throwable {
		if ( surveyUri == null ) {
			//unmarshall xml file to get survey uri
			addTask(IdmlUnmarshallTask.class);
		}
		addTask(IdmlImportTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		if ( task instanceof IdmlUnmarshallTask ) {
			IdmlUnmarshallTask t = (IdmlUnmarshallTask) task;
			t.setFile(file);
			t.setSurveyManager(surveyManager);
			t.setValidate(false);
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			t.setSurveyManager(surveyManager);
			t.setFile(file);
			t.setSurveyUri(surveyUri);
			t.setSurveyName(surveyName);
			t.setImportInPublishedSurvey(restoreIntoPublishedSurvey);
			t.setValidate(validateSurvey);
		}
		super.prepareTask(task);
	}
	
}
