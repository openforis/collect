package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.SurveyRestoreJob;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.concurrency.Task;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectMobileBackupConvertTask extends Task {

	private static final String SERVER_APPLICATION_CONTEXT_FILE_NAME = "org/openforis/collect/application-context-server-base.xml";
	private static final String DATASOURCE_TEMPLATE_FILE_NAME = "org/openforis/collect/application-context-datasource-template.xml";
	private static final String COLLECT_DB_FILE_NAME = "collect.db";
	// input
	private File collectBackupFile;
	private String surveyName;

	// output
	private File outputDbFile;
	private File outputFile;

	// transient
	private ConfigurableApplicationContext ctx;
	private UserGroupManager userGroupManager;

	@Override
	protected void createInternalVariables() throws Throwable {
		outputDbFile = File.createTempFile("collect_mobile_" + surveyName, ".db");

		// initialize application context
		File dataSourceConfigFile = createDataSourceConfigFile();
		File serverApplicationContextFile = getResourceAsFile(SERVER_APPLICATION_CONTEXT_FILE_NAME);

		ctx = new FileSystemXmlApplicationContext(
				"/" + serverApplicationContextFile.getAbsolutePath(),
				"/" + dataSourceConfigFile.getAbsolutePath());

		userGroupManager = ctx.getBean(UserGroupManager.class);

		super.createInternalVariables();
	}

	private File createDataSourceConfigFile() throws IOException {
		InputStream templateFileIS = this.getClass().getClassLoader()
				.getResourceAsStream(DATASOURCE_TEMPLATE_FILE_NAME);
		StringWriter writer = new StringWriter();
		IOUtils.copy(templateFileIS, writer, OpenForisIOUtils.UTF_8);
		String dataSourceTemplate = writer.toString();
		String dataSourceConfig = dataSourceTemplate.replace("${TARGET_DB_FILE}", outputDbFile.getAbsolutePath());
		File dataSourceConfigFile = File.createTempFile("application-context-datasource", ".xml");
		FileUtils.writeStringToFile(dataSourceConfigFile, dataSourceConfig, OpenForisIOUtils.UTF_8);
		return dataSourceConfigFile;
	}

	private File getResourceAsFile(String fileName) {
		InputStream serverApplicationContextIS = this.getClass().getClassLoader().getResourceAsStream(fileName);
		return OpenForisIOUtils.copyToTempFile(serverApplicationContextIS);
	}

	@Override
	protected long countTotalItems() {
		return 2;
	}

	@Override
	protected void execute() throws Throwable {
		// import survey into db file
		CollectJobManager jobManager = (CollectJobManager) ctx.getBean("jobManager");
		SurveyRestoreJob restoreJob = ctx.getBean(SurveyRestoreJob.class);

		restoreJob.setFile(collectBackupFile);
		restoreJob.setSurveyName(surveyName);
		restoreJob.setUserGroup(userGroupManager.getDefaultPublicUserGroup());
		restoreJob.setRestoreIntoPublishedSurvey(true);
		restoreJob.setValidateSurvey(false);
		jobManager.start(restoreJob, false);

		incrementProcessedItems();

		if (restoreJob.isCompleted()) {
			createOutpuFile();
			incrementProcessedItems();
		} else {
			changeStatus(Status.FAILED);
			setErrorMessage(restoreJob.getErrorMessage());
			setLastException(restoreJob.getLastException());
		}
	}

	/**
	 * Compress the database file
	 */
	private void createOutpuFile() throws IOException, FileNotFoundException {
		ZipOutputStream zipOutputStream = null;
		BackupFileExtractor backupFileExtractor = null;
		try {
			backupFileExtractor = new BackupFileExtractor(collectBackupFile);

			outputFile = File.createTempFile("collect_" + surveyName, ".zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));

			// include collect.db file
			zipOutputStream.putNextEntry(new ZipEntry(COLLECT_DB_FILE_NAME));
			FileInputStream dbFileIS = new FileInputStream(outputDbFile);
			IOUtils.copy(dbFileIS, zipOutputStream);
			zipOutputStream.closeEntry();

			// include info.properties file
			File infoFile = backupFileExtractor.extract(SurveyBackupJob.INFO_FILE_NAME);
			zipOutputStream.putNextEntry(new ZipEntry(SurveyBackupJob.INFO_FILE_NAME));
			IOUtils.copy(new FileInputStream(infoFile), zipOutputStream);
			zipOutputStream.closeEntry();
		} finally {
			IOUtils.closeQuietly(backupFileExtractor);
			IOUtils.closeQuietly(zipOutputStream);
		}
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		ctx.close();
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setCollectBackupFile(File collectBackupFile) {
		this.collectBackupFile = collectBackupFile;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

}
