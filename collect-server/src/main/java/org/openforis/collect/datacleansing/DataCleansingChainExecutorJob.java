package org.openforis.collect.datacleansing;

import java.util.Collection;
import java.util.List;

import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExectutorTask.DataQueryExecutorTaskInput;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.concurrency.Task;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingChainExecutorJob extends DataQueryExecutorJob {
	
	@Autowired
	private RecordManager recordManager;
	
	//input
	private DataCleansingChain chain;
	private Step recordStep;
	
	@Override
	protected <C extends Collection<? extends Task>> void addTasks(C tasks) {
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

		public DataCleansingChainNodeProcessor(DataCleansingStep step) {
			this.step = step;
		}
		
		@Override
		public void process(Node<?> node) throws Exception {
			if (node instanceof Attribute) {
				@SuppressWarnings("unchecked")
				Attribute<?, Value> attrib = (Attribute<?, Value>) node;
				CollectRecord record = (CollectRecord) node.getRecord();
				ExpressionEvaluator expressionEvaluator = record.getSurveyContext().getExpressionEvaluator();
				Value val = expressionEvaluator.evaluateAttributeValue(attrib.getParent(), attrib, attrib.getDefinition(), step.getFixExpression());
				attrib.setValue(val);
				recordManager.save(record);
			}
		}
	}

}
