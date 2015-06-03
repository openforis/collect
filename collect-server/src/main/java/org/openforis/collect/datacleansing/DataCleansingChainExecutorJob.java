package org.openforis.collect.datacleansing;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
import org.openforis.collect.datacleansing.DataQueryExectutorTask.DataQueryExecutorTaskInput;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExecutorJobInput;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
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
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}
	
	private class DataCleansingChainNodeProcessor implements NodeProcessor {
		
		private DataCleansingStep step;
		private CollectRecord lastRecord;
		private boolean unsavedChanges;

		public DataCleansingChainNodeProcessor(DataCleansingStep step) {
			this.step = step;
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
				if (StringUtils.isNotBlank(step.getFixExpression())) {
					Value val = expressionEvaluator.evaluateAttributeValue(attrib.getParent(), attrib, attrDefn, step.getFixExpression());
					recordManager.updateAttribute(attrib, val);
				} else {
					List<String> fieldFixExpressions = step.getFieldFixExpressions();
					List<FieldDefinition<?>> fieldDefinitions = attrDefn.getFieldDefinitions();
					for (int fieldIdx = 0; fieldIdx < fieldFixExpressions.size() && fieldIdx < fieldDefinitions.size(); fieldIdx++) {
						String fieldFixExpression = fieldFixExpressions.get(fieldIdx);
						if (StringUtils.isNotBlank(fieldFixExpression)) {
							FieldDefinition<?> fieldDefn = fieldDefinitions.get(fieldIdx);
							Object value = expressionEvaluator.evaluateFieldValue(attrib.getParent(), attrib, fieldDefn, fieldFixExpression);
							@SuppressWarnings("unchecked")
							Field<Object> field = (Field<Object>) attrib.getField(fieldIdx);
							recordManager.updateField(field, value);
						}
					}
				}
				enqueueRecordSave(record);
			}
		}

		@Override
		public void close() {
			if (lastRecord != null && unsavedChanges) {
				saveRecord(lastRecord);
			}
		}
		
		private void enqueueRecordSave(CollectRecord record) {
			if (lastRecord != null && ! lastRecord.getId().equals(record.getId())) {
				saveRecord(lastRecord);
			}
			lastRecord = record;
			unsavedChanges = true;
		}

		private void saveRecord(CollectRecord record) {
			if (recordStep == Step.ANALYSIS) {
				record.setStep(Step.CLEANSING); //save the data
				recordManager.save(record);
				record.setStep(Step.ANALYSIS); //restore the original record step
				recordManager.save(record);
			} else {
				recordManager.save(record);
			}
		}
	}

}
