package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CollectEarthProjectFileCreatorJob extends Job {

	//input
	private CollectSurvey survey;

	//output
	private File outputFile;

	//temp
	private File placemarkTempFile;

	@Override
	protected void initInternal() throws Throwable {
		placemarkTempFile = File.createTempFile("openforis", "collect-earth-placemark.idm.xml");
		outputFile = File.createTempFile("openforis", "collect-earth-temp.zip");
		super.initInternal();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		// download project file
//		Task projectPackageDownloadTask = new Task() {
//			@Override
//			protected void execute() throws Throwable {
//				URL website = new URL(url);
//				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
//				FileOutputStream fos = new FileOutputStream(outputFile);
//				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//				fos.close();
//			}
//		};
//		addTask(projectPackageDownloadTask);
		
		// create placemark
		IdmlExportTask idmlCreatorTask = createTask(IdmlExportTask.class);
		idmlCreatorTask.setSurvey(survey);
		OutputStream placemarkOutputStream = new FileOutputStream(placemarkTempFile);
		idmlCreatorTask.setOutputStream(placemarkOutputStream);
		addTask(idmlCreatorTask);
		
		addBaloonGeneratorTask();
		
		addCubeGeneratorTask();
		
		// create output zip file
		Task outputFileCreator = new Task() {
			@Override
			protected void execute() throws Throwable {
				ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(outputFile);

				ZipParameters zipParameters = new ZipParameters();
				zipParameters.setSourceExternalStream(true);
				zipParameters.setFileNameInZip("placemark.idml.xml");
				zipFile.addFile(placemarkTempFile, zipParameters);
			}
		};
		addTask(outputFileCreator);
	}

	private void addBaloonGeneratorTask() {
		Task baloonCreatorTask = new Task() {
			@Override
			protected void execute() throws Throwable {
				//TODO
			}
		};
		addTask(baloonCreatorTask);
	}

	private void addCubeGeneratorTask() {
		// TODO Auto-generated method stub
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
}