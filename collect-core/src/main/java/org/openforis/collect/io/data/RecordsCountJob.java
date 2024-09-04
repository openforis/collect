package org.openforis.collect.io.data;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.RecordFilter;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private class RecordsCountTask extends Task {
		@Override
		protected long countTotalItems() {
			int totalRecords = recordManager.countRecords(recordFilter);
			return totalRecords;
		}
		@Override
		protected void execute() throws Throwable {
			int count = 0;
			List<CollectRecordSummary> summaries = recordManager.loadSummaries(recordFilter);
			for (CollectRecordSummary s : summaries) {
				if ( isRunning() ) {
					CollectRecord record = recordManager.load(survey, s.getId(), recordFilter.getStepGreaterOrEqual(), 
							false, alwaysEvaluateCalculatedAttributes);
					if (isFilterExpressionVerified(record, recordFilter.getFilterExpression())) {
						count ++;
					}
					incrementProcessedItems();
				}
			}
		}
		
		private boolean isFilterExpressionVerified(CollectRecord record, String expression) {
			if (StringUtils.isBlank(expression)) return true;
			ExpressionEvaluator expressionEvaluator = record.getSurvey().getContext().getExpressionEvaluator();
			try {
				return expressionEvaluator.evaluateBoolean(record.getRootEntity(), null, expression);
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public int getProgressPercent() {
			return 0;
		}
	}
}
