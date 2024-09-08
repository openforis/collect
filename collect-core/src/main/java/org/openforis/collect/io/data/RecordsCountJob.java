package org.openforis.collect.io.data;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.RecordFilter;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RecordsCountJob extends SurveyLockingJob {
	@Autowired
	private RecordManager recordManager;
	
	// input
	private RecordFilter recordFilter;
	public boolean alwaysEvaluateCalculatedAttributes;

	@Override
	protected void buildTasks() throws Throwable {
		this.addTask(new RecordsCountTask());
	}
	
	@Override
	protected Map<String, Object> prepareResult() {
		 Map<String, Object> result = super.prepareResult();
		 result.putAll(getTasks().get(0).getResult());
		 return result;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	private class RecordsCountTask extends Task {

		// output
		private int recordsCount;

		@Override
		protected long countTotalItems() {
			int totalRecords = recordManager.countRecords(recordFilter);
			return totalRecords;
		}
		@Override
		protected void execute() throws Throwable {
			this.recordsCount = 0;
			List<CollectRecordSummary> summaries = recordManager.loadSummaries(recordFilter);
			for (CollectRecordSummary s : summaries) {
				if ( isRunning() ) {
					String filterExpression = recordFilter.getFilterExpression();
					if (StringUtils.isNotBlank(filterExpression)) {
						CollectRecord record = recordManager.load(survey, s.getId(), recordFilter.getStepGreaterOrEqual(), 
								false, alwaysEvaluateCalculatedAttributes);
						if (isFilterExpressionVerified(record, filterExpression)) {
							this.recordsCount ++;
						}
					} else {
						this.recordsCount ++;
					}
					incrementProcessedItems();
				}
			}
		}
		
		private boolean isFilterExpressionVerified(CollectRecord record, String expression) {
			ExpressionEvaluator expressionEvaluator = record.getSurvey().getContext().getExpressionEvaluator();
			try {
				return expressionEvaluator.evaluateBoolean(record.getRootEntity(), null, expression);
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		protected Map<String, Object> prepareResult() {
			 Map<String, Object> result = super.prepareResult();
			 result.put("recordsCount", this.recordsCount);
			 return result;
		}

	}
}
