package org.openforis.collect.io.data;

import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class DescendantNodeFilter implements NodeFilter {

	private AttributeDefinition descendantAttributeDefinition;
	private String descendantAttributeCondition;

	public DescendantNodeFilter(AttributeDefinition nestedAttributeDefinition, 
			String nestedAttributeCondition) {
		super();
		this.descendantAttributeDefinition = nestedAttributeDefinition;
		this.descendantAttributeCondition = nestedAttributeCondition;
	}

	@Override
	public boolean accept(Node<?> node) {
		NodeDefinition nodeDef = node.getDefinition();
		if (! (nodeDef instanceof EntityDefinition) 
				|| ! descendantAttributeDefinition.isDescendantOf((EntityDefinition) nodeDef)) {
			return false;
		}
		Record record = node.getRecord();
		SurveyContext<?> surveyContext = record.getSurveyContext();
		ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
		List<Node<?>> attributes = record.findNodesByPath(descendantAttributeDefinition.getPath());
		for (Node<?> attribute : attributes) {
			try {
				Entity parentEntity = attribute.getParent();
				if (parentEntity == node && 
						expressionEvaluator.evaluateBoolean(parentEntity, attribute, descendantAttributeCondition)) {
					return true;
				}
			} catch (InvalidExpressionException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

}
