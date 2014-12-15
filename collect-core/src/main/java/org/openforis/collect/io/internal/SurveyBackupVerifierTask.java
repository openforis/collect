/**
 * 
 */
package org.openforis.collect.io.internal;

import java.util.zip.ZipFile;

import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
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
public class SurveyBackupVerifierTask extends Task {

	//input
	private ZipFile zipFile;
	
	@Override
	protected void execute() throws Throwable {
		BackupFileExtractor fileExtractor = new BackupFileExtractor(zipFile);
		checkEntryExists(fileExtractor, SurveyBackupJob.INFO_FILE_NAME);
		checkEntryExists(fileExtractor, SurveyBackupJob.SURVEY_XML_ENTRY_NAME);
	}

	protected void checkEntryExists(BackupFileExtractor fileExtractor,
			String entryName) {
		if ( ! fileExtractor.containsEntry(entryName) ) {
			throw new RuntimeException("Invalid backup file - missing file: " + entryName);
		}
	}

	public ZipFile getZipFile() {
		return zipFile;
	}
	
	public void setZipFile(ZipFile zipFile) {
		this.zipFile = zipFile;
	}
	
}
