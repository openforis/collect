package org.openforis.collect.datacleansing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.datacleansing.manager.DataReportManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
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
public class DataReportGeneratorJob extends Job {
	
	@Autowired
	private DataReportManager reportManager;
	
	//input
	private DataQueryGroup errorQueryGroup;
	private Step recordStep;
	private User activeUser;

	//output
	private DataReport report;
	
	//temporary
	private transient ItemBatchPersister batchPersister;
	
	@Override
	protected void buildTasks() throws Throwable {
		DataQueryGroupExectutorTask task = addTask(DataQueryGroupExectutorTask.class);
		task.setInput(new DataQueryGroupExectutorTask.DataQueryGroupExecutorTaskInput((CollectSurvey) errorQueryGroup.getSurvey(), 
				errorQueryGroup.getQueries(), recordStep, new ReportItemPersisterNodeProcessor()));
	}
	
	@Override
	protected void initializeTask(Worker task) {
		super.initializeTask(task);
		report = new DataReport((CollectSurvey) errorQueryGroup.getSurvey());
		report.setQueryGroup(errorQueryGroup);
		report.setRecordStep(recordStep);
		reportManager.save(report, activeUser);
		batchPersister = new ItemBatchPersister(report);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		if (task instanceof DataQueryGroupExectutorTask) {
			DataQueryGroupExectutorTask t = (DataQueryGroupExectutorTask) task;
			report.setDatasetSize(Long.valueOf(t.getTotalItems()).intValue());
			report.setLastRecordModifiedDate(t.getLastRecordModifiedDate());
			reportManager.save(report, activeUser);
		}
		super.onTaskCompleted(task);
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		batchPersister.flush();
	}
	
	public void setQueryGroup(DataQueryGroup errorQueryGroup) {
		this.errorQueryGroup = errorQueryGroup;
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	public void setActiveUser(User activeUser) {
		this.activeUser = activeUser;
	}
	
	public DataReport getReport() {
		return report;
	}
	
	private class ReportItemPersisterNodeProcessor implements DataQueryResultProcessor {
		
		@Override
		public void init() throws Exception {}
		
		@Override
		public void process(DataQuery query, Node<?> node) throws Exception {
			DataReportItem item = createReportItem(report, query, (Attribute<?, ?>) node);
			batchPersister.add(item);
		}
		
		@Override
		public void close() throws IOException {
			batchPersister.flush();
		}
	}
	
	private DataReportItem createReportItem(DataReport report,
			DataQuery query, Attribute<?, ?> attr) {
		DataReportItem item = new DataReportItem(report, query);
		item.setRecordId(attr.getRecord().getId());
		item.setParentEntityId(attr.getParent().getInternalId());
		item.setNodeIndex(attr.getIndex());
		item.setValue(new JSONValueFormatter().formatValue(attr));
		return item;
	}

	private class ItemBatchPersister {
		
		private static final int MAX_SIZE = 10000;
		
		private List<DataReportItem> items;

		private DataReport report;
		
		public ItemBatchPersister(DataReport report) {
			items = new ArrayList<DataReportItem>();
		}
		
		public void add(DataReportItem item) {
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
