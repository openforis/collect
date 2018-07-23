package org.openforis.collect.datacleansing;

import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.DataQueryExectutorTask.DataQueryExecutorTaskInput;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.NodeProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataQueryExecutorJob extends SurveyLockingJob {

	private DataQueryExecutorJobInput input;

	@Override
	protected void buildTasks() throws Throwable {
		addDataQueryExecutorTask();
	}

	protected DataQueryExectutorTask addDataQueryExecutorTask() {
		DataQueryExectutorTask task = addTask(DataQueryExectutorTask.class);
		task.input = input;
		return task;
	}
	
	public DataQueryExecutorJobInput getInput() {
		return input;
	}
	
	public void setInput(DataQueryExecutorJobInput input) {
		this.input = input;
	}
	
	public static class DataQueryExecutorJobInput extends DataQueryExecutorTaskInput {

		public DataQueryExecutorJobInput(DataQuery query,
				Step step, NodeProcessor nodeProcessor) {
			super(query, step, nodeProcessor);
		}

		public DataQueryExecutorJobInput(DataQuery query,
				Step step, NodeProcessor nodeProcessor, Integer maxRecords) {
			super(query, step, nodeProcessor, maxRecords);
		}
	}
}
