package org.openforis.collect.datacleansing;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.datacleansing.xpath.XPathDataQueryEvaluator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.EntityDefinition;
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
public class DataCleansingChainExectutorTask extends Task {
	
	@Autowired
	private RecordManager recordManager;
	
	private DataCleansingChainExecutorTaskInput input;
	
	//output
	private List<DataCleansingChainExectutorTask.DataQueryExecutorError> errors;

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
		IOUtils.closeQuietly(input.nodeProcessor);
	}
	
	@Override
	@Transactional
	protected void execute() throws Throwable {
		CollectSurvey survey = input.chain.getSurvey();
		
		RecordFilter filter = createRecordsFilter();
		List<CollectRecord> recordSummaries = recordManager.loadSummaries(filter);
		
		Iterator<CollectRecord> it = recordSummaries.iterator();
		while (it.hasNext() && isRunning()) {
			CollectRecord recordSummary = (CollectRecord) it.next();
			CollectRecord record = recordManager.load(survey, recordSummary.getId(), input.step, false);
			
			for (DataCleansingStep step : input.chain.getSteps()) {
				DataQueryEvaluator queryEvaluator = createQueryEvaluator(step.getQuery());
				List<Node<?>> nodes = queryEvaluator.evaluate(record);
				for (Node<?> node : nodes) {
					processNode(step, node);
				}
			}
			incrementItemsProcessed();
		}
	}

	private void processNode(DataCleansingStep step, Node<?> node) {
		try {
			input.nodeProcessor.process(step, node);
		} catch(Exception e) {
			log().error(String.format("Error executing cleansing step %s", step.getId()), e);
			CollectRecord record = (CollectRecord) node.getRecord();
			errors.add(new DataQueryExecutorError(record.getRootEntityKeyValues(), record.getId(), node.getPath(), e.getMessage()));
		}
	}
	
	private RecordFilter createRecordsFilter() {
		CollectSurvey survey = input.chain.getSurvey();
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		Integer rootEntityId = rootEntityDef.getId();

		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(input.step);
		filter.setRootEntityId(rootEntityId);
		filter.setOffset(0);
		filter.setMaxNumberOfRecords(input.maxRecords);
		return filter;
	}
	
	private DataQueryEvaluator createQueryEvaluator(DataQuery query) {
		return new XPathDataQueryEvaluator(query);
	}
	
	public DataCleansingChainExectutorTask.DataCleansingChainExecutorTaskInput getInput() {
		return input;
	}
	
	public void setInput(DataCleansingChainExectutorTask.DataCleansingChainExecutorTaskInput input) {
		this.input = input;
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
	
	public static class DataCleansingChainExecutorTaskInput {
		
		private DataCleansingChain chain;
		private Step step;
		private Integer maxRecords;
		private DataCleansingStepNodeProcessor nodeProcessor;
		
		public DataCleansingChainExecutorTaskInput(DataCleansingChain chain, Step step, DataCleansingStepNodeProcessor nodeProcessor) {
			this(chain, step, nodeProcessor, null);
		}

		public DataCleansingChainExecutorTaskInput(DataCleansingChain chain, Step step, DataCleansingStepNodeProcessor nodeProcessor, Integer maxRecords) {
			super();
			this.chain = chain;
			this.step = step;
			this.nodeProcessor = nodeProcessor;
			this.maxRecords = maxRecords;
		}

		public DataCleansingChain getChain() {
			return chain;
		}
		
		public void setChain(DataCleansingChain chain) {
			this.chain = chain;
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
		
		public DataCleansingStepNodeProcessor getNodeProcessor() {
			return nodeProcessor;
		}
		
		public void setNodeProcessor(DataCleansingStepNodeProcessor nodeProcessor) {
			this.nodeProcessor = nodeProcessor;
		}
	}
	
	public interface DataCleansingStepNodeProcessor extends Closeable {
		
		void process(DataCleansingStep step, Node<?> node) throws Exception;
	}
}