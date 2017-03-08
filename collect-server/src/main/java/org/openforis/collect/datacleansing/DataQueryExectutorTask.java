package org.openforis.collect.datacleansing;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.datacleansing.xpath.XPathDataQueryEvaluator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeProcessor;
import org.openforis.collect.model.RecordFilter;
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
public class DataQueryExectutorTask extends Task {
	
	@Autowired
	private RecordManager recordManager;
	
	DataQueryExectutorTask.DataQueryExecutorTaskInput input;
	
	//output
	private List<DataQueryExectutorTask.DataQueryExecutorError> errors;

	@Override
	protected long countTotalItems() {
		RecordFilter recordsFilter = createRecordsFilter(false);
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
		if (input.nodeProcessor instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) input.nodeProcessor);
		}
	}
	
	@Override
	@Transactional
	protected void execute() throws Throwable {
		CollectSurvey survey = input.query.getSurvey();
		
		DataQueryEvaluator queryEvaluator = createQueryEvaluator(input.query);

		RecordFilter filter = createRecordsFilter(true);
		
		List<CollectRecord> recordSummaries = recordManager.loadSummaries(filter);
		
		Iterator<CollectRecord> it = recordSummaries.iterator();
		while (it.hasNext() && isRunning()) {
			CollectRecord recordSummary = (CollectRecord) it.next();
			CollectRecord record = recordManager.load(survey, recordSummary.getId(), input.step, false);
			List<Node<?>> nodes = queryEvaluator.evaluate(record);
			for (Node<?> node : nodes) {
				processNode(node);
			}
			incrementProcessedItems();
		}
	}

	private void processNode(Node<?> node) {
		try {
			input.nodeProcessor.process(node);
		} catch(Exception e) {
			log().error(String.format("Error executing query %s", input.query.getId()), e);
			CollectRecord record = (CollectRecord) node.getRecord();
			errors.add(new DataQueryExecutorError(record.getRootEntityKeyValues(), record.getId(), node.getPath(), e.getMessage()));
		}
	}
	
	private RecordFilter createRecordsFilter(boolean limitResults) {
		CollectSurvey survey = input.query.getSurvey();
		EntityDefinition entityDef = (EntityDefinition) survey.getSchema().getDefinitionById(input.query.getEntityDefinitionId());
		EntityDefinition rootEntityDef = entityDef.getRootEntity();
		Integer rootEntityId = rootEntityDef.getId();

		RecordFilter filter = new RecordFilter(survey);
		filter.setStep(input.step);
		filter.setRootEntityId(rootEntityId);
		if (limitResults) {
			filter.setOffset(0);
			filter.setMaxNumberOfRecords(input.maxRecords);
		}
		return filter;
	}
	
	private DataQueryEvaluator createQueryEvaluator(DataQuery query) {
		return new XPathDataQueryEvaluator(query);
	}
	
	public DataQueryExectutorTask.DataQueryExecutorTaskInput getInput() {
		return input;
	}
	
	public void setInput(DataQueryExectutorTask.DataQueryExecutorTaskInput input) {
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
	
	public static class DataQueryExecutorTaskInput {
		
		private DataQuery query;
		private Step step;
		private Integer maxRecords;
		private NodeProcessor nodeProcessor;
		
		public DataQueryExecutorTaskInput(DataQuery query, Step step, NodeProcessor nodeProcessor) {
			this(query, step, nodeProcessor, null);
		}

		public DataQueryExecutorTaskInput(DataQuery query, Step step, NodeProcessor nodeProcessor, Integer maxRecords) {
			super();
			this.query = query;
			this.step = step;
			this.nodeProcessor = nodeProcessor;
			this.maxRecords = maxRecords;
		}

		public DataQuery getQuery() {
			return query;
		}
		
		public void setQuery(DataQuery query) {
			this.query = query;
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
		
		public NodeProcessor getNodeProcessor() {
			return nodeProcessor;
		}
		
		public void setNodeProcessor(NodeProcessor nodeProcessor) {
			this.nodeProcessor = nodeProcessor;
		}
	}
}