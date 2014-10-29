/**
 * 
 */
package org.openforis.collect.io.metadata.codelist;

import java.io.File;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.CodeList;

/**
 * @author S. Ricci
 *
 */
public class SingleCodeListImportJob extends Job {

	//input
	private CodeListManager codeListManager;
	private File file;
	private CodeList codeList;
	private boolean overwriteData;

	@Override
	protected void buildTasks() throws Throwable {
		addTask(CodeListImportTask.class);
	}
	
	@Override
	protected void prepareTask(Task task) {
		CodeListImportTask t = (CodeListImportTask) task;
		t.setCodeListManager(codeListManager);
		t.setFile(file);
		t.setCodeList(codeList);
		t.setOverwriteData(overwriteData);
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
