package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.concurrency.Job;
import org.openforis.idm.metamodel.CodeList;


/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListExportJob extends Job {

	private CodeList list;
	private CodeListManager codeListManager;
	
	private File outputFile;
	private FileOutputStream outputStream;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		outputFile = File.createTempFile("batch_code_list_export", ".zip");
		outputStream = new FileOutputStream(outputFile);
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		CodeListExportTask t = createTask(CodeListExportTask.class);
		t.setOut(outputStream);
		t.setCodeListManager(codeListManager);
		t.setList(list);
		addTask(t);
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		IOUtils.closeQuietly(outputStream);
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setList(CodeList list) {
		this.list = list;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}
