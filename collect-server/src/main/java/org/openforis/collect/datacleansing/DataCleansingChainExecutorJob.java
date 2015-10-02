package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.DataQueryExectutorTask.DataQueryExecutorTaskInput;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExecutorJobInput;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.RecordDao.RecordStoreQuery;
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
	
	//input
	private DataCleansingChain chain;
	private Step recordStep;
	
	@Override
	protected void buildTasks() throws Throwable {
		List<DataCleansingStep> steps = chain.getSteps();
		for (DataCleansingStep s : steps) {
			DataQueryExectutorTask task = addTask(DataQueryExectutorTask.class);
			DataQueryExecutorTaskInput input = new DataQueryExecutorJobInput(s.getQuery(), recordStep, new DataCleansingChainNodeProcessor(s));
			task.setInput(input);
		}
	}
	
	public void setChain(DataCleansingChain chain) {
		this.chain = chain;
		this.survey = chain.getSurvey();
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	private class DataCleansingChainNodeProcessor implements NodeProcessor {
		
		private DataCleansingStep step;
		private CollectRecord lastRecord;
		private RecordUpdater recordUpdater;
		private QueryBuffer queryBuffer;
		
		public DataCleansingChainNodeProcessor(DataCleansingStep step) {
			this.step = step;
			this.recordUpdater = new RecordUpdater();
			this.queryBuffer = new QueryBuffer();
		}
		
		@Override
		public void init() throws Exception {
		}
		
		@Override
		public void process(Node<?> node) throws Exception {
			if (node instanceof Attribute) {
				@SuppressWarnings("unchecked")
				Attribute<?, Value> attrib = (Attribute<?, Value>) node;
				AttributeDefinition attrDefn = attrib.getDefinition();
				CollectRecord record = (CollectRecord) node.getRecord();
				ExpressionEvaluator expressionEvaluator = record.getSurveyContext().getExpressionEvaluator();
				
				DataCleansingStepValue stepValue = determineApplicableValue(attrib);
				
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
			}
		}

		private DataCleansingStepValue determineApplicableValue(Attribute<?, Value> attrib) throws InvalidExpressionException {
			List<DataCleansingStepValue> values = step.getUpdateValues();
			for (DataCleansingStepValue stepValue : values) {
				if (StringUtils.isBlank(stepValue.getCondition())) {
					return stepValue;
				}
				if (evaluateCondition(attrib, stepValue)) {
					return stepValue;
				}
			}
			throw new IllegalStateException("Cannot find a default applicable cleansing step value for cleansing step with id " + step.getId());
		}

		private boolean evaluateCondition(Attribute<?, Value> attrib, DataCleansingStepValue stepValue)
				throws InvalidExpressionException {
			ExpressionEvaluator expressionEvaluator = step.getSurvey().getContext().getExpressionEvaluator();
			boolean result = expressionEvaluator.evaluateBoolean(attrib, attrib, stepValue.getCondition());
			return result;
		}

		@Override
		public void close() {
			if (lastRecord != null) {
				appendLastRecordUpdate();
			}
			queryBuffer.flush();
		}

		private void appendRecordUpdate(CollectRecord record) {
			if (lastRecord != null && ! lastRecord.getId().equals(record.getId())) {
				appendLastRecordUpdate();
			}
			lastRecord = record;
		}

		private void appendLastRecordUpdate() {
			if (recordStep == Step.ANALYSIS) {
				lastRecord.setStep(Step.CLEANSING); //save the data
				appendRecordUpdateQuery(lastRecord);
				lastRecord.setStep(Step.ANALYSIS); //restore the original record step
				appendRecordUpdateQuery(lastRecord);
			} else {
				appendRecordUpdateQuery(lastRecord);
			}
		}
		
		private void appendRecordUpdateQuery(CollectRecord record) {
			queryBuffer.append(recordManager.createUpdateQuery(record));
		}
		
		private class QueryBuffer {
			
			private static final int DEFAULT_BATCH_SIZE = 100;
			
			private int bufferSize;
			private List<RecordStoreQuery> buffer;
			
			public QueryBuffer() {
				this(DEFAULT_BATCH_SIZE);
			}
			
			public QueryBuffer(int size) {
				this.bufferSize = size;
				this.buffer = new ArrayList<RecordStoreQuery>(size);
			}
			
			void append(RecordStoreQuery query) {
				buffer.add(query);
				if (buffer.size() == bufferSize) {
					flush();
				}
			}

			void flush() {
				recordManager.execute(buffer);
				buffer.clear();
			}
		}
	}

}
