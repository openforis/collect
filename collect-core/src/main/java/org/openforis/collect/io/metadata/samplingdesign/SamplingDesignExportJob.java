package org.openforis.collect.io.metadata.samplingdesign;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignExportTask.OutputFormat;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SamplingDesignExportJob extends Job {
	
	private SamplingDesignManager samplingDesignManager;
	
	// Input
	private CollectSurvey survey;
	private OutputFormat outputFormat;

	// Output
	private File outputFile;
	private OutputStream outputStream;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		String extension = outputFormat == OutputFormat.CSV ? ".csv" : ".xlsx";
		outputFile = File.createTempFile("sampling_point_data_export", extension);
		outputStream = new FileOutputStream(outputFile);
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		SamplingDesignExportTask t = createTask(SamplingDesignExportTask.class);
		t.setSamplingDesignManager(samplingDesignManager);
		t.setSurvey(survey);
		t.setOutputStream(outputStream);
		t.setOutputFormat(outputFormat);
		addTask(t);
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		IOUtils.closeQuietly(outputStream);
	}
	
	public void setSamplingDesignManager(SamplingDesignManager samplingDesignManager) {
		this.samplingDesignManager = samplingDesignManager;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setOutputFormat(OutputFormat outputFormat) {
		this.outputFormat = outputFormat;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	
}
