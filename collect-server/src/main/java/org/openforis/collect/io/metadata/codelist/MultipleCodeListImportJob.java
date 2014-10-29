/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class MultipleCodeListImportJob extends Job {

	//input
	private CodeListManager codeListManager;
	private CollectSurvey survey;
	private File file;
	private boolean overwriteData;
	
	//transient
	private ZipFile zipFile;

	@Override
	protected void initInternal() throws Throwable {
		zipFile = new ZipFile(file);
		super.initInternal();
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
			String entryName = entry.getName();
			if ("csv".equalsIgnoreCase(FilenameUtils.getExtension(entryName))) {
				addCodeListImportTask(FilenameUtils.getBaseName(entryName));
			}
		}
	}

	private void addCodeListImportTask(String codeListName) {
		CodeList codeList = getOrCreateCodeList(codeListName);
		CodeListImportTask task = createTask(CodeListImportTask.class);
		task.setCodeListManager(codeListManager);
		task.setCodeList(codeList);
		task.setFile(file);
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
