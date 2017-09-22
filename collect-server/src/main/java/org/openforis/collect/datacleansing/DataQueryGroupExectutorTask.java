package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.datacleansing.xpath.XPathDataQueryEvaluator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DataQueryGroupExectutorTask extends Task {
	
	@Autowired
	private RecordManager recordManager;
	
	DataQueryGroupExecutorTaskInput input;
	
	//output
	private List<DataQueryExecutorError> errors;
	private Date lastRecordModifiedDate;

	@Override
	protected long countTotalItems() {
		RecordFilter recordsFilter = createRecordsFilter();
		int count = recordManager.countRecords(recordsFilter);
		return count;
	}
	
	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		this.errors = new ArrayList<DataQueryExecutorError>();
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		IOUtils.closeQuietly(input.resultProcessor);
	}
	
	@Override
	@Transactional
	protected void execute() throws Throwable {
		CollectSurvey survey = input.getSurvey();
		
		RecordFilter filter = createRecordsFilter();
		
		List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(filter);
		
		Iterator<CollectRecordSummary> it = recordSummaries.iterator();
		while (it.hasNext() && isRunning()) {
			CollectRecordSummary recordSummary = (CollectRecordSummary) it.next();
			Date modifiedDate = recordSummary.getModifiedDate();
			if (lastRecordModifiedDate == null) {
				lastRecordModifiedDate = modifiedDate;
			} else if (modifiedDate.compareTo(lastRecordModifiedDate) > 0) {
				lastRecordModifiedDate = modifiedDate;
			}
			CollectRecord record = recordManager.load(survey, recordSummary.getId(), input.step, false);
			List<DataQuery> queries = input.getQueries();
			for (DataQuery query : queries) {
				DataQueryEvaluator queryEvaluator = createQueryEvaluator(query);
				List<Node<?>> nodes = queryEvaluator.evaluate(record);
				for (Node<?> node : nodes) {
					processNode(query, node);
				}
			}
			incrementProcessedItems();
		}
	}

	private void processNode(DataQuery query, Node<?> node) {
		try {
			input.resultProcessor.process(query, node);
		} catch(Exception e) {
			log().error(String.format("Error executing query %s", query.getId()), e);
			CollectRecord record = (CollectRecord) node.getRecord();
			errors.add(new DataQueryExecutorError(record.getRootEntityKeyValues(), record.getId(), node.getPath(), e.getMessage()));
		}
	}
	
	private RecordFilter createRecordsFilter() {
		CollectSurvey survey = input.getSurvey();
		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(input.step);
		filter.setOffset(0);
		filter.setMaxNumberOfRecords(input.maxRecords);
		return filter;
	}
	
	private DataQueryEvaluator createQueryEvaluator(DataQuery query) {
		return new XPathDataQueryEvaluator(query);
	}
	
	public DataQueryGroupExecutorTaskInput getInput() {
		return input;
	}
	
	public void setInput(DataQueryGroupExecutorTaskInput input) {
		this.input = input;
	}
	
	public Date getLastRecordModifiedDate() {
		return lastRecordModifiedDate;
	}
	
	public static class DataQueryExecutorError {
		private List<String> recordKeys;
		private int recordId;
		private String attributePath;
		private String errorMessage;
		
		public DataQueryExecutorError(List<String> recordKeys,
				int recordId, String attributePath, String errorMessage) {
			super();
			this.recordKeys = recordKeys;
			this.recordId = recordId;
			this.attributePath = attributePath;
			this.errorMessage = errorMessage;
		}

		public int getRecordId() {
			return recordId;
		}
		
		public List<String> getRecordKeys() {
			return recordKeys;
		}
		
		public String getAttributePath() {
			return attributePath;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
	}
	
	public static class DataQueryGroupExecutorTaskInput {
		
		private CollectSurvey survey;
		private List<DataQuery> queries;
		private Step step;
		private Integer maxRecords;
		private DataQueryResultProcessor resultProcessor;
		
		public DataQueryGroupExecutorTaskInput(CollectSurvey survey, List<DataQuery> queries, 
				Step step, DataQueryResultProcessor resultProcessor) {
			this(survey, queries, step, resultProcessor, null);
		}

		public DataQueryGroupExecutorTaskInput(CollectSurvey survey, List<DataQuery> queries, 
				Step step, DataQueryResultProcessor resultProcessor, Integer maxRecords) {
			super();
			this.survey = survey;
			this.queries = queries;
			this.step = step;
			this.resultProcessor = resultProcessor;
			this.maxRecords = maxRecords;
		}
		
		public CollectSurvey getSurvey() {
			return survey;
		}

		public List<DataQuery> getQueries() {
			return queries;
		}
		
		public void setQueries(List<DataQuery> queries) {
			this.queries = queries;
		}
		
		public Step getStep() {
			return step;
		}
		
		public void setStep(Step step) {
			this.step = step;
		}
		
		public Integer getMaxRecords() {
			return maxRecords;
		}
		
		public void setMaxRecords(Integer maxRecords) {
			this.maxRecords = maxRecords;
		}
		
		public DataQueryResultProcessor getResultProcessor() {
			return resultProcessor;
		}
		
		public void setResultProcessor(DataQueryResultProcessor resultProcessor) {
			this.resultProcessor = resultProcessor;
		}
	}
	
}