/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

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
	private static final String XLS = "xls";
	private static final String XLSX = "xlsx";
	private static final String MACOSX_HIDDEN_ENTRY_PREFIX = "__MACOSX/";
	private static final List<String> VALID_EXTENSIONS = Arrays.<String>asList(CSV, XLS, XLSX);
	
	//input
	private CodeListManager codeListManager;
	private CollectSurvey survey;
	private File file;
	private boolean overwriteData;
	
	//transient
	private ZipFile zipFile;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		zipFile = new ZipFile(file);
	}
	
	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		if (!hasExtension(file.getName(), ZIP)) {
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
					if (canSkipEntry(entryName)) {
						// ignore it
					} else if (!hasValidCodeListExtension(entryName)) {
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

	private boolean canSkipEntry(String entryName) {
		return entryName.startsWith(MACOSX_HIDDEN_ENTRY_PREFIX);
	}

	private boolean hasExtension(String fileName, String expectedExtension) {
		return expectedExtension.equalsIgnoreCase(FilenameUtils.getExtension(fileName));
	}
	
	private boolean hasValidCodeListExtension(String fileName) {
		for (String validExtension : VALID_EXTENSIONS) {
			if (hasExtension(fileName, validExtension)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void buildTasks() throws Throwable {
		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
			String entryName = entry.getName();
			if (!canSkipEntry(entryName) && hasValidCodeListExtension(entryName)) {
				addCodeListImportTask(entryName);
			}
		}
	}

	@Override
	protected void initializeTask(Worker task) {
		try {
			CodeListImportTask importTask = (CodeListImportTask) task;
			String entryName = importTask.getEntryName();
			ZipArchiveEntry zipEntry = zipFile.getEntry(entryName);
			InputStream is = zipFile.getInputStream(zipEntry);
			importTask.setInputStream(is);
			super.initializeTask(task);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void addCodeListImportTask(String entryName) {
		String codeListName = FilenameUtils.getBaseName(entryName);
		CodeList codeList = getOrCreateCodeList(codeListName);
		CodeListImportTask task = new CodeListImportTask();
		task.setCodeListManager(codeListManager);
		task.setCodeList(codeList);
		task.setOverwriteData(overwriteData);
		task.setEntryName(entryName);
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
