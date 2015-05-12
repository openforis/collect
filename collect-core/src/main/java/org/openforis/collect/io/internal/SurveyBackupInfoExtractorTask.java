/**
 * 
 */
package org.openforis.collect.io.internal;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.SurveyBackupInfo;
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
public class SurveyBackupInfoExtractorTask extends Task {

	//input
	private File file;

	//output
	private SurveyBackupInfo info;

	@Override
	protected void execute() throws Throwable {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			info = SurveyBackupInfo.parse(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public SurveyBackupInfo getInfo() {
		return info;
	}
}
