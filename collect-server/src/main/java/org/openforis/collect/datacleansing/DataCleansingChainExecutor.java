package org.openforis.collect.datacleansing;

import java.util.List;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class DataCleansingChainExecutor {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private DataQueryExecutor queryExecutor;
	
	public void execute(DataCleansingChain chain, Step step) {
		List<DataCleansingStep> steps = chain.getSteps();
		for (DataCleansingStep s : steps) {
			try {
				DataQuery query = s.getQuery();
				DataQueryResultIterator it = queryExecutor.execute(query, step);
				while (it.hasNext()) {
					Node<?> node = it.next();
					if (node instanceof Attribute) {
						@SuppressWarnings("unchecked")
						Attribute<?, Value> attrib = (Attribute<?, Value>) node;
						CollectRecord record = (CollectRecord) node.getRecord();
						ExpressionEvaluator expressionEvaluator = record.getSurveyContext().getExpressionEvaluator();
						Value val = expressionEvaluator.evaluateAttributeValue(attrib.getParent(), attrib, attrib.getDefinition(), s.getFixExpression());
						attrib.setValue(val);
						recordManager.save(record);
					}
				}
			} catch (InvalidExpressionException e) {
				//TODO
			}
		}
	}
	
}
