package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.datacleansing.xpath.XPathDataQueryEvaluator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
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
public class DataErrorQueryGroupExectutorTask extends Task {
	
	@Autowired
	private RecordManager recordManager;
	
	DataErrorQueryGroupExecutorTaskInput input;
	
	//output
	private List<DataErrorQueryExecutorError> errors;

	@Override
	protected long countTotalItems() {
		RecordFilter recordsFilter = createRecordsFilter();
		int count = recordManager.countRecords(recordsFilter);
		return count;
	}
	
	@Override
	protected void initializeInternalVariables() throws Throwable {
		super.initializeInternalVariables();
		this.errors = new ArrayList<DataErrorQueryExecutorError>();
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
		
		List<CollectRecord> recordSummaries = recordManager.loadSummaries(filter);
		
		Iterator<CollectRecord> it = recordSummaries.iterator();
		while (it.hasNext() && isRunning()) {
			CollectRecord recordSummary = (CollectRecord) it.next();
			CollectRecord record = recordManager.load(survey, recordSummary.getId(), input.step, false);
			List<DataErrorQuery> queries = input.getQueries();
			for (DataErrorQuery query : queries) {
				DataQueryEvaluator queryEvaluator = createQueryEvaluator(query.getQuery());
				List<Node<?>> nodes = queryEvaluator.evaluate(record);
				for (Node<?> node : nodes) {
					processNode(query, node);
				}
			}
			incrementItemsProcessed();
		}
	}

	private void processNode(DataErrorQuery query, Node<?> node) {
		try {
			input.resultProcessor.process(query, node);
		} catch(Exception e) {
			log().error(String.format("Error executing query %s", query.getId()), e);
			CollectRecord record = (CollectRecord) node.getRecord();
			errors.add(new DataErrorQueryExecutorError(record.getRootEntityKeyValues(), record.getId(), node.getPath(), e.getMessage()));
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
	
	public DataErrorQueryGroupExecutorTaskInput getInput() {
		return input;
	}
	
	public void setInput(DataErrorQueryGroupExecutorTaskInput input) {
		this.input = input;
	}
	
	public static class DataErrorQueryExecutorError {
		private List<String> recordKeys;
		private int recordId;
		private String attributePath;
		private String errorMessage;
		
		public DataErrorQueryExecutorError(List<String> recordKeys,
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
	
	public static class DataErrorQueryGroupExecutorTaskInput {
		
		private CollectSurvey survey;
		private List<DataErrorQuery> queries;
		private Step step;
		private Integer maxRecords;
		private DataErrorQueryResultProcessor resultProcessor;
		
		public DataErrorQueryGroupExecutorTaskInput(CollectSurvey survey, List<DataErrorQuery> queries, 
				Step step, DataErrorQueryResultProcessor resultProcessor) {
			this(survey, queries, step, resultProcessor, null);
		}

		public DataErrorQueryGroupExecutorTaskInput(CollectSurvey survey, List<DataErrorQuery> queries, 
				Step step, DataErrorQueryResultProcessor resultProcessor, Integer maxRecords) {
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

		public List<DataErrorQuery> getQueries() {
			return queries;
		}
		
		public void setQueries(List<DataErrorQuery> queries) {
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
		
		public DataErrorQueryResultProcessor getResultProcessor() {
			return resultProcessor;
		}
		
		public void setResultProcessor(DataErrorQueryResultProcessor resultProcessor) {
			this.resultProcessor = resultProcessor;
		}
	}
	
}