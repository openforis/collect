/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.lang.DeepComparable;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class AttributeDefault implements Serializable, DeepComparable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String value;
	private String expression;
	private String condition;
	
	public AttributeDefault() {
		super();
	}
	
	public AttributeDefault(String expression) {
		this(expression, null);
	}

	public AttributeDefault(String expression, String condition) {
		this();
		this.expression = expression;
		this.condition = condition;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getExpression() {
		return this.expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getCondition() {
		return this.condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	@SuppressWarnings("unchecked")
	public <V extends Value> V evaluate(Attribute<? extends AttributeDefinition,V> attrib) throws InvalidExpressionException {
		if (StringUtils.isBlank(value)) {
			return evaluateExpression(attrib);
		} else {
			AttributeDefinition definition = attrib.getDefinition();
			return (V) definition.createValue(value);
		}
	}

	public boolean evaluateCondition(Attribute<?,?> attrib) throws InvalidExpressionException {
		if ( StringUtils.isBlank(condition) ) {
			return true;
		} else {
			ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(attrib);
			return expressionEvaluator.evaluateBoolean(attrib.getParent(), attrib, condition);
		}
	}

	private <V extends Value> V evaluateExpression(Attribute<?, V> attrib) throws InvalidExpressionException {
		ExpressionEvaluator expressionEvaluator = getExpressionEvaluator(attrib);
		V val = expressionEvaluator.evaluateAttributeValue(attrib.getParent(), attrib, attrib.getDefinition(), expression);
		return val;
	}

	public Set<NodeDefinition> determineReferencedNodes(NodeDefinition context, NodeDefinition thisNodeDef) throws InvalidExpressionException {
		Set<NodeDefinition> result = new HashSet<NodeDefinition>();
		result.addAll(calculateReferencedNodes(expression, context, thisNodeDef));
		result.addAll(calculateReferencedNodes(condition, context, thisNodeDef));
		return result;
	}
	
	private Set<NodeDefinition> calculateReferencedNodes(String expr, NodeDefinition context, NodeDefinition thisNodeDef) throws InvalidExpressionException {
		if ( StringUtils.isBlank(expr) ) {
			return Collections.emptySet();
		}
		Set<NodeDefinition> referencedNodes = getExpressionEvaluator(context).determineReferencedNodeDefinitions(context, thisNodeDef, expr);
		return referencedNodes;
	}
	
	private ExpressionEvaluator getExpressionEvaluator(Node<?> node) {
		SurveyContext<?> context = node.getSurvey().getContext();
		return context.getExpressionEvaluator();
	}
	
	private ExpressionEvaluator getExpressionEvaluator(SurveyObject surveyObj) {
		SurveyContext<?> context = surveyObj.getSurvey().getContext();
		return context.getExpressionEvaluator();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AttributeDefault clone = (AttributeDefault) super.clone();
		return clone;
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeDefault other = (AttributeDefault) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
