package org.openforis.collect.datacleansing;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.DataCleansingChainExectutorTask.DataCleansingChainExecutorTaskInput;
import org.openforis.collect.datacleansing.DataCleansingChainExectutorTask.DataCleansingStepNodeProcessor;
import org.openforis.collect.datacleansing.DataCleansingChainExectutorTask.DataCleansingStepNodeProcessorResult;
import org.openforis.collect.datacleansing.manager.DataCleansingReportManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.jooq.JooqDaoSupport.CollectStoreQueryBuffer;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DataCleansingChainExecutorJob extends SurveyLockingJob {
	
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private DataCleansingReportManager reportManager;
	
	//input
	private DataCleansingChain chain;
	private Step recordStep;

	@Override
	protected void buildTasks() throws Throwable {
		DataCleansingChainExectutorTask task = addTask(DataCleansingChainExectutorTask.class);
		task.setInput(new DataCleansingChainExecutorTaskInput(chain, recordStep, new DataCleansingChainNodeProcessor(chain)));
	}
	
	@Override
	protected void onCompleted() {
		super.onCompleted();
		DataCleansingChainExectutorTask task = getChainExecutorTask();
		if (chain.getId() != null) {
			DataCleansingReport report = new DataCleansingReport(survey);
			report.setDatasetSize(task.getDatasetSize());
			report.setRecordStep(recordStep);
			report.setLastRecordModifiedDate(task.getLastRecordModifiedDate());
			report.setCleansingChainId(chain.getId());
			report.setCleansedRecords(task.getCleansedRecords());
			report.setCleansedNodes(task.getCleansedNodes());
			reportManager.save(report);
		}
	}
	
	public void setChain(DataCleansingChain chain) {
		this.chain = chain;
		this.survey = chain.getSurvey();
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	private DataCleansingChainExectutorTask getChainExecutorTask() {
		DataCleansingChainExectutorTask task;
		if (getTasks().isEmpty()) {
			task = null;
		} else {
			task = (DataCleansingChainExectutorTask) getTasks().get(0);
		}
		return task;
	}
	
	public int getUpdatedRecords() {
		DataCleansingChainExectutorTask task = getChainExecutorTask();
		return task == null ? 0 : task.getCleansedRecords();
	}
	
	public int getProcessedNodes() {
		DataCleansingChainExectutorTask task = getChainExecutorTask();
		return task == null ? 0 : task.getCleansedNodes();
	}
	
	private class DataCleansingChainNodeProcessor implements DataCleansingStepNodeProcessor {
		
		private DataCleansingChain chain;
		private CollectRecord lastRecord;
		private RecordUpdater recordUpdater;
		private CollectStoreQueryBuffer queryBuffer;
		
		public DataCleansingChainNodeProcessor(DataCleansingChain chain) {
			this.chain = chain;
			this.recordUpdater = new RecordUpdater();
			this.queryBuffer = new CollectStoreQueryBuffer();
		}
		
		@Override
		public DataCleansingStepNodeProcessorResult process(DataCleansingStep step, Node<?> node) throws Exception {
			switch (step.getType()) {
			case ATTRIBUTE_UPDATE:
				if (! (node instanceof Attribute)) {
					throw new IllegalArgumentException("Invalid node type for attribute update: " + node.getClass().getName());
				}
				@SuppressWarnings("unchecked")
				Attribute<?, Value> attrib = (Attribute<?, Value>) node;
				AttributeDefinition attrDefn = attrib.getDefinition();
				CollectRecord record = (CollectRecord) node.getRecord();
				ExpressionEvaluator expressionEvaluator = record.getSurveyContext().getExpressionEvaluator();
				
				DataCleansingStepValue stepValue = determineApplicableValue(step, attrib);
				if (stepValue == null) {
					return null;
				}
				switch(stepValue.getUpdateType()) {
				case ATTRIBUTE:
					Value val = expressionEvaluator.evaluateAttributeValue(attrib.getParent(), attrib, attrDefn, stepValue.getFixExpression());
					recordUpdater.updateAttribute(attrib, val);
					break;
				case FIELD:
					List<String> fieldFixExpressions = stepValue.getFieldFixExpressions();
					List<FieldDefinition<?>> fieldDefinitions = attrDefn.getFieldDefinitions();
					for (int fieldIdx = 0; fieldIdx < fieldFixExpressions.size() && fieldIdx < fieldDefinitions.size(); fieldIdx++) {
						String fieldFixExpression = fieldFixExpressions.get(fieldIdx);
						if (StringUtils.isNotBlank(fieldFixExpression)) {
							FieldDefinition<?> fieldDefn = fieldDefinitions.get(fieldIdx);
							Object value = expressionEvaluator.evaluateFieldValue(attrib.getParent(), attrib, fieldDefn, fieldFixExpression);
							@SuppressWarnings("unchecked")
							Field<Object> field = (Field<Object>) attrib.getField(fieldIdx);
							recordUpdater.updateField(field, value);
						}
					}
					break;
				}
				appendRecordUpdate(record);
				return DataCleansingStepNodeProcessorResult.ATTRIBUTE_UPDATED;
			case ENTITY_DELETE:
				recordUpdater.deleteNode(node.getParent());
				return DataCleansingStepNodeProcessorResult.ENTITY_DELETED;
			case RECORD_DELETE:
				return DataCleansingStepNodeProcessorResult.RECORD_TO_BE_DELETED;
			default:
				return null;
			}
		}

		private DataCleansingStepValue determineApplicableValue(DataCleansingStep step, Attribute<?, Value> attrib) throws InvalidExpressionException {
			List<DataCleansingStepValue> values = step.getUpdateValues();
			for (DataCleansingStepValue stepValue : values) {
				if (StringUtils.isBlank(stepValue.getCondition())) {
					return stepValue;
				}
				if (evaluateCondition(attrib, stepValue)) {
					return stepValue;
				}
			}
			throw new IllegalStateException("Cannot find a default applicable cleansing step value for cleansing step with id " + chain.getId());
		}

		private boolean evaluateCondition(Attribute<?, Value> attrib, DataCleansingStepValue stepValue)
				throws InvalidExpressionException {
			ExpressionEvaluator expressionEvaluator = chain.getSurvey().getContext().getExpressionEvaluator();
			boolean result = expressionEvaluator.evaluateBoolean(attrib.getParent(), attrib, stepValue.getCondition());
			return result;
		}

		@Override
		public void close() {
			if (lastRecord != null) {
				appendLastRecordUpdate();
			}
			recordManager.execute(queryBuffer.flush());
		}

		private void appendRecordUpdate(CollectRecord record) {
			if (lastRecord != null && ! lastRecord.getId().equals(record.getId())) {
				appendLastRecordUpdate();
			}
			lastRecord = record;
		}

		private void appendLastRecordUpdate() {
			appendRecordUpdateQuery(lastRecord, lastRecord.getDataStep(), lastRecord.getDataWorkflowSequenceNumber());
		}
		
		private void appendRecordUpdateQuery(CollectRecord record, Step step, int dataSequenceNumber) {
			record.updateSummaryFields();
			queryBuffer.append(recordManager.createDataUpdateQuery(record, record.getId(), step, dataSequenceNumber));
		}
	}

}
