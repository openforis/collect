package org.openforis.collect.datacleansing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
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
	private DataErrorQueryGroup errorQueryGroup;
	private Step recordStep;

	//output
	private DataErrorReport report;
	
	private transient ItemBatchPersister batchPersister;
	
	@Override
	protected void buildTasks() throws Throwable {
		DataErrorQueryGroupExectutorTask task = addTask(DataErrorQueryGroupExectutorTask.class);
		task.setInput(new DataErrorQueryGroupExectutorTask.DataErrorQueryGroupExecutorTaskInput((CollectSurvey) errorQueryGroup.getSurvey(), 
				errorQueryGroup.getQueries(), recordStep, new ReportItemPersisterNodeProcessor()));
	}
	
	@Override
	protected void initializeTask(Worker task) {
		super.initializeTask(task);
		report = new DataErrorReport((CollectSurvey) errorQueryGroup.getSurvey());
		report.setQueryGroup(errorQueryGroup);
		report.setRecordStep(recordStep);
		reportManager.save(report);
		batchPersister = new ItemBatchPersister(report);
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		batchPersister.flush();
	}
	
	public void setErrorQueryGroup(DataErrorQueryGroup errorQueryGroup) {
		this.errorQueryGroup = errorQueryGroup;
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	public DataErrorReport getReport() {
		return report;
	}
	
	private class ReportItemPersisterNodeProcessor implements DataErrorQueryResultProcessor {
		
		@Override
		public void init() throws Exception {}
		
		@Override
		public void process(DataErrorQuery query, Node<?> node) throws Exception {
			Attribute<?, ?> attr = (Attribute<?, ?>) node;
			DataErrorReportItem item = createReportItem(report, query, attr);
			batchPersister.add(item);
		}
		
		@Override
		public void close() throws IOException {
			batchPersister.flush();
		}
	}
	
	private DataErrorReportItem createReportItem(DataErrorReport report,
			DataErrorQuery query, Attribute<?, ?> attr) {
		DataErrorReportItem item = new DataErrorReportItem(report, query);
		item.setRecordId(attr.getRecord().getId());
		item.setParentEntityId(attr.getParent().getInternalId());
		item.setNodeIndex(attr.getIndex());
		item.setValue(new JSONValueFormatter().formatValue(attr));
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
