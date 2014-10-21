/**
 * 
 */
package org.openforis.idm.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.openforis.idm.model.expression.ValueExpression;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class AttributeDefault implements Serializable {

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
			ExpressionFactory expressionFactory = getExpressionFactory(attrib);
			BooleanExpression expr = expressionFactory.createBooleanExpression(condition);
			return expr.evaluate(attrib.getParent(), attrib);
		}
	}

	@SuppressWarnings("unchecked")
	private <V extends Value> V evaluateExpression(Attribute<?, V> attrib) throws InvalidExpressionException {
		ExpressionFactory expressionFactory = getExpressionFactory(attrib);
		ValueExpression defaultValueExpression = expressionFactory.createValueExpression(expression);
		Object object = defaultValueExpression.evaluate(attrib.getParent(), attrib);
		if ( object == null ) {
			return null;
		} else {
			AttributeDefinition defn = attrib.getDefinition();
			Class<? extends Value> expectedValueType = defn.getValueType();
			if ( object instanceof Value ) {
				if ( expectedValueType.isAssignableFrom(object.getClass()) ) {
					return (V) object;
				} else {
					throw new IllegalArgumentException(String.format("Unexpected value type. Found %s expected %s", expectedValueType.getName(), object.getClass().getName()));
				}
			} else {
				String stringValue = object.toString();
				V value = defn.createValue(stringValue);
				return value;
			}
		}
	}

	public Set<NodeDefinition> determineReferencedNodes(NodeDefinition context) throws InvalidExpressionException {
		Set<NodeDefinition> result = new HashSet<NodeDefinition>();
		result.addAll(calculateReferencedNodes(context, expression));
		result.addAll(calculateReferencedNodes(context, condition));
		return result;
	}
	
	private Set<NodeDefinition> calculateReferencedNodes(NodeDefinition context, String expr) throws InvalidExpressionException {
		if ( StringUtils.isBlank(expr) ) {
			return Collections.emptySet();
		}
		ModelPathExpression modelPathExpression = getExpressionFactory(context).createModelPathExpression(expr);
		Set<NodeDefinition> referencedNodes = modelPathExpression.getReferencedNodeDefinitions(context);
		return referencedNodes;
	}
	
	private ExpressionFactory getExpressionFactory(Node<?> node) {
		SurveyContext context = node.getSurvey().getContext();
		return context.getExpressionFactory();
	}
	
	private ExpressionFactory getExpressionFactory(SurveyObject surveyObj) {
		SurveyContext context = surveyObj.getSurvey().getContext();
		return context.getExpressionFactory();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
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
