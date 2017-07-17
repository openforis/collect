/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.Survey;

/**
 * @author S. Ricci
 *
 */
public class CodeListBatchImportJob extends Job {

	private static final String ZIP = "zip";
	private static final String CSV = "csv";
	
	//input
	private CodeListManager codeListManager;
	private CollectSurvey survey;
	private File file;
	private boolean overwriteData;
	
	//transient
	private ZipFile zipFile;
	private Enumeration<ZipArchiveEntry> zipEntries;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		zipFile = new ZipFile(file);
		zipEntries = zipFile.getEntries();
	}
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		if (!validateExtension(file.getName(), ZIP)) {
			throw new IllegalArgumentException("survey.code_list.import_data.error.invalid_extension");
		}
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
			if (!entries.hasMoreElements()) {
				throw new IllegalArgumentException("survey.code_list.import_data.error.empty_file");
			} else {
				while (entries.hasMoreElements()) {
					ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
					String entryName = entry.getName();
					if (!validateExtension(entryName, CSV)) {
						throw new IllegalArgumentException("survey.code_list.import_data.error.invalid_extension");
					} else if (!FilenameUtils.getBaseName(entryName).matches(Survey.INTERNAL_NAME_REGEX) ) {
						throw new IllegalArgumentException("survey.code_list.import_data.error.invalid_filename");
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
	}

	private boolean validateExtension(String fileName, String expectedExtension) {
		return expectedExtension.equals(FilenameUtils.getExtension(fileName));
	}

	@Override
	protected void buildTasks() throws Throwable {
		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
			String entryName = entry.getName();
			if (CSV.equalsIgnoreCase(FilenameUtils.getExtension(entryName))) {
				addCodeListImportTask(FilenameUtils.getBaseName(entryName));
			}
		}
	}

	@Override
	protected void initializeTask(Worker task) {
		try {
			ZipArchiveEntry entry = zipEntries.nextElement();
			InputStream is = zipFile.getInputStream(entry);
			((CodeListImportTask) task).setInputStream(is);
			((CodeListImportTask) task).setEntryName(entry.getName());
			super.initializeTask(task);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void addCodeListImportTask(String codeListName) {
		CodeList codeList = getOrCreateCodeList(codeListName);
		CodeListImportTask task = new CodeListImportTask();
		task.setCodeListManager(codeListManager);
		task.setCodeList(codeList);
		task.setOverwriteData(overwriteData);
		addTask(task);
	}

	private CodeList getOrCreateCodeList(String codeListName) {
		CodeList codeList = survey.getCodeList(codeListName);
		if (codeList == null) {
			codeList = survey.createCodeList();
			codeList.setName(codeListName);
			survey.addCodeList(codeList);
		}
		return codeList;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setOverwriteData(boolean overwriteData) {
		this.overwriteData = overwriteData;
	}
	
}
