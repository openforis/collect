/**
 * 
 */
package org.openforis.collect.io.proxy;

import java.io.File;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.concurrency.proxy.JobProxy;

/**
 * @author S. Ricci
 *
 */
public class SurveyBackupJobProxy extends JobProxy {

	public SurveyBackupJobProxy(SurveyBackupJob job) {
		super(job);
	}
	
	@ExternalizedProperty
	public String getOutputFileName() {
		SurveyBackupJob job = (SurveyBackupJob) this.getJob();
		File outputFile = job.getOutputFile();
		return outputFile == null ? null: outputFile.getAbsolutePath();
	}

}
