package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExectutorTask;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExectutorTask.DataQueryExecutorTaskInput;
import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author A. Modragon
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataErrorReportGeneratorJob extends Job {
	
	@Autowired
	private DataErrorReportManager reportManager;
	
	//input
	private DataErrorQuery errorQuery;
	private Step recordStep;

	//output
	private DataErrorReport report;
	
	private transient ItemBatchPersister batchPersister;
	
	@Override
	protected void buildTasks() throws Throwable {
		DataQueryExectutorTask task = addTask(DataQueryExectutorTask.class);
		task.setInput(new DataQueryExecutorTaskInput(errorQuery.getQuery(), recordStep, new ReportItemPersisterNodeProcessor()));
	}
	
	@Override
	protected void prepareTask(Task task) {
		super.prepareTask(task);
		report = new DataErrorReport((CollectSurvey) errorQuery.getSurvey());
		report.setQuery(errorQuery);
		reportManager.save(report);
		batchPersister = new ItemBatchPersister(report);
	}
	
	@Override
	protected void onTaskCompleted(Task task) {
		super.onTaskCompleted(task);
		batchPersister.flush();
	}
	
	public void setErrorQuery(DataErrorQuery errorQuery) {
		this.errorQuery = errorQuery;
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	public DataErrorReport getReport() {
		return report;
	}
	
	private class ReportItemPersisterNodeProcessor implements NodeProcessor {
		
		@Override
		public void process(Node<?> node) throws Exception {
			Attribute<?, ?> attr = (Attribute<?, ?>) node;
			DataErrorReportItem item = createReportItem(report, attr);
			batchPersister.add(item);
		}
	}
	
	private DataErrorReportItem createReportItem(DataErrorReport report,
			Attribute<?, ?> attr) {
		DataErrorReportItem item = new DataErrorReportItem(report);
		item.setRecordId(attr.getRecord().getId());
		item.setParentEntityId(attr.getParent().getInternalId());
		item.setNodeIndex(attr.getIndex());
		item.setValue(new JSONValueFormatter().formatValue(attr));
		item.setStatus(org.openforis.collect.datacleansing.DataErrorReportItem.Status.PENDING);
		return item;
	}

	private class ItemBatchPersister {
		
		private static final int MAX_SIZE = 10000;
		
		private List<DataErrorReportItem> items;

		private DataErrorReport report;
		
		public ItemBatchPersister(DataErrorReport report) {
			items = new ArrayList<DataErrorReportItem>();
		}
		
		public void add(DataErrorReportItem item) {
			items.add(item);
			if (items.size() > MAX_SIZE) {
				flush();
			}
		}

		public void flush() {
			if (! items.isEmpty()) {
				reportManager.saveItems(report, items);
				items.clear();
			}
		}
		
	}
	
}
