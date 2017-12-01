package org.openforis.collect.model;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class RecordIteratorJob extends Job {
	
	@Autowired
	private RecordManager recordManager;
	
	private RecordFilter recordFilter;
	
	public RecordIteratorJob() {
	}
	
	public RecordIteratorJob(RecordManager recordManager, RecordFilter recordFilter) {
		super();
		this.recordManager = recordManager;
		this.recordFilter = recordFilter;
	}

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new Task() {
			@Override
			protected long countTotalItems() {
				return recordManager.countRecords(recordFilter);
			}
			@Override
			protected void execute() throws Throwable {
				List<CollectRecordSummary> summaries = recordManager.loadSummaries(recordFilter);
				for (CollectRecordSummary summary : summaries) {
					if (this.isRunning()) {
						CollectRecord record = recordManager.load(recordFilter.getSurvey(), summary.getId(), 
								summary.getStep(), false);
						processRecord(record);
						incrementProcessedItems();
					}
				}
			}
		});
	}

	protected abstract void processRecord(CollectRecord record);
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
}
