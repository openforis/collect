/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class CodeListImportJob extends Job {

	private static final String CSV = "csv";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "codeListImport.error.internalErrorImportingFile";
	private static final String WRONG_FILE_TYPE_ERROR_MESSAGE_KEY = "codeListImport.error.wrongFileType";
	
	//input
	private CodeListManager codeListManager;
	private File file;
	private CodeList codeList;
	private boolean overwriteData;
	
	private InputStream is;

	@Override
	protected void initInternal() throws Throwable {
		validateParameters();
		is = new FileInputStream(file);
		super.initInternal();
		
	}
	
	private void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		} else if (!validateFile()) {
			setErrorMessage(WRONG_FILE_TYPE_ERROR_MESSAGE_KEY);
		}
	}
	
	private boolean validateFile() {
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			return true;
		} else {
			String errorMessage = "File type not supported" + extension;
			setErrorMessage(errorMessage);
			return false;
		}
	}

	@Override
	protected void buildTasks() throws Throwable {
		addTask(CodeListImportTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		CodeListImportTask t = (CodeListImportTask) task;
		t.setCodeListManager(codeListManager);
		t.setInputStream(is);
		t.setCodeList(codeList);
		t.setOverwriteData(overwriteData);
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		IOUtils.closeQuietly(is);
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setCodeList(CodeList codeList) {
		this.codeList = codeList;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public void setOverwriteData(boolean overwriteData) {
		this.overwriteData = overwriteData;
	}
	
}
