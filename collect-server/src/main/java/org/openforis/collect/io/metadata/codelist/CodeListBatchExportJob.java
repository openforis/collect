package org.openforis.collect.io.metadata.codelist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.openforis.idm.metamodel.CodeList;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListBatchExportJob extends Job {

	//input
	private CollectSurvey survey;
	private CodeListManager codeListManager;
	
	//output
	private File outputFile;
	private ZipOutputStream zipOutputStream;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		outputFile = File.createTempFile("batch_code_list_export", ".zip");
		zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		CodeList samplingDesignCodeList = survey.getSamplingDesignCodeList();
		
		for (CodeList codeList : survey.getCodeLists()) {
			if (codeList.getId() != samplingDesignCodeList.getId()) {
				addCodeListExportTask(codeList);
			}
		}
	}

	private void addCodeListExportTask(CodeList codeList) {
		CodeListExportTask t = new CodeListExportTask();
		t.setOut(zipOutputStream);
		t.setList(codeList);
		t.setCodeListManager(codeListManager);
		addTask(t);
	}
	
	@Override
	protected void initializeTask(Worker task) {
		CodeList codeList = ((CodeListExportTask) task).getList();
		String zipEntryName = codeList.getName() + ".csv";
		try {
			zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		super.initializeTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		super.onTaskCompleted(task);
		try {
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		IOUtils.closeQuietly(zipOutputStream);
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
}
