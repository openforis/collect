package org.openforis.collect.io.metadata;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;

public abstract class ReferenceDataImportSimpleJob<E extends ParsingError, T extends ReferenceDataImportTask<E>>
		extends Job {

	protected File file;
	protected CollectSurvey survey;

	public void setFile(File file) {
		this.file = file;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public List<E> getErrors() {
		T task = getTask();
		return task == null ? Collections.<E>emptyList() : task.getErrors();
	}

	public boolean hasErrors() {
		T task = getTask();
		return task == null ? true : task.hasErrors();
	}

	public List<Long> getProcessedRows() {
		T task = getTask();
		return task == null ? Collections.<Long>emptyList() : task.getProcessedRows();
	}

	public long getProcessedItems() {
		T task = getTask();
		return task == null ? 0 : task.getProcessedItems(); 
	}
	
	public boolean isRowProcessed(long rowNumber) {
		T task = getTask();
		return task == null ? false : task.isRowProcessed(rowNumber);
	}

	public boolean isRowInError(long rowNumber) {
		T task = getTask();
		return task == null ? false : task.isRowInError(rowNumber);
	}

	public Collection<Long> getRowsInError() {
		T task = getTask();
		return task == null ? Collections.<Long>emptyList() : task.getRowsInError();
	}

	public List<Long> getSkippedRows() {
		T task = getTask();
		return task == null ? Collections.<Long>emptyList() : task.getSkippedRows();
	}

	@SuppressWarnings("unchecked")
	private T getTask() {
		List<Worker> tasks = getTasks();
		return tasks.isEmpty() ? null : (T) tasks.get(0);
	}
}
