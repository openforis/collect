package org.openforis.collect.datacleansing.xpath;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryEvaluator;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class XPathDataQueryEvaluator implements DataQueryEvaluator {
	
	private DataQuery query;
	
	public XPathDataQueryEvaluator(DataQuery query) {
		this.query = query;
	}

	@Override
	public List<Node<?>> evaluate(CollectRecord record) {
		List<Node<?>> result = new ArrayList<Node<?>>();
		
		String condition = query.getConditions();
		AttributeDefinition attrDef = query.getAttributeDefinition();
		
		SurveyContext surveyContext = record.getSurveyContext();
		ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
		List<Node<?>> nodes = record.findNodesByPath(attrDef.getPath());
		for (Node<?> node : nodes) {
			try {
				if (expressionEvaluator.evaluateBoolean(node.getParent(), node, condition)) {
					result.add(node);
				}
			} catch (InvalidExpressionException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}
	
}