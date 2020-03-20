/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class CodeListImportJob extends Job {

	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "codeListImport.error.internalErrorImportingFile";
	private static final String WRONG_FILE_TYPE_ERROR_MESSAGE_KEY = "codeListImport.error.wrongFileType";
	
	//input
	private CodeListManager codeListManager;
	private File file;
	private CodeList codeList;
	private boolean overwriteData;
	
	private InputStream is;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		is = new FileInputStream(file);
	}
	
	@Override
	protected void validateInput() throws Throwable {
		if ( ! file.exists() && ! file.canRead() ) {
			setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
			changeStatus(Status.FAILED);
		} else if (!validateFile()) {
			setErrorMessage(WRONG_FILE_TYPE_ERROR_MESSAGE_KEY);
			changeStatus(Status.FAILED);
		}
	}
	
	private boolean validateFile() {
		return true;
	}

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new CodeListImportTask());
	}
	
	@Override
	protected void initializeTask(Worker task) {
		CodeListImportTask t = (CodeListImportTask) task;
		t.setCodeListManager(codeListManager);
		t.setInputStream(is);
		t.setCodeList(codeList);
		t.setOverwriteData(overwriteData);
		super.initializeTask(t);
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
