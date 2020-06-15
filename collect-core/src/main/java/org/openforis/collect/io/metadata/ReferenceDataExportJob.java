package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;

public abstract class ReferenceDataExportJob extends Job {

	// Input
	protected String tempFilePrefix = "refData";
	protected CollectSurvey survey;
	protected ReferenceDataExportOutputFormat outputFormat;

	// Output
	protected File outputFile;
	protected OutputStream outputStream;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		outputFile = File.createTempFile(tempFilePrefix, "." + outputFormat.getFileExtesion());
		outputStream = new FileOutputStream(outputFile);
	}

	@Override
	protected void onCompleted() {
		super.onCompleted();
		IOUtils.closeQuietly(outputStream);
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public void setOutputFormat(ReferenceDataExportOutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}

	public File getOutputFile() {
		return outputFile;
	}

}
